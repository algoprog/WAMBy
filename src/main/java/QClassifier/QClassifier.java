package QClassifier;

import Core.Config;
import Core.Wamby;
import TextModel.Word;
import TextModel.WordsUtil;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Chris Samarinas
 */
public class QClassifier {

    private static MultiLayerNetwork net_trec;
    private static MultiLayerNetwork net_squad;

    public static void init() throws IOException {
        net_squad = ModelSerializer.restoreMultiLayerNetwork(new File(Config.MODEL_SQUAD_TYPES));
        net_squad.init();
        net_trec = ModelSerializer.restoreMultiLayerNetwork(new File(Config.MODEL_TREC_TYPES));
        net_trec.init();
    }
    public static String classify(String question) {
        String question_l = question.toLowerCase();
        ArrayList<Word> words = WordsUtil.getWords(question);

        if(question_l.length()<2) {
            return "DESCRIPTION";
        }
        else if(question_l.length()>2 && question_l.startsWith("who") && words.get(1).getLemma().equals("be") && words.get(2).getNE().equals("PERSON")) {
            return "DESCRIPTION";
        }
        else if(question_l.length()>2 && question_l.startsWith("what") && words.get(1).getPOS().startsWith("VB") && words.get(2).getPOS().startsWith("NN")) {
            return "DESCRIPTION";
        }
        else if(question_l.startsWith("who")) {
            return "PERSON";
        }
        else if(question_l.startsWith("where")) {
            return "LOCATION";
        }
        else if(question_l.startsWith("when")) {
            return "DATE";
        }
        else if(question_l.startsWith("why")) {
            return "DESCRIPTION";
        }
        else if(question_l.startsWith("how to")) {
            return "DESCRIPTION";
        }
        else if(words.get(0).getLemma().equals("be")) {
            return "DESCRIPTION";
        }
        else if(words.get(0).getPOS().equals("MD")) {
            return "DESCRIPTION";
        }
        else if(question_l.startsWith("how") && (words.get(1).getPOS().equals("MD")||words.get(1).getPOS().startsWith("VB"))) {
            return "DESCRIPTION";
        }

        List<String> tokens = new ArrayList<>();
        for (Word w : words) {
            tokens.add(w.getLemma());
        }
        List<String> tokensFiltered = new ArrayList<>();
        for(String t : tokens ){
            if(Wamby.wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
        int maxLength = 20;
        int vectorSize = 50;
        INDArray features = Nd4j.create(1, vectorSize, maxLength);
        INDArray featuresMask = Nd4j.zeros(1, maxLength);
        INDArray labelsMask = Nd4j.zeros(1, maxLength);
        for(int j=0; j<tokensFiltered.size() && j<maxLength; j++){
            String token = tokensFiltered.get(j);
            INDArray vector = Wamby.wordVectors.getWordVectorMatrix(token);
            features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);
            featuresMask.putScalar(new int[]{0, j}, 1.0);  // word is present -> 1.0 in features mask
        }
        int lastIdx = Math.min(tokens.size(),maxLength);
        labelsMask.putScalar(new int[]{0,lastIdx-1},1.0);

        int labels = 11;
        INDArray predicted;
        INDArray probs;
        int max_label;
        double max_prob;

        // SQUAD_NET
        //System.out.println("SQUAD_NET");
        predicted = net_squad.output(features,false,featuresMask,labelsMask);
        probs = predicted.getRow(0).getColumn(Math.min(tokens.size(), maxLength)-1);
        max_label = 0;
        max_prob = -1;
        ArrayList<Double> p = new ArrayList<>();
        ArrayList<Double> p1 = new ArrayList<>();
        for(int i=0;i<labels;i++) {
            double prob = probs.getDouble(i);
            p.add(prob);
            p1.add(prob);
            if(prob > max_prob) {
                max_prob = prob;
                max_label = i;
            }
            //System.out.println(DataIterator.getLabels_2().get(i)+":"+prob);
        }
        Collections.sort(p, Collections.<Double>reverseOrder());
        if(max_prob > p.get(1)+0.2 && !question_l.startsWith("what is")) {
            return DataIterator.getLabels_2().get(max_label);
        }

        // TREC_NET
        //System.out.println("TREC_NET");
        predicted = net_trec.output(features,false,featuresMask,labelsMask);
        probs = predicted.getRow(0).getColumn(Math.min(tokens.size(), maxLength)-1);
        max_label = 0;
        max_prob = -1;
        p = new ArrayList<>();
        ArrayList<Double> p2 = new ArrayList<>();
        for(int i=0;i<labels;i++) {
            double prob = probs.getDouble(i);
            p.add(prob);
            p2.add((prob+p1.get(i))/2);
            if(prob > max_prob) {
                max_prob = prob;
                max_label = i;
            }
            //System.out.println(DataIterator.intToLabel(i)+":"+prob);
        }
        Collections.sort(p, Collections.<Double>reverseOrder());
        if(max_prob > p.get(1)+0.2) {
            String label = DataIterator.intToLabel(max_label);
            if(label.equals("ENTITY")) {
                return "DESCRIPTION";
            }else{
                return label;
            }
        }

        max_label = 0;
        max_prob = -1;
        for(int i=0;i<labels;i++) {
            double prob = p2.get(i);
            if(prob > max_prob) {
                max_prob = prob;
                max_label = i;
            }
        }
        //System.out.println(max_prob);
        if(max_prob > 0.35 && max_label < labels-2) {
            //System.out.println("COMBINED");
            return DataIterator.intToLabel(max_label);
        }

        return "DESCRIPTION";
    }

    public static void test() throws IOException {
        int correct = 0;
        int all = 0;

        BufferedReader in = new BufferedReader(new FileReader("C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_types_test_2.txt"));
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split("\\t");
            String type = parts[0];
            String pred = classify(parts[1]);
            if(pred.equals(type)||(pred.equals("DESCRIPTION")&&type.equals("ENTITY"))) {
                correct++;
            }
            all++;
            if(all % 1000 == 0) System.out.println(all);
        }

        double accuracy = (double) correct / all;

        System.out.println("Accuracy: "+accuracy);

        in.close();
    }
}
