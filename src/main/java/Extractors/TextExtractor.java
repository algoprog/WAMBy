package Extractors;

import java.io.IOException;
import java.util.ArrayList;

import Core.Config;
import TextModel.Paragraph;
import TextModel.StringsUtil;
import TextModel.Word;
import TextModel.WordsUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Chris Samarinas
 */
public class TextExtractor {
    private ArrayList<Paragraph> paragraphs;
    private String paragraph_text = "";
    private String title = "";
    private int linked_words = 0;
    private boolean inList = false;
    private int listSize = 0;
    private int id = 1;
    private int web_position;
    
    public TextExtractor(Document doc, int web_position) throws IOException{
        paragraphs = new ArrayList<>();
        
        for(Element e : doc.select("sup")){
            e.remove();
        }

        for(Element e : doc.select("[id~=(?i)(comment)]")){
            e.remove();
        }
        for(Element e : doc.select("[class~=(?i)(comment)]")){
            e.remove();
        }
        for(Element e : doc.select("[id~=(?i)(header)]")){
            e.remove();
        }
        for(Element e : doc.select("[class~=(?i)(footer)]")){
            e.remove();
        }

        title = doc.title();
        this.web_position = web_position;

        Element body = doc.body();
        
        NodeVisitor myNodeVisitor = new MyNodeVisitor();
        NodeTraversor traversor = new NodeTraversor(myNodeVisitor);
        traversor.traverse(body);
    }
    
    private class MyNodeVisitor implements NodeVisitor {
        
        @Override
        public void head(Node node, int depth) {
            paragraph_text = StringsUtil.removeSpaces(paragraph_text);
            if (node.nodeName().equals("div")||
                node.nodeName().equals("p")||
                node.nodeName().equals("h1")||
                node.nodeName().equals("h2")||
                node.nodeName().equals("h3")||
                node.nodeName().equals("tr")||
                node.nodeName().equals("ul")||node.nodeName().equals("ol")){
                if(node.nodeName().equals("ul")||node.nodeName().equals("ol")){
                    inList = true;
                }
                if(paragraph_text.replaceAll("\\s","").length()>0){
                    if(!paragraph_text.endsWith(".")) {
                        paragraph_text += ". ";
                    }else{
                        paragraph_text += " ";
                    }
                    Paragraph p = new Paragraph(id,web_position,paragraph_text,title);
                    double link_percentage = (double)(linked_words)/p.getWordsCount();

                    if(p.getListSize()>0 && link_percentage < Config.LIST_LINK_PERCENTAGE_THRESHOLD &&
                            p.getWordsCount() > Config.WORDS_COUNT_THRESHOLD) {
                        paragraphs.add(p);
                        id++;
                    }
                    else if(p.getListSize()==0 && link_percentage < Config.LINK_PERCENTAGE_THRESHOLD &&
                       p.getWordsCount() > Config.WORDS_COUNT_THRESHOLD){
                        paragraphs.add(p);
                        id++;
                    }
                    paragraph_text = "";
                    linked_words = 0;
                    listSize = 0;
                }
            }
            else if (node.nodeName().equals("a")){
                linked_words += node.childNodes().toString().split(" ").length;
            }
            else if (node.nodeName().equals("li")){
                paragraph_text += "• ";
                listSize++;
            }
            if (node.childNodeSize() == 0 && node.nodeName().equals("#text")) {
                paragraph_text += StringEscapeUtils.unescapeHtml4(node.toString());
            }
        }
        
        @Override
        public void tail(Node node, int depth) {
            paragraph_text = StringsUtil.removeSpaces(paragraph_text);
            if (node.nodeName().equals("div")||
                node.nodeName().equals("p")||
                node.nodeName().equals("h1")||
                node.nodeName().equals("h2")||
                node.nodeName().equals("h3")||
                node.nodeName().equals("tr")||
                node.nodeName().equals("ul")||node.nodeName().equals("ol")){
                if(paragraph_text.replaceAll("\\s","").length()>0){
                    if(!paragraph_text.endsWith(".")) {
                        paragraph_text += ". ";
                    }else{
                        paragraph_text += " ";
                    }
                    Paragraph p = new Paragraph(id,web_position,paragraph_text,title);
                    double link_percentage = (double)(linked_words)/p.getWordsCount();
                    if(inList){
                        p.setListSize(listSize);
                        listSize = 0;
                    }
                    if(p.getListSize()>0 && link_percentage < Config.LIST_LINK_PERCENTAGE_THRESHOLD &&
                            p.getWordsCount() > Config.WORDS_COUNT_THRESHOLD) {
                        paragraphs.add(p);
                        id++;
                    }
                    else if(p.getListSize()==0 && link_percentage < Config.LINK_PERCENTAGE_THRESHOLD &&
                       p.getWordsCount() > Config.WORDS_COUNT_THRESHOLD &&
                       !node.nodeName().equals("h1") &&
                       !node.nodeName().equals("h2") &&
                       !node.nodeName().equals("h3")){
                        paragraphs.add(p);
                        id++;
                    }
                    paragraph_text = "";
                    linked_words = 0;
                }
            }
            else if (node.nodeName().equals("li")){
                paragraph_text += " ";
            }
            if(node.nodeName().equals("ul")||node.nodeName().equals("ol")){
                inList = false;
            }
        }
    }
    
    public ArrayList<Paragraph> getParagraphs(){
        return paragraphs;
    }
    
    public String getText() {
        String text = "";
        for(Paragraph p : paragraphs) {
            text += p.getText()+"\n";
        }
        return text;
    }
}
