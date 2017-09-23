package Semantic;

import TextModel.Paragraph;
import TextModel.Word;
import TextModel.WordsUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Chris Samarinas
 */
public class SimTest {

    public static void startTest() throws IOException {
        String train_corpus = "C:/Users/Chris/Desktop/Wamby/data/msr_paraphrase/train.txt";
        String test_corpus = "C:/Users/Chris/Desktop/Wamby/data/msr_paraphrase/test.txt";

        double SIMILARITY_THRESHOLD = 0.83; // 0.55 0.9 0.0003 0.81 0.66 0.83

        TextSimilarity ts = new TextSimilarity();
        ts.addDocuments(11602);

        int limit = 0;

        System.out.println("Calculating IDFs...");
        BufferedReader in = new BufferedReader(new FileReader(test_corpus));
        String line;
        while((line = in.readLine()) != null && limit < 20000)
        {
            String[] parts = line.split("\\t");
            String s1 = parts[3];
            String s2 = parts[4];

            ArrayList<Word> words = WordsUtil.getWords(s1);
            for(Word w : words) {
                ts.addWord(w.getText(),1);
            }

            words = WordsUtil.getWords(s2);
            for(Word w : words) {
                ts.addWord(w.getText(),1);
            }

            if(limit % 100 == 0) {
                System.out.println(limit+100);
            }

            limit++;
        }
        in.close();

        System.out.println("Calculating similarities...");
        limit = 0;
        int correct = 0;
        int predicted_positives = 0;
        int all_positives = 0;
        int true_positives = 0;
        in = new BufferedReader(new FileReader(test_corpus));
        double sum = 0;
        while((line = in.readLine()) != null && limit < 20000)
        {
            String[] parts = line.split("\\t");
            String relevant = parts[0];
            String s1 = parts[3];
            String s2 = parts[4];

            Paragraph p1 = new Paragraph(0,0,s1,"");
            Paragraph p2 = new Paragraph(0,0,s2,"");

            double similarity = ts.sim_matrix(p1, p2);

            System.out.println(similarity);

            String found_relevant = "0";
            if(similarity > SIMILARITY_THRESHOLD) {
                found_relevant = "1";
                predicted_positives++;
            }

            if(relevant.equals(found_relevant)) {
                correct++;
                if(relevant.equals("1")) {
                    true_positives++;
                }
            }

            if(relevant.equals("1")) {
                all_positives++;
                sum += similarity;
            }

            if(limit % 100 == 0) {
                System.out.println(limit+100);
            }

            limit++;
        }
        in.close();

        double accuracy = (double) correct / limit;
        double precision = (double) true_positives / predicted_positives;
        double recall = (double) true_positives / all_positives;
        double f1 = 2 * (precision * recall) / (precision + recall);

        System.out.println("Accuracy: "+accuracy);
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        System.out.println("F1: "+f1);
        System.out.println((sum/all_positives));
    }
}
