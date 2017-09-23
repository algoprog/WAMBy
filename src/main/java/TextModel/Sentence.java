package TextModel;

import Core.Wamby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Chris Samarinas
 */
public class Sentence {
    protected int id;
    protected String text;
    protected int words_count;
    protected int stopwords_count;
    protected LinkedHashMap<Word, VocabularyEntry> vocabulary;
    protected double[] avg_word_vector;

    public Sentence() {}

    public Sentence(int id, String text, ArrayList<Word> words){
        this.id = id;
        vocabulary = new LinkedHashMap<>();
        this.text = text.trim().replaceAll("\\s+", " ");
        addWords(words);
    }

    protected void addWords(ArrayList<Word> words) {
        int position = 0;
        //int vectors_count = 0;
        for (Word word : words) {
            if (vocabulary.containsKey(word)) {
                vocabulary.get(word).positions.add(position);
                /*
                if (avg_word_vector != null) {
                    if (Wamby.wordVectors.hasWord(word.getText())) {
                        double[] v = Wamby.wordVectors.getWordVector(word.getText());
                        for (int i = 0; i < v.length; i++) {
                            avg_word_vector[i] += v[i];
                        }
                        vectors_count++;
                    }
                } else {
                    avg_word_vector = Wamby.wordVectors.getWordVector(word.getText());
                    if (avg_word_vector != null) {
                        vectors_count++;
                    }
                }
                */
            } else {
                ArrayList<Integer> l = new ArrayList<>();
                l.add(position);
                VocabularyEntry w = new VocabularyEntry();
                w.positions = l;
                w.word_text = word.getText();
                vocabulary.put(word, w);
                /*
                if (Wamby.wordVectors.hasWord(word.getText())) {
                    avg_word_vector = Wamby.wordVectors.getWordVector(word.getText());
                    vectors_count++;
                }
                */
                if (WordsUtil.isStopword(word.getText())) {
                    stopwords_count++;
                }
                position++;
            }
            this.words_count = words.size();
            /*
            if (avg_word_vector != null) {
                for (int i = 0; i < avg_word_vector.length; i++) {
                    avg_word_vector[i] /= vectors_count;
                }
            }
            */
        }
    }

    public int getWordsCount(){
        return words_count;
    }

    public double[] getVector() { return this.avg_word_vector; }

    public int getWordOccurrences(Word word){
        if(vocabulary.containsKey(word)){
            return vocabulary.get(word).positions.size();
        }
        return 0;
    }

    public LinkedHashMap<Word, VocabularyEntry> getVocabulary() { return vocabulary; }

    public String getText(){
        return text;
    }

    public String getLemmatizedText() {
        String txt = "";
        for(Word w : vocabulary.keySet()) {
            txt += " " + w.getLemma();
        }
        return txt.trim();
    }

    public int getId() { return this.id; }

    public void setId(int id) { this.id = id; }

    public class VocabularyEntry {
        public ArrayList<Integer> positions;
        public String word_text;
    }
}
