package com.botica.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
    private static final String BOTS_PROPERTIES_PATH = "src/main/resources/ConfigurationFiles/";                // The path to the bots properties files.
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";                                     // The path to the docker compose file.
    private static final String DUMMY_DOCKERFILE = "docker/Dockerfile";                                         // The path to the dummy dockerfile.
    private static final String INIT_VOLUME_SCRIPT_PATH = "docker/init_volume.sh";                              // The path to the init volume script.
    
    public static void main(String[] args) {
        createBotPropertiesFiles(BOTS_DEFINITION_PATH);
        logger.info("Bot properties files created successfully! Bot ids: {}", botIds);
        createDockerCompose();
        createDummyDockerfile();
        createInitVolumeScript();
    }

    private static List<String> botIds = new ArrayList<>();
    private static Set<String> requiredPaths = new HashSet<>();
    private static List<String> botImages = new ArrayList<>();
    private static String botImage;

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

        botImage = jsonMap.get(property).toString();

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
        
        List<String> botrequiredPaths = (List<String>) jsonMap.get("requiredPaths");
        requiredPaths.addAll(botrequiredPaths);

        return configurationPairs;
    }

    private static void createBotPropertiesFile(Map<String,Object> bot, List<String> configurationPairs){

        List<String> botProperties = new ArrayList<>();

        bot.keySet().forEach(key -> botProperties.add("bot." + key + " = " + bot.get(key)));

        String botId = bot.get("botId").toString();

        botIds.add(botId);
        botImages.add(botImage);
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
        String templateIntermediateContent = "  BOT_ID:\r\n" +
                "    depends_on:\r\n" +
                "      - rabbitmq\r\n" +
                "    restart: unless-stopped\r\n" +
                "    image: BOT_IMAGE\r\n" +
                "    environment:\r\n" +
                "      - BOT_PROPERTY_FILE_PATH=/app/volume/src/main/resources/ConfigurationFiles/BOT_ID.properties\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network\r\n" +
                "    volumes:\r\n" +
                "      - botica-volume:/app/volume";
        String finalContent = "\nvolumes:\r\n" +
                "  botica-volume:\r\n\n" +
                "networks:\r\n" +
                "  rabbitmq-network:\r\n" +
                "    driver: bridge\r\n\n" +
                "secrets:\r\n" +
                "  rabbit_config:\r\n" +
                "    file: ./rabbitmq/definitions.json";

        content.add(initialContent);
        
        for(int i = 0;i < botIds.size();i++){
            String intermediateContent = templateIntermediateContent;
            content.add(intermediateContent.replace("BOT_ID", botIds.get(i)).replace("BOT_IMAGE", botImages.get(i)));
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

    private static void createDummyDockerfile() {
        Path dockerfilePath = Path.of(DUMMY_DOCKERFILE);
        createDir(dockerfilePath);

        try {
            Files.writeString(dockerfilePath, "FROM alpine:3.18.4\n\n", StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(dockerfilePath, "WORKDIR /app\n\n", StandardOpenOption.APPEND);
            Files.writeString(dockerfilePath, "RUN mkdir -p rabbitmq\n", StandardOpenOption.APPEND);

            Set<String> directoriesToCreate = createRequiredDirectories();

            for (String directory : directoriesToCreate) {
                String dockerfileCommand = String.format("RUN mkdir -p %s%n", directory);
                Files.writeString(dockerfilePath, dockerfileCommand, StandardOpenOption.APPEND);
            }

            Files.writeString(dockerfilePath, "\nRUN chmod -R 755 /app\n", StandardOpenOption.APPEND);

            logger.info("Dummy Dockerfile created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<String> createRequiredDirectories(){

        Set<String> directoriesToCreate = new HashSet<>();
        String directoryToCreate;

        for (String path : requiredPaths) {
            if (Files.isDirectory(Path.of(path))) {
                directoryToCreate = path.replace("./", "");
            } else{
                directoryToCreate = path.replace("./", "").replace("/" + Path.of(path).getFileName().toString(), "");
            }

            if (directoryToCreate.contains("/")){
                directoryToCreate = directoryToCreate.substring(0, directoryToCreate.lastIndexOf("/"));
                directoriesToCreate.add(directoryToCreate);
            }
        }

        return directoriesToCreate;
    }

    private static void createInitVolumeScript() {
        Path scriptPath = Path.of(INIT_VOLUME_SCRIPT_PATH);
        createDir(scriptPath);

        try {
            Files.writeString(scriptPath, "#!/bin/bash\n\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "if docker ps -a | grep -q dummy; then\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    echo \"Removing existing container...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    docker rm dummy\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "fi\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "if docker volume ls | grep -q botica_botica-volume; then\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    echo \"Removing existing volume...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    docker volume rm botica_botica-volume\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "fi\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "docker container create --name dummy -v botica_botica-volume:/app dummy\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker volume create --name botica_botica-volume\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "docker cp ./rabbitmq/server-config.json dummy:/app/rabbitmq/server-config.json\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker cp ./src/main/resources/ConfigurationFiles dummy:/app/src/main/resources/ConfigurationFiles\n\n", StandardOpenOption.APPEND);

            for (String path : requiredPaths) {

                String scriptCommand;
                String auxPath = path.replace("./", "");
                if (auxPath.contains("/")) {
                    String newPath = auxPath.substring(0, auxPath.lastIndexOf("/"));
                    scriptCommand = String.format("docker cp %s dummy:/app/%s%n", path, newPath);
                }else{
                    scriptCommand = String.format("docker cp %s dummy:/app%n", path);
                }
                Files.writeString(scriptPath, scriptCommand, StandardOpenOption.APPEND);
            }

            Files.writeString(scriptPath, "\ndocker rm dummy\n", StandardOpenOption.APPEND);

            logger.info("Init volume script created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDir(Path filePath) {

        Path parent = filePath.getParent();

        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
