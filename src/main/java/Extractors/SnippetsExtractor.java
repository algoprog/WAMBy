package Extractors;

import Core.Config;
import Core.Wamby;
import TextModel.Paragraph;
import TextModel.Sentence;

import java.util.ArrayList;

/**
 * Created by Chris Samarinas
 */
public class SnippetsExtractor {
    public static ArrayList<Paragraph> getSnippets(ArrayList<Paragraph> paragraphs, String query_txt) {
        Paragraph query = new Paragraph(0,0,query_txt,"");
        int id = 0;
        ArrayList<Paragraph> snippets = new ArrayList<>();
        for (Paragraph p : paragraphs) {
            if(p.getListSize()>0) {
                if(p.getListSize()<=Config.MAX_LIST_SNIPPET_SIZE) {
                    snippets.add(p);
                }
                continue;
            }
            ArrayList<Sentence> sentences = p.getSentences();
            ArrayList<Sentence> selected_sentences = new ArrayList<>();
            for(Sentence s : sentences) {
                if(Wamby.textSimilarity.combined_sim(query, s) > Config.SENTENCE_SIMILARITY_THRESHOLD) {
                    selected_sentences.add(s);
                }
            }
            for(int i=0;i<selected_sentences.size()-1;i++) {
                Sentence s1 = selected_sentences.get(i);
                Sentence s2 = selected_sentences.get(i+1);
                double dist = 0;
                for(int j=s1.getId()+1; j<=s2.getId()-1;j++) {
                    Sentence sb = sentences.get(j);
                    dist += sb.getWordsCount() / (Math.max(Wamby.textSimilarity.combined_sim(s1, sb), Wamby.textSimilarity.combined_sim(s2, sb)) + 1);
                }
                if(dist < Config.SENTENCE_CLUSTERING_THRESHOLD) {
                    sentences.get(s2.getId()).setId(s1.getId());
                    for(int j=s1.getId()+1; j<=s2.getId()-1;j++) {
                        Sentence sb = sentences.get(j);
                        sb.setId(s1.getId());
                    }
                }
            }
            int prev_id = -1;
            String snippet = "";
            for(int i=0;i<sentences.size();i++) {
                Sentence s = sentences.get(i);
                if(s.getId()==prev_id) {
                    if(snippets.size()>0) {
                        snippets.remove(snippets.size()-1);
                        id--;
                    }
                    snippet += " " + s.getText();
                }
                else {
                    snippet = s.getText();
                }
                Paragraph sp = new Paragraph(id, p.getWebPosition(), snippet, p.getTitle());
                if(sp.getWordsCount()>Config.MIN_SNIPPET_SIZE) {
                    snippets.add(sp);
                }
                id++;
                prev_id = s.getId();
            }
        }

        return snippets;
    }
}
