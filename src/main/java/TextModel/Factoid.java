package TextModel;

import java.util.ArrayList;

/**
 * Created by Chris Samarinas
 */
public class Factoid implements Comparable<Factoid>{
    private String text;
    private ArrayList<Word> left_window;
    private ArrayList<Word> right_window;
    private double score;
    private int web_position;
    private String web_title;
    private int occurrences = 0;

    public Factoid(String text, ArrayList<Word> left_window, ArrayList<Word> right_window, int web_position, String web_title) {
        this.text = text;
        this.left_window = left_window;
        this.right_window = right_window;
        this.web_position = web_position;
        this.web_title = web_title;
    }

    public String getText() { return text; }
    public double getScore() { return score; }
    public String getWebTitle() { return web_title; }
    public int getWebPosition() { return web_position; }
    public ArrayList<Word> getLeftWindow() { return left_window; }
    public ArrayList<Word> getRightWindow() { return right_window; }
    public void setScore(double score) { this.score = score; }

    public String getContext() {
        String context = "";
        for(Word w : left_window) {
            if(w.getText().equals(".")) break;
            String text = w.getOriginalText();
            if(text.equals("LRB")) text = "(";
            else if(text.equals("RRB")) text = ")";
            context = text + " " + context;
        }
        context += " " + text;
        for(Word w : right_window) {
            String text = w.getOriginalText();
            if(text.equals("LRB")) text = "(";
            else if(text.equals("RRB")) text = ")";
            context = context + " " + text;
            if(w.getText().equals(".")) break;
        }
        return context.trim().replaceAll(" +", " ");
    }

    @Override
    public int compareTo(Factoid o) {
        if (this.getScore() > o.getScore()) return -1;
        else if(this.getScore() < o.getScore()) return 1;
        else return 0;
    }
}
