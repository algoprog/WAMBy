package QClassifier;

import Core.Wamby;
import TextModel.Word;
import TextModel.WordsUtil;
import Utils.FileReaders;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class DataIterator implements DataSetIterator {
    private final int batchSize;
    private final int vectorSize;
    private final int truncateLength;
    private final int totalExamples;

    private int cursor = 0;
    private final TokenizerFactory tokenizerFactory;

    private String dataset;
    private ArrayList<String> data;
    private int labels_count;

    /**
     * @param batchSize size of each batch for training
     * @param truncateLength truncate questions to this length
     * @param train true: return the training data. false: return the testing data.
     */
    public DataIterator(int batchSize, int truncateLength, int maxSamples, boolean train, int labels_count, int vectorSize) throws IOException {
        this.batchSize = batchSize;
        this.vectorSize = vectorSize;
        this.labels_count = labels_count;

        //this.dataset = "C:/Users/Chris/Desktop/Wamby/data/trec_types/" + (train ? "train" : "test") + ".txt";
        if(train) {
            this.dataset = "C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_types_train_all.txt";
        }
        else {
            this.dataset = "C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_types_test_all.txt";
        }

        this.truncateLength = truncateLength;
        this.totalExamples = Math.min(FileReaders.countLines(dataset), maxSamples);

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        data = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(dataset));
        String line;
        int i = 0;
        while((line = in.readLine()) != null && i < totalExamples)
        {
            data.add(line);
            i++;
        }
    }


    @Override
    public DataSet next(int num) {
        if (cursor >= totalExamples) throw new NoSuchElementException();
        try{
            return nextDataSet(num);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int num) throws IOException {
        List<String> questions = new ArrayList<>(num);
        int[] tlabels = new int[num];
        int k = 0;
        while (cursor<totalExamples && k<num) {
            String line = data.get(cursor);
            String[] parts = line.split("\\t");
            String label = parts[0];
            String question = parts[1];
            questions.add(question);
            tlabels[k] = labelToInt(label);
            //System.out.println(line+"---"+tlabels[k]);
            k++;
            cursor++;
        }

        // tokenize questions and filter out unknown words
        List<List<String>> allTokens = new ArrayList<>(questions.size());
        int maxLength = 0;
        for(String s : questions){
            ArrayList<Word> words = WordsUtil.getWords(s);
            List<String> tokens = new ArrayList<>();
            for (Word w : words) {
                //tokens.add(w.getLemma());
                tokens.add(w.getText());
            }
            List<String> tokensFiltered = new ArrayList<>();
            for(String t : tokens ){
                if(Wamby.wordVectors.hasWord(t)) tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength,tokensFiltered.size());
        }

        // truncate long questions
        if(maxLength > truncateLength) maxLength = truncateLength;

        // create data for training
        INDArray features = Nd4j.create(questions.size(), vectorSize, maxLength);
        INDArray labels = Nd4j.create(questions.size(), labels_count, maxLength);

        // mask arrays contain 1 if data is present at that time step or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(questions.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(questions.size(), maxLength);

        int[] temp = new int[2];
        for(int i=0; i<questions.size(); i++){
            List<String> tokens = allTokens.get(i);
            temp[0] = i;
            // get word vectors
            for( int j=0; j<tokens.size() && j<maxLength; j++ ){
                String token = tokens.get(j);
                INDArray vector = Wamby.wordVectors.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0);  // word is present -> 1.0 in features mask
            }

            int idx = tlabels[i];
            int lastIdx = Math.min(tokens.size(),maxLength);
            labels.putScalar(new int[]{i,idx,lastIdx-1},1.0);   // set the label
            labelsMask.putScalar(new int[]{i,lastIdx-1},1.0);   // an output exists at the final time step
        }

        return new DataSet(features,labels,featuresMask,labelsMask);
    }

    private static int labelToInt(String label) {
        List<String> labels = getLabels_();
        int i = 0;
        for(String tlabel : labels) {
            if(tlabel.equals(label)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public static String intToLabel(int k) {
        return getLabels_().get(k);
    }

    @Override
    public int totalExamples() {
        return totalExamples;
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return 2;
    }

    @Override
    public void reset() {
        cursor = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    public static List<String> getLabels_() {
        return Arrays.asList("DATE","DURATION","LOCATION","MONEY","NUMBER","ORDINAL","ORGANIZATION","PERCENT","PERSON","ENTITY","DESCRIPTION");
    }

    public static List<String> getLabels_2() {
        return Arrays.asList("DATE","DURATION","LOCATION","MONEY","NUMBER","ORDINAL","ORGANIZATION","PERCENT","PERSON","SET","TIME");
    }

    @Override
    public List<String> getLabels() {
        return getLabels_();
    }

    @Override
    public boolean hasNext() {
        return cursor < numExamples();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {}

    @Override
    public  DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
