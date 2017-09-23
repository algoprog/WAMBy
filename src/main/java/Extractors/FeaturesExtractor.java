package Extractors;

import TextModel.Paragraph;
import TextModel.Word;
import TextModel.WordsUtil;

import java.util.ArrayList;

/**
 * Created by Chris Samarinas
 */
public class FeaturesExtractor {
    public static Paragraph getFeatures(String query) {
        String features = "";
        String query_l = query.toLowerCase();

        if(query_l.contains("how old")||query_l.contains("age")) {
            features += " age year born";
        }
        if(query_l.contains("how tall")) {
            features += " height m metre ft foot in inch";
        }
        if(query_l.contains("how far")) {
            features += " mile km kilometre";
        }
        if(query_l.matches("(.*)(cost|price)(.*)")) {
            features += " $ dollar usd € euro eur ¥ yen £ pound gbp";
        }
        if(query_l.matches("(.*)(weigh|heavy)(.*)")) {
            features += " kg kilogram gram tn ton pound lbs grain dram";
        }

        ArrayList<Word> words = WordsUtil.getWords(query);
        for(Word w : words) {
            if(w.getPOS().startsWith("VB")||
                    w.getPOS().startsWith("NN")||
                    w.getPOS().startsWith("JJ")||
                    w.getPOS().equals("CD")) {
                features += " " + w.getText();
            }
        }

        features = features.trim();

        return new Paragraph(0, 0, features, "");
    }
}
