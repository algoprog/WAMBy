package Extractors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import TextModel.StringsUtil;
import org.bytedeco.javacpp.presets.opencv_core;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 *
 * @author Chris Samarinas
 */
public class CommentsExtractor {
    
    private ArrayList<CNode> nodes;
    private ArrayList<String> answers;
    HashMap<Integer, Bucket> buckets; // the buckets with structually same nodes
    private Bucket commentsBucket;
    
    private class Bucket {
        private ArrayList<CNode> nodes;
        private HashSet<Integer> ids;
        private int depth;
        private double average_text_length;
        
        public Bucket(int depth) {
            nodes = new ArrayList<>();
            this.depth = depth;
            this.average_text_length = 0;
            this.ids = new HashSet<>();
        }
        public void addNode(CNode n) {
            if(!ids.contains(n.getID())) {
                nodes.add(n);
                ids.add(n.getID());
            }
            
        }
        
        public void setDepth(int value) { this.depth = value; }
        
        public void findAverageTextLength() { 
            int sum = 0;
            for(CNode n : this.nodes) {
                sum += Jsoup.parse(n.toString()).text().length();
            }
            
            if(getNodesCount() > 0){
                this.average_text_length = (double)sum / getNodesCount();
            }
        }
        
        public int getDepth() { return this.depth; }
        
        public double getAverageTextLength() { return this.average_text_length; }
        
        public int getNodesCount() { return this.nodes.size(); }
        
        public ArrayList<String> getComments() {
            ArrayList<String> comments = new ArrayList<>();
            
            for(CNode n : this.nodes) {
                comments.add(n.toString());
            }
            
            return comments;
        }
    }
    
    private class CNode {
        private final int parent_id;
        private final int id;
        private int state; // 0, 1, 2 or 3
        private int depth;
        private Node node;
        
        public CNode(Node node, int id, int parent, int depth) { 
            this.node = node; 
            this.id = id;
            this.parent_id = parent; 
            this.state = 0; 
            this.depth = depth;
        }
        
        public void setState(int s) { this.state = s; }
        
        public int getID() { return this.id; }
        
        public int getParent() { return this.parent_id; }
        
        public int getState() { return this.state; }
        
        public Node getNode() { return this.node; }
        
        public int getDepth() { return this.depth; }
        
        @Override
        public String toString() { return this.node.toString(); }
    }
    
    public CommentsExtractor(String url, Document doc) throws IOException {
        nodes = new ArrayList<>();
        buckets = new HashMap<>();
        
        for(Element e : doc.select("[class*='quote']")){
            e.remove();
        }
        
        answers = new ArrayList<>();

        if(url.contains("quora.com")) {
            System.out.println("Quora");
            quoraAnswers(doc);
        }
        else if(url.contains("reddit.com")) {
            redditAnswers(doc);
        }
        else {
            detectAnswers(doc);
        }

        //if(answers.size()>0) {
            return;
        //}

        /*
        Element body = doc.body();
        
        visitNode(body, 0, -1);
        
        commentsBucket = new Bucket(Integer.MAX_VALUE);
        int key = 0;
        
        Iterator it = buckets.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry p = (Map.Entry)it.next();
            Bucket b = (Bucket)p.getValue();
            
            if(b.getNodesCount()>1) {
                b.findAverageTextLength();
                
                //System.out.println("\nBUCKET KEY: "+p.getKey()+"\nDEPTH: "+b.getDepth()+"\nNODES: "+b.getNodesCount()+"\nAVG_TEXT_LENGTH: "+b.getAverageTextLength()+"\n");
                
                if(b.getDepth() < commentsBucket.getDepth()) {
                    commentsBucket = b;
                    key = (Integer)p.getKey();
                }
                else if(b.getDepth() == commentsBucket.getDepth()) {
                    if(b.getAverageTextLength() > commentsBucket.getAverageTextLength()) {
                        commentsBucket = b;
                        key = (Integer)p.getKey();
                    }
                }
            }
        }
        
        //System.out.println("[FINAL CHOICE]\nBUCKET KEY: "+key+"\nDEPTH: "+commentsBucket.getDepth()+"\nNODES: "+commentsBucket.getNodesCount()+"\nAVG_TEXT_LENGTH: "+commentsBucket.getAverageTextLength()+"\n");
        */
    }
    
    public ArrayList<String> getComments() {
        if(answers.size()>0) {
            return cleanText(answers);
        }

        if(this.commentsBucket==null) {
            return new ArrayList<>();
        }

        ArrayList<String> comments = new ArrayList<>();
        ArrayList<String> tmp_comments = cleanText(this.commentsBucket.getComments());
        for(String comment : tmp_comments) {
            if(!comment.trim().equals("")) {
                comments.add(comment);
            }
        }
        return comments;
    }
    
    private ArrayList<String> cleanText(ArrayList<String> comments) {
        //return comments;

        ArrayList<String> cleanComments = new ArrayList<>();
        for(String s : comments) {
            String comment = Jsoup.parse("<html><body>"+s+"</body></html>").text();
            cleanComments.add(comment);
        }
        
        return cleanComments;
    }
    
    private int getNodeType(String nodeName) {
        switch(nodeName) {
            case "div":
                return 2;
            case "table":
                return 3;
            case "tbody":
                return 4;
            case "tr":
                return 5;
            case "td":
                return 6;
            case "body":
                return 7;
            case "a":
                return 1;
            case "#text":
                return 0;
            default:
                return 8;
        }
    }

    private ArrayList<String> quoraAnswers(Document doc) {
        for(Element e : doc.select("div.ExpandedAnswer")) {
            answers.add(e.html());
        }
        return answers;
    }

    private ArrayList<String> redditAnswers(Document doc) {
        doc.select("div.child").remove();
        for(Element e : doc.select("div.comment")) {
            answers.add(e.select("div.md").html());
        }
        return answers;
    }

    private ArrayList<String> detectAnswers(Document doc) {
        for(Element e : doc.select("[itemtype*='schema.org/Answer']")) {
            answers.add(e.html());
        }
        return answers;
    }
    
    private void visitNode(Node node, int depth, int parent_id) {
        //System.out.println("VISIT");
        
        int nodeType = getNodeType(node.nodeName());
        //System.out.println(node.nodeName());
        
        if(nodeType > 1) {
            //System.out.println("BLOCK NODE "+nodeType);
            
            int current_node_id = nodes.size();
            CNode cnode = new CNode(node, current_node_id, parent_id, depth);
            nodes.add(cnode);
            
            int children_blocks = 0;
            for(Node child : node.childNodes()) {
                if(getNodeType(child.nodeName()) > 0){
                    children_blocks++;
                    visitNode(child, depth+1, current_node_id);
                }
            }
            
            if(children_blocks==0) {
                if(StringsUtil.containsDate(Jsoup.parse(node.toString()).text())) {
                    propagateState(1, parent_id);
                }
            }
        }
        else if(nodeType == 1) {
            //System.out.println("["+node.attr("href")+"]");
            if(StringsUtil.containsUserLink(node.attr("href"))) {
                //System.out.println("IS PROFILE LINK");
                propagateState(2, parent_id);
            }else {
                //System.out.println("IS LINK");
            }
        }
    }
    
    private void propagateState(int state, int parent_id) {
        int distance = 0;
        while(parent_id > 0 && distance < 4) {
            CNode parent = nodes.get(parent_id);
            int parent_state = parent.getState();
            if(parent_state == 0) {
                parent.setState(state);
                //System.out.println("CONTAINS "+state);
            }
            else if(parent_state != state) {
                parent.setState(3);
                System.out.println("BOTH");
                
                int depth = parent.getDepth();
                int bucket_key = depth;
                if(buckets.containsKey(bucket_key)) {
                    buckets.get(bucket_key).addNode(parent);
                }
                else {
                    Bucket b = new Bucket(depth);
                    b.addNode(parent);
                    buckets.put(bucket_key, b);
                }
                
            }
            parent_id = parent.getParent();
            distance++;
        }
    }
}
