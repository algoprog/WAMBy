package TextModel;

import Core.Wamby;
import TextModel.Sentence;
import TextModel.Word;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Chris Samarinas
 */
public class WordsUtil {
    private static String[] stopwords = {"a","about","above","above","across","after","afterwards","again","against","all","almost","alone","along","already","also","although","always","am","among","amongst","amoungst","amount","an","and","another","any","anyhow","anyone","anything","anyway","anywhere","are","around","as","at","back","be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","below","beside","besides","between","beyond","bill","both","bottom","but","by","call","can","cannot","cant","co","con","could","couldnt","cry","de","describe","detail","do","done","down","due","during","each","eg","eight","either","eleven","else","elsewhere","empty","enough","etc","even","ever","every","everyone","everything","everywhere","except","few","fifteen","fify","fill","find","fire","first","five","for","former","formerly","forty","found","four","from","front","full","further","get","give","go","had","has","hasnt","have","he","hence","her","here","hereafter","hereby","herein","hereupon","hers","herself","him","himself","his","how","however","hundred","ie","if","in","inc","indeed","interest","into","is","it","its","itself","keep","last","latter","latterly","least","less","ltd","made","many","may","me","meanwhile","might","mill","mine","more","moreover","most","mostly","move","much","must","my","myself","name","namely","neither","never","nevertheless","next","nine","no","nobody","none","noone","nor","not","nothing","now","nowhere","of","off","often","on","once","one","only","onto","or","other","others","otherwise","our","ours","ourselves","out","over","own","part","per","perhaps","please","put","rather","re","same","see","seem","seemed","seeming","seems","serious","several","she","should","show","side","since","sincere","six","sixty","so","some","somehow","someone","something","sometime","sometimes","somewhere","still","such","system","take","ten","than","that","the","their","them","themselves","then","thence","there","thereafter","thereby","therefore","therein","thereupon","these","they","thickv","thin","third","this","those","though","three","through","throughout","thru","thus","to","together","too","top","toward","towards","twelve","twenty","two","un","under","until","up","upon","us","very","via","was","we","well","were","what","whatever","when","whence","whenever","where","whereafter","whereas","whereby","wherein","whereupon","wherever","whether","which","while","whither","who","whoever","whole","whom","whose","why","will","with","within","without","would","yet","you","your","yours","yourself","yourselves"};
    private static final Set<String> stopWordSet = new HashSet<>(Arrays.asList(stopwords));
    
    public static boolean isStopword(String word) {
	if(word.length() < 2) return true;
	if(stopWordSet.contains(word)) return true;
	else return false;
    }
    
    public static ArrayList<Word> getWords(String text){

        ArrayList<Word> words = new ArrayList<>();
        
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        Wamby.stanford_nlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word_text = token.get(CoreAnnotations.TextAnnotation.class);
				
				// this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                // this is the lemma of the token
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
				
				if(!ne.equals("NUMBER")) {
                    word_text = word_text.replaceAll("-"," ");
                    word_text = word_text.replaceAll("_"," ");
                    word_text = word_text.replaceAll("['`]","").trim();
				}
				
                if(word_text.length()>0){
                    Word w = new Word(word_text, pos, ne, lemma);
                    words.add(w);
                }
            }
        }
        
        return words;
    }

    public static ArrayList<Sentence> getSentences(String text) {
        ArrayList<Sentence> sentences_res = new ArrayList<>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        Wamby.stanford_nlp.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        int id = 0;

        for(CoreMap sentence: sentences) {
            ArrayList<Word> words = new ArrayList<>();
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word_text = token.get(CoreAnnotations.TextAnnotation.class);

                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                // this is the lemma of the token
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                if(!ne.equals("NUMBER")) {
                    word_text = word_text.replaceAll("-"," ");
                    word_text = word_text.replaceAll("_"," ");
                    word_text = word_text.replaceAll("['`]","").trim();
                }

                if(word_text.length()>0){
                    Word w = new Word(word_text, pos, ne, lemma);
                    words.add(w);
                }
            }

            Sentence s = new Sentence(id, sentence.toString(), words);
            sentences_res.add(s);
            id++;
        }

        return sentences_res;
    }
}
