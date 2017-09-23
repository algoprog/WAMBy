package Semantic;

import Core.Wamby;
import TextModel.Paragraph;
import TextModel.Sentence;
import TextModel.Word;

import java.util.*;

/**
 * Created by Chris Samarinas
 */
public class TextSimilarity {
    private HashMap<String, Integer> IDF;
    private int doc_count = 0;

    public TextSimilarity() {
        IDF = new HashMap<>();
    }

    public void addDocuments(int n) {
        this.doc_count += n;
    }

    public void addWord(String word, int occurrences) {
        IDF.put(word, IDF.getOrDefault(word, 0)+occurrences);
    }

    public double getIDF(String word) {
        return Math.log((double)(doc_count+1) / IDF.getOrDefault(word, 1));
    }

    public double sim_tfidf(Sentence p1, Sentence p2) {
        double dot_product = 0;

        HashMap<Word, Paragraph.VocabularyEntry> v1 = p1.getVocabulary();
        HashMap<Word, Paragraph.VocabularyEntry> v2 = p2.getVocabulary();

        double square_sum_1 = 0;

        Iterator it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            Word w = (Word)pair.getKey();
            int tf1 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();

            double idf = getIDF(w.getText());

            if(v2.containsKey(w)) {
                int tf2 = v2.get(w).positions.size();

                dot_product +=  (tf1*idf) * (tf2*idf);
            }

            square_sum_1 += Math.pow(tf1*idf, 2);
        }

        double square_sum_2 = 0;
        it = v2.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word w = (Word)pair.getKey();
            int tf2 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();
            double idf = getIDF(w.getText());
            square_sum_2 += Math.pow(tf2*idf, 2);
        }

        return dot_product / Math.sqrt(square_sum_1 * square_sum_2);
    }

    public double sim_tfidf_wv(Sentence p1, Sentence p2) {
        double dot_product = 0;

        HashMap<Word, Paragraph.VocabularyEntry> v1 = p1.getVocabulary();
        HashMap<Word, Paragraph.VocabularyEntry> v2 = p2.getVocabulary();

        double idf_sum = 0;

        Iterator it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Word w = (Word)pair.getKey();

            double maxSim = 0;
            Iterator it2 = v2.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair2 = (Map.Entry)it2.next();
                Word w2 = (Word)pair2.getKey();
                double sim = cosineSimilarity(Wamby.wordVectors.getWordVector(w.getText()),Wamby.wordVectors.getWordVector(w2.getText()));
                if(sim > maxSim) {
                    maxSim = sim;
                }
            }
            double idf = getIDF(w.getLemma());
            dot_product += maxSim*idf;
            idf_sum += idf;
        }
        double sim1 = dot_product / idf_sum;

        idf_sum = 0;
        dot_product = 0;

        it = v2.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Word w = (Word)pair.getKey();

            double maxSim = 0;
            Iterator it2 = v1.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair2 = (Map.Entry)it2.next();
                Word w2 = (Word)pair2.getKey();
                double sim = cosineSimilarity(Wamby.wordVectors.getWordVector(w.getText()),Wamby.wordVectors.getWordVector(w2.getText()));
                if(sim > maxSim) {
                    maxSim = sim;
                }
            }
            double idf = getIDF(w.getLemma());
            dot_product += maxSim*idf;
            idf_sum += idf;
        }
        double sim2 = dot_product / idf_sum;

        return (sim1 + sim2)/2;
    }

    public double sim_avg_wv(Sentence p1, Sentence p2) {
        if(p1.getVector()==null || p2.getVector()==null) return 0;

        return cosineSimilarity(p1.getVector(), p2.getVector());
    }

    public double sim_tfidf_wv_2(Sentence p1, Sentence p2) {
        double SYNONYM_THRESHOLD = 0.75;

        HashMap<Word, Paragraph.VocabularyEntry> v1 = p1.getVocabulary();
        HashMap<Word, Paragraph.VocabularyEntry> v2 = p2.getVocabulary();

        HashSet<Word> words = new HashSet<>();
        Iterator it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word word = (Word)pair.getKey();
            words.add(word);
        }

        it = v2.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word word = (Word)pair.getKey();
            if(!words.contains(word)) {
                words.add(word);
            }
        }

        double dot_product = 0;
        double square_sum_a = 0;
        double square_sum_b = 0;

        it = words.iterator();
        while (it.hasNext()) {
            Word word = (Word)it.next();

            double idf = getIDF(word.getText());

            double tf1 = 0;
            if(v1.containsKey(word)) {
                tf1 = v1.get(word).positions.size();
            }
            else {
                double maxSim = 0;
                double maxTF = 0;
                Iterator it2 = v1.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry pair2 = (Map.Entry)it2.next();
                    Word w2 = (Word)pair2.getKey();
                    Sentence.VocabularyEntry ve = (Sentence.VocabularyEntry)pair2.getValue();
                    double sim = cosineSimilarity(Wamby.wordVectors.getWordVector(word.getText()),Wamby.wordVectors.getWordVector(w2.getText()));
                    if(sim > maxSim) {
                        maxSim = sim;
                        maxTF = ve.positions.size();
                    }
                }
                if(maxSim > SYNONYM_THRESHOLD) {
                    tf1 = maxSim*maxTF;
                }
            }
            square_sum_a += Math.pow(tf1*idf, 2);

            double tf2 = 0;
            if(v2.containsKey(word)) {
                tf2 = v2.get(word).positions.size();
            }
            else {
                double maxSim = 0;
                double maxTF = 0;
                Iterator it2 = v2.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry pair2 = (Map.Entry)it2.next();
                    Word w2 = (Word)pair2.getKey();
                    Sentence.VocabularyEntry ve = (Sentence.VocabularyEntry)pair2.getValue();
                    double sim = cosineSimilarity(Wamby.wordVectors.getWordVector(word.getText()),Wamby.wordVectors.getWordVector(w2.getText()));
                    if(sim > maxSim) {
                        maxSim = sim;
                        maxTF = ve.positions.size();
                    }
                }
                if(maxSim > SYNONYM_THRESHOLD) {
                    tf2 = maxSim*maxTF;
                }
            }
            square_sum_b += Math.pow(tf2*idf, 2);

            dot_product += tf1*idf*tf2*idf;
        }

        double sim = dot_product / Math.sqrt(square_sum_a * square_sum_b);
        String type = "" + sim;
        if(type.equals("NaN")) return 0;
        else return sim;
    }

    public double sim_tfidf_wv_3(Sentence p1, Sentence p2) {
        double SYNONYM_THRESHOLD = 0.75; // 0.75

        HashMap<Word, Paragraph.VocabularyEntry> v1 = p1.getVocabulary();
        HashMap<Word, Paragraph.VocabularyEntry> v2 = p2.getVocabulary();

        double dot_product_1 = 0;
        double square_sum_1a = 0;
        double square_sum_1b = 0;
        Iterator it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word w = (Word)pair.getKey();

            int tf1 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();
            double tf2 = 0;

            if(v2.containsKey(w)) {
                tf2 = v2.get(w).positions.size();
            }

            double idf = getIDF(w.getLemma());
            double idf2 = idf;

            if(tf2==0) {
                double max_sim = 0;
                int max_tf = 0;
                for(Word w2 : v2.keySet()) {
                    if(Wamby.wordVectors.hasWord(w.getText()) && Wamby.wordVectors.hasWord(w2.getText())) {
                        double[] wv1 = Wamby.wordVectors.getWordVector(w.getText());
                        double[] wv2 = Wamby.wordVectors.getWordVector(w2.getText());
                        double sim = cosineSimilarity(wv1, wv2);
                        if(sim > max_sim) {
                            max_sim = sim;
                            max_tf = v2.get(w2).positions.size();
                            idf2 = getIDF(w2.getLemma());
                        }
                    }
                }
                if (max_sim > SYNONYM_THRESHOLD) {
                    tf2 = max_sim * max_tf;
                    square_sum_1b += Math.pow(tf2*idf2, 2);
                }
            }

            dot_product_1 += (tf1*idf) * (tf2*idf2);
            square_sum_1a += Math.pow(tf1*idf, 2);
        }
        it = v2.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word w = (Word)pair.getKey();
            int tf2 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();
            double idf = getIDF(w.getLemma());
            square_sum_1b += Math.pow(tf2*idf, 2);
        }

        double dot_product_2 = 0;
        double square_sum_2a = 0;
        double square_sum_2b = 0;
        it = v2.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word w = (Word)pair.getKey();

            int tf2 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();
            double tf1 = 0;

            if(v1.containsKey(w)) {
                tf1 = v1.get(w).positions.size();
            }

            double idf = getIDF(w.getLemma());
            double idf1 = idf;

            if(tf1==0) {
                double max_sim = 0;
                int max_tf = 0;
                for(Word w2 : v1.keySet()) {
                    if(Wamby.wordVectors.hasWord(w.getText()) && Wamby.wordVectors.hasWord(w2.getText())) {
                        double[] wv1 = Wamby.wordVectors.getWordVector(w.getText());
                        double[] wv2 = Wamby.wordVectors.getWordVector(w2.getText());
                        double sim = cosineSimilarity(wv1, wv2);
                        if(sim > max_sim) {
                            max_sim = sim;
                            max_tf = v1.get(w2).positions.size();
                            idf1 = getIDF(w2.getLemma());
                        }
                    }
                }
                if (max_sim > SYNONYM_THRESHOLD) {
                    tf1 = max_sim * max_tf;
                    square_sum_2b += Math.pow(tf1*idf1, 2);
                }
            }

            dot_product_2 += (tf1*idf1) * (tf2*idf);
            square_sum_2a += Math.pow(tf2*idf, 2);
        }
        it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Word w = (Word)pair.getKey();
            int tf1 = ((Paragraph.VocabularyEntry)pair.getValue()).positions.size();
            double idf = getIDF(w.getLemma());
            square_sum_2b += Math.pow(tf1*idf, 2);
        }

        double sim = 0.5 * (dot_product_1 / Math.sqrt(square_sum_1a * square_sum_1b) + dot_product_2 / Math.sqrt(square_sum_2a * square_sum_2b));
        String type = "" + sim;
        if(type.equals("NaN")) return 0;
        else return sim;
    }

    public double sim_matrix(Sentence p1, Sentence p2) {
        double SYNONYM_THRESHOLD = 0.8;

        HashMap<Word, Paragraph.VocabularyEntry> v1 = p1.getVocabulary();
        HashMap<Word, Paragraph.VocabularyEntry> v2 = p2.getVocabulary();

        ArrayList<Word> w1 = new ArrayList<>();
        w1.addAll(v1.keySet());

        ArrayList<Word> w2 = new ArrayList<>();
        w2.addAll(v2.keySet());

        HashSet<Word> words = new HashSet<>();

        for(Word w : w1) {
            words.add(w);
        }

        for(Word w : w2) {
            if(!words.contains(w)) {
                words.add(w);
            }
        }

        ArrayList<Word> words_all = new ArrayList<>();

        HashMap<String, Integer> word_pos = new HashMap<>();

        int i = 0;
        for(Word w : words) {
            words_all.add(i,w);
            word_pos.put(w.getText(), i);
            i++;
            //System.out.println("add "+w.getText());
        }

        int n = words_all.size();

        double[][] sim_matrix = new double[n][n];
        for (int j=0; j<n; j++) {
            for (int k=j; k<n; k++) {
                double sim = wordvec_sim(words_all.get(j).getText(),words_all.get(k).getText());
                if(sim<SYNONYM_THRESHOLD) sim = 0;
                sim_matrix[j][k] = sim;
                sim_matrix[k][j] = sim;
                //System.out.println("SIM["+j+","+k+"] = "+sim);
                //System.out.println("SIM["+k+","+j+"] = "+sim);
            }
        }

        double[] t1 = new double[n];
        for(Word w : w1) {
            int pos = word_pos.get(w.getText());
            int tf = v1.get(w).positions.size();
            double idf = getIDF(w.getText());
            t1[pos] = tf * idf;
            //System.out.println("a["+pos+"] = "+(tf*idf));
        }

        double d2 = 0;
        double[] t2 = new double[n];
        for(Word w : w2) {
            int pos = word_pos.get(w.getText());
            int tf = v2.get(w).positions.size();
            double idf = getIDF(w.getText());
            t2[pos] = tf * idf;
            //System.out.println("b["+pos+"] = "+(tf*idf));
            d2 += Math.pow(tf*idf,2);
        }

        double d1 = 0;
        double sim = 0;
        for(Word b : w2) {
            int b_pos = word_pos.get(b.getText());
            double aW = 0;
            for(Word a : w1) {
                int a_pos = word_pos.get(a.getText());
                aW += t1[a_pos]*sim_matrix[a_pos][b_pos];
            }
            //System.out.println("aW["+b_pos+"] = "+aW);
            d1 += Math.pow(aW, 2);
            sim += aW*t2[b_pos];
        }

        if(d1==0||d2==0) return 0;

        return sim / (Math.sqrt(d1)*Math.sqrt(d2));
    }

    public static double wordvec_sim(String w1, String w2) {
        double[] wv1 = Wamby.wordVectors.getWordVector(w1.toLowerCase());
        double[] wv2 = Wamby.wordVectors.getWordVector(w2.toLowerCase());
        return cosineSimilarity(wv1, wv2);
    }

    public static double cosineSimilarity(double[] a, double[] b) {
        if(a==null || b==null) return 0;

        double square_sum_a = 0;
        for(int i = 0; i < a.length; i++) {
            square_sum_a += Math.pow(a[i], 2);
        }
        double square_sum_b = 0;
        for(int i = 0; i < b.length; i++) {
            square_sum_b += Math.pow(b[i], 2);
        }
        double dot_product = 0;
        for(int i = 0; i < a.length; i++) {
            dot_product += a[i]*b[i];
        }

        return 0.5*(dot_product / (Math.sqrt(square_sum_a) * Math.sqrt(square_sum_b)))+0.5;
    }

    public double common_word_order(Sentence p1, Sentence p2) {
        LinkedHashMap<Word, Sentence.VocabularyEntry> v1 = p1.getVocabulary();
        LinkedHashMap<Word, Sentence.VocabularyEntry> v2 = p2.getVocabulary();

        class WordPosition implements Comparable<WordPosition> {
            public String word;
            public int position;
            public WordPosition(String word, int position) {
                this.word = word;
                this.position = position;
            }

            @Override
            public int compareTo(WordPosition o) {
                if (this.position > o.position) return 1;
                else if(this.position < o.position) return -1;
                else return 0;
            }
        }

        ArrayList<WordPosition> p = new ArrayList<>();
        ArrayList<WordPosition> r = new ArrayList<>();

        Iterator it = v1.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            Word w = (Word)pair.getKey();
            Sentence.VocabularyEntry w1 = (Sentence.VocabularyEntry)pair.getValue();

            if(v2.containsKey(w)) {
                Sentence.VocabularyEntry w2 = v2.get(w);
                int min_dist = Integer.MAX_VALUE;
                int min_pos1 = 0;
                int min_pos2 = 0;
                for(Integer pos1 : w1.positions) {
                    for(Integer pos2 : w2.positions) {
                        int dist = Math.abs(pos1-pos2);
                        if(dist < min_dist) {
                            min_dist = dist;
                            min_pos1 = pos1;
                            min_pos2 = pos2;
                        }
                    }
                }
                p.add(new WordPosition(w.getLemma(),min_pos1));
                r.add(new WordPosition(w.getLemma(),min_pos2));
            }
        }

        Collections.sort(p);
        Collections.sort(r);

        HashMap<String, Integer> p_final = new HashMap<>();
        int i = 1;
        for(WordPosition pos : p) {
            p_final.put(pos.word, i++);
        }

        i = 1;
        int sum = 0;
        for(WordPosition pos : r) {
            sum += Math.abs(i++ - p_final.get(pos.word));
        }

        int delta = p.size();

        if(delta==0) return 0;
        else if(delta % 2 == 0) return 1 - 2 * sum / Math.pow(delta, 2);
        else if(delta > 1) return 1 - 2 * sum / (Math.pow(delta, 2) - 1);
        else return 1;
    }

    public double combined_sim(Sentence p1, Sentence p2) {
        return sim_tfidf_wv_3(p1, p2);
    }

    public double title_sim(Sentence p1, Sentence p2) {
        return 0.8* sim_tfidf_wv_3(p1, p2) + 0.2*common_word_order(p1, p2);
    }

    public double ngrams_sim(Sentence ps, Sentence pl) {
        double[] similarities = new double[3];

        int s_length = ps.getWordsCount();
        int l_length = pl.getWordsCount();

        if(s_length<4 || l_length<4) return 0;

        String pl_text = pl.getLemmatizedText().toLowerCase().replaceAll("[^\\w\\s]","");

        ArrayList<Word> v = new ArrayList<>();
        v.addAll(ps.getVocabulary().keySet());

        for(int n=2; n<=4; n++) {
            for(int i=n-1;i<s_length;i++) {
                String ngram = "";
                for(int j=i-n+1; j<=i; j++) {
                    ngram += " " + v.get(j).getLemma();
                }
                ngram = ngram.trim().toLowerCase();
                if(pl_text.contains(ngram)) {
                    similarities[n-2]++;
                }
            }
            similarities[n-2] = similarities[n-2]/(s_length-n+1);
        }

        return (similarities[0] + 2*similarities[1] + 4*similarities[2])/7;
    }
}
