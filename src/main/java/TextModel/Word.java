package TextModel;

import java.util.Objects;

/**
 *
 * @author Chris Samarinas
 */
public class Word {
    private String text;
    private String original_text;
    private String pos; // part of speech
    private String ne; // named entity type
    private String lemma;
    
    public Word(String text, String pos, String ne, String lemma){
        this.original_text = text;
        this.text = text.toLowerCase();
        this.pos = pos;
        this.ne = ne;
        this.lemma = lemma.toLowerCase();
    }
    
    public String getText(){
        return text;
    }

    public String getOriginalText(){
        return original_text;
    }
    
    public String getPOS(){
        return pos;
    }
    
    public String getNE(){
        return ne;
    }
    
    public String getLemma(){
        return lemma;
    }
    
    @Override
    public int hashCode(){
        return text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        if (!Objects.equals(this.lemma, other.lemma)) {
            return false;
        }
        return true;
    }
}
