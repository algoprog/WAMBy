package TextModel;

import Core.Wamby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 *
 * @author Chris Samarinas
 */
public class Paragraph extends Sentence implements Comparable<Paragraph> {
    private int web_position;
    private String title;
    private double score;
    private int listSize = 0;
    private ArrayList<Sentence> sentences;
    
    public Paragraph(int id, int web_position, String text, String title){
        vocabulary = new LinkedHashMap<>();
        this.text = text.trim().replaceAll("\\s+", " ");
        sentences = WordsUtil.getSentences(text);
        ArrayList<Word> words = new ArrayList<>();

        for(Sentence s : sentences) {
            HashSet<String> words_added = new HashSet<>();
            for(Word w : s.getVocabulary().keySet()) {
                words.add(w);
                if(!words_added.contains(w.getText())) {
                    Wamby.textSimilarity.addWord(w.getText(),1);
                    words_added.add(w.getText());
                }
            }
        }
        if(web_position>0) Wamby.textSimilarity.addDocuments(sentences.size());
        addWords(words);
        this.id = id;
        this.title = title;
        this.web_position = web_position;
    }

    public String getTitle() { return title; }

    @Override
    public String getText(){
        String txt = "";
        for(Sentence s : sentences) {
            txt += s.getText() + " ";
        }
        return txt;
    }

    @Override
    public String getLemmatizedText(){
        String txt = "";
        for(Sentence s : sentences) {
            txt += s.getLemmatizedText() + " ";
        }
        return txt;
    }

    public int getWebPosition() { return web_position; }
    
    public void setListSize(int k){
        this.listSize = k;
    }
    
    public int getListSize() { return this.listSize; }
    
    public void setScore(double score){ this.score = score; }
    
    public double getScore(){
        return score;
    }

    public ArrayList<Sentence> getSentences() { return this.sentences; }

    @Override
    public int compareTo(Paragraph o) {
        if (this.getScore() > o.getScore()) return -1;
        else if(this.getScore() < o.getScore()) return 1;
        else return 0;
    }
}
