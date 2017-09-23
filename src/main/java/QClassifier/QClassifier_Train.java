package QClassifier;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;

public class QClassifier_Train {

    public static void trainModel() throws IOException {

        int batchSize = 25;
        int vectorSize = 50;     // word vectors size
        int nEpochs = 1;        // number of epochs (110)
        int truncateToLength = 30;
        int lstm_units = 20;    // number of LSTM units
        int classes = 11;

        File locationToSave = new File("C:/Users/Chris/Desktop/Wamby/data/models/question_types_alll.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally

        // Network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
                .updater(Updater.ADAM).adamMeanDecay(0.9).adamVarDecay(0.999)
                .seed(12345)
                .regularization(true).l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .learningRate(0.25)
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(vectorSize).nOut(lstm_units)
                        .activation(Activation.fromString("softmax")).build())
                .layer(1, new RnnOutputLayer.Builder().activation(Activation.fromString("softmax"))
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(lstm_units).nOut(classes).build())
                .pretrain(false).backprop(true).build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        //MultiLayerNetwork net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        net.init();

        net.setListeners(new ScoreIterationListener(1));

        DataSetIterator train = new AsyncDataSetIterator(new DataIterator(batchSize,truncateToLength,5000,true, classes, vectorSize),1);
        DataSetIterator test = new AsyncDataSetIterator(new DataIterator(100,truncateToLength,2000,false, classes, vectorSize),1);

        /*
        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored
        StatsStorage statsStorage = new InMemoryStatsStorage();

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        net.setListeners(new StatsListener(statsStorage));
        */


        Evaluation evaluation = new Evaluation();
        /*
        while(test.hasNext()){
            DataSet t = test.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray inMask = t.getFeaturesMaskArray();
            INDArray outMask = t.getLabelsMaskArray();
            INDArray predicted = net.output(features,false,inMask,outMask);

            evaluation.evalTimeSeries(labels,predicted,outMask);
        }
        test.reset();

        System.out.println(evaluation.stats());
        */


        System.out.println("Started training...");
        for( int i=0; i<nEpochs; i++ ){
            net.fit(train);
            train.reset();
            System.out.println("Epoch " + i + " complete.");


            evaluation = new Evaluation();
            while(test.hasNext()){
                DataSet t = test.next();
                INDArray features = t.getFeatureMatrix();
                INDArray labels = t.getLabels();
                INDArray inMask = t.getFeaturesMaskArray();
                INDArray outMask = t.getLabelsMaskArray();
                INDArray predicted = net.output(features,false,inMask,outMask);

                evaluation.evalTimeSeries(labels,predicted,outMask);
            }
            test.reset();
            System.out.println(evaluation.stats());
        }

        //Save the model
        boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        ModelSerializer.writeModel(net, locationToSave, saveUpdater);


        System.out.println("Finished training");

    }

}
