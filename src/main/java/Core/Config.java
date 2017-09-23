package Core;

/**
 *
 * @author Chris Samarinas
 */
public class Config {
    public static final double LINK_PERCENTAGE_THRESHOLD = 0.9;
    public static final double LIST_LINK_PERCENTAGE_THRESHOLD = 0.2;
    public static final int WORDS_COUNT_THRESHOLD = 2;
    public static final int FACTOID_WINDOW_SIZE = 200;
    public static final int WEB_RESULTS_TOTAL = 10;
    public static final double SENTENCE_SIMILARITY_THRESHOLD = 0.1;
    public static final double SENTENCE_CLUSTERING_THRESHOLD = 50;
    public static final double MIN_SNIPPET_SIZE = 4;
    public static final double MAX_LIST_SNIPPET_SIZE = 20;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
    public static final String WORD_VECTORS_DIR = "G:/GloVe/glove.6B.50d.txt";
    public static final String MODEL_TREC_TYPES = "C:/Users/Chris/Desktop/Wamby/data/models/question_types_trec.zip";
    public static final String MODEL_SQUAD_TYPES = "C:/Users/Chris/Desktop/Wamby/data/models/question_types_squad_2.zip";
}
