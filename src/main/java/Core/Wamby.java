package Core;

import Extractors.EntitiesExtractor;
import Extractors.SnippetsExtractor;
import QClassifier.QClassifier;
import Rankers.FactoidRanker;
import Rankers.SnippetRanker;
import Semantic.TextSimilarity;
import TextModel.Factoid;
import TextModel.Paragraph;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.*;

/**
 *
 * @author Chris Samarinas
 */
public class Wamby {

    public static StanfordCoreNLP stanford_nlp;
    public static WordVectors wordVectors;
    public static TextSimilarity textSimilarity;

    public static void main(String[] args) throws IOException, InterruptedException {

        init();

        Scanner s = new Scanner(System.in);
        System.out.print("[Question]: ");
        String query = s.nextLine();

        while(!query.equals("exit")){

            ArrayList<String> answers = answer(query);

            System.out.println("\n[Answer]: "+answers.get(0));
            System.out.print("\n[Question]: ");
            query = s.nextLine();
        }
    }

    public static ArrayList<String> answer(String query) throws IOException {
        int topk = 20;

        ArrayList<String> final_answers = new ArrayList<>();

        textSimilarity = new TextSimilarity();

        String qtype = QClassifier.classify(query);
        query = query.toLowerCase();

        System.out.println("Question: "+query);
        System.out.println("Question type: "+qtype);
        //System.out.println("Features: "+ FeaturesExtractor.getFeatures(query).getText());

        /*
        if(!qtype.equals("DESCRIPTION")) {
            query = '"' + query + '"';
        }
        */

        WebSearch.WebResult[] results = WebSearch.WebSearch.search(query);
        String answer = "";

        ArrayList<Paragraph> paragraphs = new ArrayList<>();
        ArrayList<Paragraph> answers = new ArrayList<>();
        ArrayList<Factoid> factoids = new ArrayList<>();
        ArrayList<Factoid> snippets_factoids = new ArrayList<>();
        ArrayList<Factoid> titles_factoids = new ArrayList<>();
        int i = 1;

        for(WebSearch.WebResult result : results){

            System.out.println(result.getUrl());

            System.out.println("Analyzing web page "+i+"...");

            try {
                Document doc = Jsoup.connect(result.getUrl()).userAgent(Core.Config.USER_AGENT).timeout(30000).get();

                if(qtype.equals("DESCRIPTION")) {
                    Extractors.CommentsExtractor c = new Extractors.CommentsExtractor(result.getUrl(), doc);
                    ArrayList<String> comments = c.getComments();

                    if(comments.size()>0) {
                        System.out.println("Found " + comments.size() + " comments");
                        int id = 0;
                        String title = result.getTitle();
                        for(String comment : comments) {
                            Paragraph p = new Paragraph(id,i,comment,title);
                            answers.add(p);
                            id++;
                        }
                    }else {
                        Extractors.TextExtractor t = new Extractors.TextExtractor(doc, i);
                        ArrayList<Paragraph> snippets = SnippetsExtractor.getSnippets(t.getParagraphs(), query);
                        paragraphs.addAll(snippets);
                        System.out.println("Found "+snippets.size()+" snippets");
                    }
                }
                else {
                    Extractors.TextExtractor t = new Extractors.TextExtractor(doc, i);
                    System.out.println("Text extracted");
                    if(!qtype.equals("DESCRIPTION")) {
                        ArrayList<Factoid> new_factoids = EntitiesExtractor.extractEntities(t.getText(), i, qtype, result.getTitle());
                        ArrayList<Factoid> new_title_factoids = EntitiesExtractor.extractEntities(result.getTitle(), i, qtype, "");
                        factoids.addAll(new_factoids);
                        titles_factoids.addAll(new_title_factoids);

                        System.out.println("Found "+(new_factoids.size())+" factoids");

                        ArrayList<Factoid> new_snippets_factoids = EntitiesExtractor.extractEntities(result.getSnippet(), i, qtype, "");
                        snippets_factoids.addAll(new_snippets_factoids);
                    }
                }
            }catch (Exception ignored) {}

            i++;
        }

        if(!paragraphs.isEmpty() || !factoids.isEmpty() || !answers.isEmpty()){
            if(qtype.equals("DESCRIPTION")) {
                if(!answers.isEmpty()) {
                    SnippetRanker.rankAnswers(answers, query);

                    i = 0;
                    for(Paragraph p : answers){
                        final_answers.add(p.getText());
                        System.out.println("\n["+i+"] "+p.getText()+"\nScore: "+p.getScore());
                        System.out.println("Source: "+results[p.getWebPosition()-1].getUrl());
                        i++;
                        if(i==topk) break;
                    }

                    answer = answers.get(0).getText();
                }
                else {
                    SnippetRanker.rankAnswers(paragraphs, query);

                    i = 0;
                    for(Paragraph p : paragraphs){
                        final_answers.add(p.getText());
                        System.out.println("\n["+i+"] "+p.getText()+"\nScore: "+p.getScore());
                        System.out.println("Source: "+results[p.getWebPosition()-1].getUrl());
                        i++;
                        if(i==topk) break;
                    }

                    answer = paragraphs.get(0).getText();
                }
            }
            else if(factoids.size()>0){
                System.out.println("Analyzing factoids...");
                factoids = FactoidRanker.rankFactoids(factoids, query, false, false, qtype);

                HashMap<String, Double> snippets_scores = new HashMap<>();
                HashMap<String, Double> titles_scores = new HashMap<>();

                System.out.println("Analyzing factoids from snippets...");
                snippets_factoids = FactoidRanker.rankFactoids(snippets_factoids, query, false, true, qtype);
                System.out.println("Analyzing factoids from titles...");
                titles_factoids = FactoidRanker.rankFactoids(titles_factoids, query, false, true, qtype);

                for(Factoid sf : snippets_factoids){
                    snippets_scores.put(sf.getText(), sf.getScore());
                    //System.out.println("SF "+sf.getText()+" : "+sf.getScore());
                }

                for(Factoid sf : titles_factoids){
                    titles_scores.put(sf.getText(), sf.getScore());
                    //System.out.println("SF "+sf.getText()+" : "+sf.getScore());
                }

                Paragraph qp = new Paragraph(0,0,query,"");

                for(Factoid f : factoids) {
                    double extra_score = 0;
                    if(snippets_scores.containsKey(f.getText())){
                        extra_score += 0.5*snippets_scores.get(f.getText());
                    }
                    if(titles_scores.containsKey(f.getText())){
                        extra_score += 0.5*titles_scores.get(f.getText());
                    }

                    String[] fw = f.getText().split(" ");
                    double information_content = 0;
                    for(String fword : fw) {
                        information_content += 1-FactoidRanker.maxSim(fword,qp);
                    }
                    information_content /= fw.length;
                    if(information_content<0.05) {
                        information_content = 0;
                    }else {
                        information_content = 1;
                    }

                    //System.out.println(f.getText()+" : "+f.getScore());

                    f.setScore(information_content*f.getScore()*(1+extra_score)/2);

                    //System.out.println(f.getText()+" : "+f.getScore()+" : "+extra_score);
                }

                Collections.sort(factoids);

                int j = 0;
                for (Factoid f : factoids) {
                    final_answers.add(f.getText());
                    System.out.println("\n["+(j+1)+"] "+f.getText()+"\nScore: "+f.getScore()+"\nContext: "+f.getContext()+"\nSource: "+results[f.getWebPosition()-1].getUrl());
                    j++;
                    if(j==topk) break;
                }

                if(qtype.equals("PERSON")||qtype.equals("LOCATION")||qtype.equals("ORGANIZATION")) {
                    answer = factoids.get(0).getText();
                }
                else {
                    answer = factoids.get(0).getText() + " -- " +factoids.get(0).getContext();
                }
            }
        }

        System.gc();

        System.out.println(answer);

        return final_answers;
    }

    private static void init() throws IOException, InterruptedException{
        
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER
        System.out.println("Loading StanfordCoreNLP...");
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.put("ner.applyNumericClassifiers", true);
        props.put("tokenize.options", "normalizeCurrency=false");
        //props.put("ner.model","edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");
        stanford_nlp = new StanfordCoreNLP(props);

        wordVectors = WordVectorSerializer.loadTxtVectors(new File(Config.WORD_VECTORS_DIR));

        QClassifier.init();

        //CudaEnvironment.getInstance().getConfiguration().allowMultiGPU(true);
        /*CudaEnvironment.getInstance().getConfiguration()
                .setMaximumDeviceCacheableLength(1024 * 1024 * 1024L)
                .setMaximumDeviceCache(6L * 1024 * 1024 * 1024L)
                .setMaximumHostCacheableLength(1024 * 1024 * 1024L)
                .setMaximumHostCache(6L * 1024 * 1024 * 1024L);
                */
    }

    private static void test() throws IOException {
        String test_file = "C:/Users/Chris/Desktop/Wamby/data/test/questions2.txt";
        String out_file = "C:/Users/Chris/Desktop/Wamby/data/test/results2.txt";
        String out_file_full = "C:/Users/Chris/Desktop/Wamby/data/test/results_full2.txt";

        PrintWriter writer = new PrintWriter(out_file, "UTF-8");
        PrintWriter writer_all = new PrintWriter(out_file_full, "UTF-8");

        BufferedReader in = new BufferedReader(new FileReader(test_file));
        String line;
        while((line = in.readLine()) != null)
        {

            String[] parts = line.split("\\|");
            String question = parts[0];
            String real_answers = parts[1].toLowerCase();

            ArrayList<String> answers = answer(question);
            int pos = 0;
            while (pos < answers.size()) {
                if(real_answers.contains(answers.get(pos).toLowerCase())) break;
                pos++;
            }

            writer.println((pos+1));
            writer_all.println("Question: "+question);
            for(String answer : answers) {
                writer_all.println(answer);
            }
        }

        writer.close();
        writer_all.close();
    }

    private static void mrr_calc() throws IOException {
        String test_file = "C:/Users/Chris/Desktop/Wamby/data/test/4.txt";

        BufferedReader in = new BufferedReader(new FileReader(test_file));
        String line;
        double mrr = 0;
        int cnt = 0;
        while((line = in.readLine()) != null)
        {
            int score = Integer.parseInt(line);
            if(score>0) mrr += (double)1/score;
            cnt++;
        }
        mrr /= cnt;
        System.out.println(mrr);
    }
    
}
