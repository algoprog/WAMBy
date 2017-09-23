package Rankers;

import Core.Config;
import Core.Wamby;
import Semantic.TextSimilarity;
import TextModel.Factoid;
import TextModel.Paragraph;
import Extractors.FeaturesExtractor;
import TextModel.Word;
import WebSearch.WebSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Chris Samarinas
 */
public class FactoidRanker {

    public static ArrayList<Factoid> rankFactoids(ArrayList<Factoid> factoids, String query_txt, boolean webscore, boolean in_results, String type) throws IOException {

        String query_clean = query_txt.replaceAll("[^\\w\\s]","").trim();;
        Paragraph query = new Paragraph(0, 0, query_clean, "");
        Paragraph features = FeaturesExtractor.getFeatures(query_clean);

        HashMap<String, Double> scores = new HashMap<>();
        HashMap<String, Double> context_scores = new HashMap<>();
        HashMap<String, Double> title_scores = new HashMap<>();
        HashMap<String, Double> web_position_scores = new HashMap<>();
        HashMap<String, Integer> ids = new HashMap<>();
        HashMap<String, Integer> occurrences = new HashMap<>();

        int j = 0;
        for(Factoid f : factoids) {
            double num = 0;
            double denom = 0;

            ArrayList<Word> left_window = f.getLeftWindow();
            int i = 1;
            for(Word w : left_window) {
                double idf = Wamby.textSimilarity.getIDF(w.getText());
                double query_sim = maxSim(w.getText(), query);
                double features_sim = maxSim(w.getText(), features);
                double sim = Math.max(query_sim, features_sim);
                num += idf * sim / i;
                denom += idf / i;
                i++;
            }

            ArrayList<Word> right_window = f.getRightWindow();
            i = 1;
            for(Word w : right_window) {
                double idf = Wamby.textSimilarity.getIDF(w.getText());
                double query_sim = maxSim(w.getText(), query);
                double features_sim = maxSim(w.getText(), features);
                double sim = Math.max(query_sim, features_sim);
                num += idf * sim / i;
                denom += idf / i;
                i++;
            }

            double context_score = 0;
            if(denom > 0) {
                context_score = num / denom;
            }

            double title_score = Wamby.textSimilarity.title_sim(query, new Paragraph(0,0,f.getWebTitle(),""));
            double web_position_score = 1-(f.getWebPosition()-1)/Config.WEB_RESULTS_TOTAL;

            Paragraph context = new Paragraph(0,0,f.getContext(),"");

            double ngrams_score = Wamby.textSimilarity.ngrams_sim(query, context);
            double relevancy = 0.85*Wamby.textSimilarity.combined_sim(features, context) + 0.15*ngrams_score;

            if(ids.containsKey(f.getText())) {
                double temp_score = scores.get(f.getText());
                if(relevancy > temp_score) {
                    ids.remove(f.getText());
                    ids.put(f.getText(),j);
                    scores.remove(f.getText());
                    scores.put(f.getText(),relevancy);
                }
                temp_score = context_scores.get(f.getText());
                if(context_score > temp_score) {
                    context_scores.remove(f.getText());
                }else {
                    context_score = temp_score;
                }
                temp_score = title_scores.get(f.getText());
                if(title_score > temp_score) {
                    title_scores.remove(f.getText());
                }else {
                    title_score = temp_score;
                }
                temp_score = web_position_scores.get(f.getText());
                if(web_position_score > temp_score) {
                    web_position_scores.remove(f.getText());
                }else {
                    web_position_score = temp_score;
                }

                if(f.getWebPosition()!=factoids.get(ids.get(f.getText())).getWebPosition()) {
                    int c = occurrences.get(f.getText());
                    occurrences.remove(f.getText());
                    occurrences.put(f.getText(), c+1);
                }
            }else {
                ids.put(f.getText(),j);
                occurrences.put(f.getText(),1);
            }

            double score;
            if(!in_results) {
                score = 0.5*context_score + 0.1*ngrams_score + 0.2*title_score + 0.2*web_position_score;
            }else {
                score = 0.25*context_score + 0.45*web_position_score + 0.3*(occurrences.get(f.getText())/Config.WEB_RESULTS_TOTAL);
            }
            factoids.get(ids.get(f.getText())).setScore(score);
            //System.out.println("context: "+context_score+" ngrams: "+ngrams_score+" title: "+title_score+" web: "+web_position_score);

            scores.put(f.getText(),relevancy);
            context_scores.put(f.getText(),context_score);
            title_scores.put(f.getText(),title_score);
            web_position_scores.put(f.getText(),web_position_score);

            j++;
        }

        ArrayList<Factoid> merged_factoids = new ArrayList<>();

        for(Integer id : ids.values()) {
            merged_factoids.add(factoids.get(id));
        }

        Collections.sort(merged_factoids);

        int factoids_count = Math.min(100,merged_factoids.size());

        ArrayList<Factoid> top_factoids = new ArrayList<>();
        ArrayList<Factoid> top_factoids_rescored = new ArrayList<>();
        for(int i=0;i<factoids_count;i++) {
            Factoid f = merged_factoids.get(i);
            top_factoids.add(f);
            top_factoids_rescored.add(f);
            //System.out.println(f.getText()+" : "+f.getScore());
            //System.out.println(f.getText()+" : "+occurrences.get(f.getText()));
        }

        if(!type.equals("PERSON") && !type.equals("LOCATION") && !type.equals("ORGANIZATION") && !type.equals("DATE")) {
            return top_factoids;
        }

        for(int i=0; i<factoids_count; i++) {
            Factoid f1 = top_factoids.get(i);
            Factoid f1_r = top_factoids_rescored.get(i);
            for(int k=i+1;k<factoids_count;k++) {
                double sim = Wamby.textSimilarity.sim_tfidf_wv_3(
                        new Paragraph(0,0,top_factoids.get(i).getText(),""),
                        new Paragraph(0,0,top_factoids.get(k).getText(),"")
                );

                Factoid f2 = top_factoids.get(k);
                Factoid f2_r = top_factoids_rescored.get(k);

                f2_r.setScore(f2_r.getScore()+1/factoids_count*sim*f1.getScore());
                f1_r.setScore(f1_r.getScore()+1/factoids_count*sim*f2.getScore());
            }
        }

        //System.out.println("-------------------------------------------------------------------------");

        for(int i=0; i<factoids_count; i++) {
            Factoid f = top_factoids.get(i);
            f.setScore(f.getScore()/2);
            //System.out.println(f.getText()+" : "+f.getScore());
        }

        top_factoids = top_factoids_rescored;

        if(!webscore) {
            return top_factoids;
        }

        System.out.println("Calculating web scores...");

        ArrayList<Integer> web_scores = new ArrayList<>();
        ArrayList<Integer> sorted_web_scores = new ArrayList<>();
        for(Factoid f : top_factoids) {
            System.out.println("Results for: "+query_clean + " " + f.getText());
            int web_score = (int)(WebSearch.resultsCount(query_clean + " " + f.getText())/1000);
            System.out.println(web_score);
            web_scores.add(web_score);
            sorted_web_scores.add(web_score);
        }

        Collections.sort(sorted_web_scores, Collections.<Integer>reverseOrder());
        HashMap<Integer, Integer> higher_than = new HashMap<>();
        int k = 0;
        for(Integer s : sorted_web_scores) {
            higher_than.put(s, factoids_count-k-1);
            k++;
        }

        ArrayList<Factoid> final_factoids = new ArrayList<>();

        for(int i=0;i<factoids_count;i++) {
            double score = top_factoids.get(i).getScore();
            double web_score = (double)(higher_than.get(web_scores.get(i))) / factoids_count;
            if(web_score > 0.4) {
                Factoid f = top_factoids.get(i);
                f.setScore(score * web_score);
                final_factoids.add(f);
                System.out.println(f.getText()+" : "+web_score);
            }
        }

        Collections.sort(final_factoids);

        return final_factoids;
    }

    public static double maxSim(String word, Paragraph query) {
        double max_sim = 0;
        double[] word_vector = Wamby.wordVectors.getWordVector(word.toLowerCase());
        for(Word w : query.getVocabulary().keySet()) {
            //System.out.println(w.getText());
            double[] query_word_vector = Wamby.wordVectors.getWordVector(w.getText());
            double similarity = TextSimilarity.cosineSimilarity(word_vector, query_word_vector);
            if(similarity > max_sim) {
                max_sim = similarity;
            }
        }

        return max_sim;
    }
}
