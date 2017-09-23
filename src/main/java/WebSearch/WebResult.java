package WebSearch;

/**
 *
 * @author Chris Samarinas
 */
public class WebResult {
    private String title;
    private String url;
    private String snippet;
    
    public WebResult(String title, String url, String snippet){
        this.title = title;
        this.url = url;
        this.snippet = snippet;
    }
    
    public void setTitle(String str){
        title = str;
    }
    
    public void setUrl(String str){
        url = str;
    }
    
    public void setSnippet(String str){
        snippet = str;
    }
    
    public String getTitle(){
        return title;
    }
    
    public String getUrl(){
        return url;
    }
    
    public String getSnippet(){
        return snippet;
    }
}
