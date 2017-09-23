package WebSearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

import Core.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Chris Samarinas
 */
public class WebSearch {

    public static WebResult[] search(String query) throws UnsupportedEncodingException, IOException{
        String search_url = "https://www.googleapis.com/customsearch/v1element?key=AIzaSyCVAXiUzRYsML1Pv6RwSG1gunmMikTzQqY&num="+Config.WEB_RESULTS_TOTAL+"&prettyPrint=false&source=gcsc&gss=.com&sig=ee93f9aae9c9e9dba5eea831d506e69a&cx=partner-pub-8993703457585266:4862972284&googlehost=www.google.com&hl=en&q="+URLEncoder.encode(query, "UTF-8");
        String json = Jsoup.connect(search_url).ignoreContentType(true).execute().body();
        
        JSONObject obj = new JSONObject(json);
        JSONArray results = obj.getJSONArray("results");
        
        WebResult[] final_results = new WebResult[results.length()];
        
        for (int i = 0; i < results.length(); i++){
            JSONObject result = results.getJSONObject(i);
            String title = result.getString("titleNoFormatting");
            String url = result.getString("unescapedUrl");
            String snippet = result.getString("contentNoFormatting");
            WebResult r = new WebResult(title, url, snippet);
            final_results[i] = r;
        }
        
        return final_results;
    }

    /*
    public static WebResult[] search(String query) {
        WebResult[] final_results = new WebResult[Config.WEB_RESULTS_TOTAL];
        int i = 0;
        Document doc;
        try{
            doc = Jsoup.connect("https://www.google.com/search?hl=en&q="+URLEncoder.encode(query, "UTF-8")).userAgent(Config.USER_AGENT).ignoreHttpErrors(true).timeout(0).get();
            Elements links = doc.select("div[class=g]");
            for (Element link : links) {
                Elements titles = link.select("h3[class=r]");
                String title = titles.text();
                String url = titles.select("a").attr("href");
                Elements bodies = link.select("span[class=st]");
                String snippet = bodies.text();
                WebResult r = new WebResult(title, url, snippet);
                final_results[i] = r;
                i++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return final_results;
    }
    */

    public static long resultsCount(String query) throws IOException {
        final URL url;
        url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8"));
        final URLConnection connection = url.openConnection();

        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.addRequestProperty("User-Agent", "Google Chrome/36");//put the browser name/version

        final Scanner reader = new Scanner(connection.getInputStream(), "UTF-8");  //scanning a buffer from object returned by http request

        while(reader.hasNextLine()){   //for each line in buffer
            final String line = reader.nextLine();

            if(!line.contains("\"resultStats\">"))//line by line scanning for "resultstats" field because we want to extract number after it
                continue;
            try{
                return Long.parseLong(line.split("\"resultStats\">")[1].split("<")[0].replaceAll("[^\\d]", ""));//finally extract the number convert from string to integer
            }finally{
                reader.close();
            }
        }
        reader.close();
        return 0;
    }
    
}
