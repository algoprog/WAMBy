package QClassifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chris Samarinas
 */
public class TREC_Types {
    public static void createDataset() throws IOException {
        PrintWriter writer = new PrintWriter("C:/Users/Chris/Desktop/Wamby/data/trec_types/train_.txt", "UTF-8");
        BufferedReader in = new BufferedReader(new FileReader("C:/Users/Chris/Desktop/Wamby/data/trec_types/train.txt"));
        String line;
        /*
        while ((line = in.readLine()) != null){
            String[] parts = line.split("\\t");
            String type = parts[1];
            if(type.equals("NUM:date")) {
                type = "DATE";
            }
            else if(type.equals("NUM:money")) {
                type = "MONEY";
            }
            else if(type.equals("NUM:ord")) {
                type = "ORDINAL";
            }
            else if(type.equals("NUM:period")) {
                type = "DURATION";
            }
            else if(type.equals("NUM:perc")) {
                type = "PERCENT";
            }
            else if(type.equals("NUM:count")) {
                type = "NUMBER";
            }
            else if(type.startsWith("ENTY")) {
                type = "ENTITY";
            }
            else if(type.startsWith("DESC")) {
                type = "DESCRIPTION";
            }
            else if(type.equals("HUM:gr")) {
                type = "ORGANIZATION";
            }
            else if(type.equals("HUM:ind")) {
                type = "PERSON";
            }
            else if(type.startsWith("LOC")) {
                type = "LOCATION";
            }
            else {
                continue;
            }
            String question = parts[2];
            writer.println(type+"\t"+question);
        }
        writer.close();
        */

        ArrayList<String> items = new ArrayList<>();
        while ((line = in.readLine()) != null){
            if(!line.startsWith("DESCRIPTION") && !line.startsWith("ENTITY")) items.add(line);
        }
        Collections.shuffle(items);
        for(String l : items) {
            writer.println(l);
        }
        writer.close();
    }
}
