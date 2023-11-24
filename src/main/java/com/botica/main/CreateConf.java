package com.botica.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CreateConf {

    protected static final Logger logger = LogManager.getLogger(CreateConf.class);

    private static final String BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/bots-definition.json";    // The path to the bots definition file.
    private static final String BOTS_PROPERTIES_PATH = "src/main/resources/ConfigurationFiles/";                 // The path to the bots properties files.
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";                                     // The path to the docker compose file.
    
    public static void main(String[] args) {
        createBotPropertiesFiles(BOTS_DEFINITION_PATH);
        logger.info("Bot properties files created successfully! Bot ids: {}", botIds);
        createDockerCompose();
    }

    private static List<String> botIds = new ArrayList<>();

    private static void createBotPropertiesFiles(String filePath){

        try (FileReader reader = new FileReader(filePath)) {

            // Parse the JSON file using JSONTokener
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray jsonArray = new JSONArray(tokener);
            
            // Iterate through the array and process each JSON object
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> jsonmap = jsonObject.toMap();
               
                List<String> configurationPairs = createConfigurationPairs(jsonmap);
                List<Map<String,Object>> bots = (List<Map<String,Object>>) jsonmap.get("bots");

                bots.forEach(bot -> createBotPropertiesFile(bot, configurationPairs));                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> createConfigurationPairs(Map<String, Object> jsonMap) {
        List<String> configurationPairs = new ArrayList<>();
        String property;

        property = "botType";
        configurationPairs.add(property + "=" + jsonMap.get(property).toString());
        property = "dockerImage";
        configurationPairs.add(property + "=" + jsonMap.get(property).toString());
        property = "order";
        configurationPairs.add(property + "=" + jsonMap.get(property).toString());
        property = "keyToPublish";
        configurationPairs.add(property + "=" + jsonMap.get(property).toString());
        property = "orderToPublish";
        configurationPairs.add(property + "=" + jsonMap.get(property).toString());

        Map<String, Object> rabbitOptions = (Map<String, Object>) jsonMap.get("rabbitOptions");
        property = "queueByBot";
        configurationPairs.add("rabbitOptions." + property + "=" + rabbitOptions.get(property).toString());
        property = "mainQueue";
        configurationPairs.add("rabbitOptions." + property + "=" + rabbitOptions.get(property).toString());
        property = "bindings";
        List<String> bindings = (List<String>) rabbitOptions.get(property);
        String content = bindings.stream().collect(Collectors.joining(","));
        configurationPairs.add("rabbitOptions." + property + "=" + content);

        return configurationPairs;
    }

    private static void createBotPropertiesFile(Map<String,Object> bot, List<String> configurationPairs){

        List<String> botProperties = new ArrayList<>();

        bot.keySet().forEach(key -> botProperties.add("bot." + key + " = " + bot.get(key)));

        String botId = bot.get("botId").toString();

        botIds.add(botId);
        configurationPairs.forEach(pair -> botProperties.add(0,pair));

        Path filePath = Path.of(BOTS_PROPERTIES_PATH + botId + ".properties");

        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, botProperties, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Bot properties file created successfully! Bot id: {}", botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDockerCompose(){ 

        List<String> content = new ArrayList<>();

        String initialContent = "version: '3'\r\n" +
                "\r\n" +
                "networks:\r\n" +
                "  rabbitmq-network:\r\n" +
                "    driver: bridge\r\n" +
                "\r\n" +
                "services:\r\n" +
                "  rabbitmq:\r\n" +
                "    image: \"rabbitmq:3.12-management\"\r\n" +
                "    ports:\r\n" +
                "      - \"5672:5672\"\r\n" +
                "      - \"15672:15672\"\r\n" +
                "    environment:\r\n" +
                "      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbitmq_management load_definitions \"/run/secrets/rabbit_config\"\r\n" +
                "    secrets:\r\n" +
                "      - rabbit_config\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network";
        String templateIntermediateContent = "  XXX:\r\n" +
                "    depends_on:\r\n" +
                "      - rabbitmq\r\n" +
                "    build:\r\n" +
                "      context: .\r\n" +
                "      dockerfile: ./Dockerfile\r\n" +
                "    environment:\r\n" +
                "      - BOT_PROPERTY_FILE_PATH=/app/src/main/resources/ConfigurationFiles/XXX\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network";
        String finalContent = "secrets:\r\n" +
                "  rabbit_config:\r\n" +
                "    file: ./rabbitmq/definitions.json";

        content.add(initialContent);
        
        for(int i = 0;i < botIds.size();i++){
            String intermediateContent = templateIntermediateContent;
            content.add(intermediateContent.replace("XXX", botIds.get(i)));
        }

        content.add(finalContent);

        Path filePath = Path.of(DOCKER_COMPOSE_PATH);
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Docker compose file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
