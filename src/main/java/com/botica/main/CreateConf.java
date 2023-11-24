package com.botica.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.io.FileReader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateConf {
    
    public static void main(String[] args) {
        func("src\\main\\java\\com\\botica\\bots\\bots-definition.json");
        System.out.println(temporal);
        createCompose();
    }

    public static Map<String,List<String>> bots2List= new HashMap<>();
    public static List<String> temporal= new ArrayList<>();

    public static void func(String filePath){

        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON file using JSONTokener
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray jsonArray = new JSONArray(tokener);
            List<String> pares= new ArrayList<>();
            String property;
            // Iterate through the array and process each JSON object
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> jsonmap= jsonObject.toMap();
                property="botType";
                pares.add(property+"="+jsonmap.get(property).toString());
                property="imageDocker";

                bots2List.put(jsonmap.get(property).toString(),List.of());

                pares.add(property+"="+jsonmap.get(property).toString());
                property="order";
                pares.add(property+"="+jsonmap.get(property).toString());
                property="keyToPublish";
                pares.add(property+"="+jsonmap.get(property).toString());
                property="orderToPublish";
                pares.add(property+"="+jsonmap.get(property).toString());
                Map<String, Object> dd= (Map<String, Object>) jsonmap.get("rabbitOptions");
                property="queueByBot";
                pares.add("rabbitOptions."+property+"="+dd.get(property).toString());
                property="mainQueue";
                pares.add("rabbitOptions."+property+"="+dd.get(property).toString());
                
                property="bindings";
                List<String> ls= (List<String>) dd.get(property);
                String content= ls.stream().collect(Collectors.joining(","));
                pares.add("rabbitOptions."+property+"="+content);

                List<Map<String,Object>> bots= (List<Map<String,Object>>) jsonmap.get("bots");

                bots.forEach(z->createFile(z, pares,jsonmap.get("imageDocker").toString()));


                System.out.println(pares);

                pares.clear(); 
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFile(Map<String,Object> s, List<String> pares, String Image){
        List<String> ls= new ArrayList<>();
        s.keySet().forEach(x->ls.add("bot."+x.toString()+"="+s.get(x)));
        pares.forEach(z->ls.add(0,z));
        Path filePath = Path.of("src\\main\\resources\\ConfigurationFiles\\"+s.get("botId")+".properties");
        //bots2List.get(Image).add(s.get("botId").toString());
        temporal.add(s.get("botId").toString());
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, ls, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("File written successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createCompose(){ 
        List<String> ls= new ArrayList<>();

        String parteDeAdelante="version: '3'\r\n" + //
                "\r\n" + //
                "networks:\r\n" + //
                "  rabbitmq-network:\r\n" + //
                "    driver: bridge\r\n" + //
                "\r\n" + //
                "services:\r\n" + //
                "  rabbitmq:\r\n" + //
                "    image: \"rabbitmq:3.12-management\"\r\n" + //
                "    ports:\r\n" + //
                "      - \"5672:5672\"\r\n" + //
                "      - \"15672:15672\"\r\n" + //
                "    environment:\r\n" + //
                "      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbitmq_management load_definitions \"/run/secrets/rabbit_config\"\r\n" + //
                "    secrets:\r\n" + //
                "      - rabbit_config\r\n" + //
                "    networks:\r\n" + //
                "      - rabbitmq-network";
        String plantillasIntermedias="  XXX:\r\n" + //
                "    depends_on:\r\n" + //
                "      - rabbitmq\r\n" + //
                "    build:\r\n" + //
                "      context: .\r\n" + //
                "      dockerfile: ./Dockerfile\r\n" + //
                "    environment:\r\n" + //
                "      - BOT_PROPERTY_FILE_PATH=/app/src/main/resources/ConfigurationFiles/XXX\r\n" + //
                "    networks:\r\n" + //
                "      - rabbitmq-network";
        String parteDeAtras="secrets:\r\n" + //
                "  rabbit_config:\r\n" + //
                "    file: ./rabbitmq/definitions.json";

        
        ls.add(parteDeAdelante);
        for(int i=0;i<temporal.size();i++){
            String copia= plantillasIntermedias;
            
            ls.add(copia.replace("XXX", temporal.get(i)));
        }
        ls.add(parteDeAtras);

        Path filePath = Path.of("src\\main\\resources\\ConfigurationFiles\\docker-compose.yml");
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, ls, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("File written successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }


        

    }
}
