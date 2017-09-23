package Rankers;

import Core.Config;
import Core.Wamby;
import TextModel.Paragraph;
import TextModel.Sentence;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Chris Samarinas
 */
public class SnippetRanker {
    
    public static void rankAnswers(ArrayList<Paragraph> paragraphs, String query_txt){

        Paragraph query = new Paragraph(0, 0, query_txt, "");

        double max_information_score = 0;

        ArrayList<Double> information_scores = new ArrayList<>();
        ArrayList<Double> max_sims = new ArrayList<>();

        for(Paragraph p : paragraphs){
            double information_score = 0;
            double max_sim = 0;
            for(Sentence s : p.getSentences()) {
                double sim = Wamby.textSimilarity.combined_sim(query, s);
                if(sim > max_sim) {
                    max_sim = sim;
                }
                information_score += sim;
            }
            max_sims.add(max_sim);

            if(information_score>max_information_score) {
                max_information_score = information_score;
            }

            information_scores.add(information_score);
        }

        int i = 0;
        for (Paragraph p : paragraphs) {
            double title_sim = Wamby.textSimilarity.title_sim(query, new Paragraph(0,0,p.getTitle(),""));
            double web_position_score = (1-(p.getWebPosition()-1)/Config.WEB_RESULTS_TOTAL);
            double ngrams_score = Wamby.textSimilarity.ngrams_sim(query, p);
            double score = 0.25*information_scores.get(i)/max_information_score + 0.05*ngrams_score  + 0.2*max_sims.get(i) + 0.3*title_sim + 0.2*web_position_score;
            p.setScore(score);
            i++;
        }
        
        Collections.sort(paragraphs);
    }
}
