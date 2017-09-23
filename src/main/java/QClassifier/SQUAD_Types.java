package QClassifier;

import TextModel.Word;
import TextModel.WordsUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Chris Samarinas
 */
public class SQUAD_Types {

    public static void createDataset() throws IOException {
        /*
        String dataset = "C:/Users/Chris/Desktop/Wamby/data/squad/dev.json";

        PrintWriter writer = new PrintWriter("C:/Users/Chris/Desktop/Wamby/data/squad/squad_types.txt", "UTF-8");
        File input = new File(dataset);
        String json = new String(Files.readAllBytes(Paths.get(dataset)));

        JSONObject obj = new JSONObject(json);
        JSONArray data = obj.getJSONArray("data");

        int max_length = 0;

        for (int i=0; i<data.length(); i++) {
            JSONObject item = data.getJSONObject(i);
            JSONArray paragraphs = item.getJSONArray("paragraphs");
            for (int j=0; j<paragraphs.length(); j++) {
                JSONObject paragraph = paragraphs.getJSONObject(j);
                JSONArray qas = paragraph.getJSONArray("qas");
                for (int k=0; k<qas.length(); k++) {
                    JSONObject qa = qas.getJSONObject(k);
                    String question = qa.getString("question");
                    JSONArray answers = qa.getJSONArray("answers");
                    String min_answer = answers.getJSONObject(0).getString("text");
                    for(int m=0; m<answers.length(); m++) {
                        String answer = answers.getJSONObject(m).getString("text");
                        if(answer.length() < min_answer.length()){
                            min_answer = answer;
                        }
                    }

                    String type = "OTHER";

                    ArrayList<Word> words = WordsUtil.getWords(min_answer);
                    for(Word w : words) {
                        if(!w.getNE().equals("O")) {
                            type = w.getNE();
                            break;
                        }
                    }

                    if(type.equals("OTHER") && words.size()==1) {
                        type = words.get(0).getPOS();
                    }

                    if(words.size() > 0) {
                        if(question.length()>max_length) {
                            max_length = question.length();
                        }
                        writer.println(type+"\t"+question);
                    }
                }
            }
        }
        writer.close();
        System.out.println(max_length);
*/

        HashMap<String, ArrayList<String>> data = new HashMap<>();
        PrintWriter writer = new PrintWriter("C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_types_train_all.txt", "UTF-8");
        PrintWriter writer2 = new PrintWriter("C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_types_test_all.txt", "UTF-8");
        BufferedReader in = new BufferedReader(new FileReader("C:/Users/Chris/Desktop/Wamby/data/squad_types/squad_all.txt"));
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split("\\t");
            String s1 = parts[0];
            String s2 = parts[1];

            if(s1.equals("SET")||s1.equals("TIME")) {
                continue;
            }

            if(data.get(s1)!=null) {
                data.get(s1).add(s1+"\t"+s2);
            }else{
                ArrayList<String> items = new ArrayList<>();
                items.add(s1+"\t"+s2);
                data.put(s1,items);
            }
        }

        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> lines2 = new ArrayList<>();
        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ArrayList<String> items = (ArrayList<String>) pair.getValue();
            int i = 0;
            for(String item : items) {
                if(i>=items.size()/2) {
                    lines2.add(item);
                }else {
                    lines.add(item);
                }
                i++;
            }
        }

        Collections.shuffle(lines);
        Collections.shuffle(lines2);

        for(String item : lines) {
            writer.println(item);
        }
        for(String item : lines2) {
            writer2.println(item);
        }

        writer.close();
        writer2.close();
    }
}
