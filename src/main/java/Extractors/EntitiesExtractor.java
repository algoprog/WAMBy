package Extractors;

import Core.Config;
import TextModel.Factoid;
import TextModel.Word;
import TextModel.WordsUtil;

import java.util.ArrayList;

/**
 * Created by Chris Samarinas
 */
public class EntitiesExtractor {

    public static ArrayList<Factoid> extractEntities(String text, int web_position, String type, String title) {
        ArrayList<Word> words = WordsUtil.getWords(text);

        ArrayList<Factoid> factoids = new ArrayList<>();

        int window_size = Config.FACTOID_WINDOW_SIZE;

        int i = 0;
        String prev = "";
        int wcount = 0;
        for(Word w : words) {
            if(w.getNE().equals(type)) {
                ArrayList<Word> left_window = new ArrayList<>();
                ArrayList<Word> right_window = new ArrayList<>();

                int left_limit = Math.max(0, i-window_size/2);
                for(int j=i-1;j>=left_limit;j--) {
                    left_window.add(words.get(j));
                }

                int right_limit = Math.min(words.size()-1, i+window_size/2);
                for(int j=i+1;j<=right_limit;j++) {
                    right_window.add(words.get(j));
                }

                String fact = w.getOriginalText();

                if(!prev.equals("")) {
                    factoids.remove(factoids.size()-1);
                    for(int k=0;k<wcount;k++) {
                        left_window.remove(0);
                    }
                    fact = prev + " " + fact;
                    wcount++;
                }else{
                    wcount = 1;
                }

                if(fact.length()>1) {
                    factoids.add(new Factoid(fact, left_window, right_window, web_position, title));
                }
                prev = fact;
                //System.out.println(fact);
            }else {
                prev = "";
                wcount = 0;
            }
            i++;
        }

        return factoids;
    }
}
