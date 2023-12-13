package com.botica.utils.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.botica.utils.directory.DirectoryOperations;

public class CreateConfiguration {

    protected static final Logger logger = LogManager.getLogger(CreateConfiguration.class);

    private static List<String> botIds = new ArrayList<>();
    private static Set<String> requiredPaths = new HashSet<>();
    private static List<String> botImages = new ArrayList<>();
    private static String botImage;
    private static HashMap<String, List<String>> rabbitQueues = new HashMap<>();

    private CreateConfiguration() {
    }

    public static void createBotPropertiesFiles(String configurationFilePath, String botPropertiesPath){

        try (FileReader reader = new FileReader(configurationFilePath)) {

            // Parse the JSON file using JSONTokener
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray jsonArray = new JSONArray(tokener);
            
            // Iterate through the array and process each JSON object
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> jsonmap = jsonObject.toMap();
               
                Map<String, String> configurationPairs = createConfigurationPairs(jsonmap);
                List<Map<String,Object>> bots = (List<Map<String,Object>>) jsonmap.get("bots");

                bots.forEach(bot -> createBotPropertiesFile(bot, configurationPairs, botPropertiesPath));                
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> createConfigurationPairs(Map<String, Object> jsonMap) {
        Map<String, String> configurationPairs = new HashMap<>();
        String property;

        property = "botType";
        configurationPairs.put(property, jsonMap.get(property).toString());
        property = "dockerImage";
        configurationPairs.put(property, jsonMap.get(property).toString());
        botImage = jsonMap.get(property).toString();
        property = "keyToPublish";
        configurationPairs.put(property, jsonMap.get(property).toString());
        property = "orderToPublish";
        configurationPairs.put(property, jsonMap.get(property).toString());

        Map<String, Object> autonomy = getMap(jsonMap, "autonomy");
        String autonomyType = getString(autonomy, "type");
        configurationPairs.put("autonomy.type", autonomyType);

        if (autonomyType.equals("proactive")) {
            configurationPairs.put("autonomy.initialDelay", getString(autonomy, "initialDelay"));
            configurationPairs.put("autonomy.period", getString(autonomy, "period"));
        } else if (autonomyType.equals("reactive")) {
            configurationPairs.put("autonomy.order", getString(autonomy, "order"));
        } else {
            throw new IllegalArgumentException("Invalid autonomy type!");
        }

        Map<String, Object> rabbitOptions = getMap(jsonMap, "rabbitOptions");
        configurationPairs.put("rabbitOptions.queueByBot", getString(rabbitOptions, "queueByBot"));
        String mainQueue = getString(rabbitOptions, "mainQueue");
        configurationPairs.put("rabbitOptions.mainQueue", mainQueue);

        List<String> bindings = getList(rabbitOptions, "bindings");
        String content = String.join(",", bindings);
        configurationPairs.put("rabbitOptions.bindings", content);

        rabbitQueues.put(mainQueue, bindings);
        
        List<String> botRequiredPaths = getList(jsonMap, "requiredPaths");
        requiredPaths.addAll(botRequiredPaths);

        return configurationPairs;
    }

    private static String getString(Map<String, Object> map, String key) {
        return map.get(key).toString();
    }

    private static Map<String, Object> getMap(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    private static List<String> getList(Map<String, Object> map, String key) {
        return (List<String>) map.get(key);
    }

    private static void createBotPropertiesFile(Map<String,Object> bot, Map<String, String> configurationPairs, String botPropertiesPath){

        Map<String, String> specificBotProperties = new HashMap<>();
        Map<String, String> botConfigurationPairs = new HashMap<>(configurationPairs);
        String property;

        bot.keySet().stream()
                    .filter(key -> !key.equals("autonomy"))
                    .forEach(key -> specificBotProperties.put("bot." + key, bot.get(key).toString()));

        if (bot.containsKey("autonomy")) {
            Map<String, Object> autonomy = (Map<String, Object>) bot.get("autonomy");
            property = "initialDelay";
            if (autonomy.containsKey(property)) {
                botConfigurationPairs.put("autonomy." + property, autonomy.get(property).toString());
            }
            property = "period";
            if (autonomy.containsKey(property)) {
                botConfigurationPairs.put("autonomy." + property, autonomy.get(property).toString());
            }
        }

        String botId = bot.get("botId").toString();

        botIds.add(botId);
        botImages.add(botImage);
        botConfigurationPairs.keySet().forEach(key -> specificBotProperties.put(key, botConfigurationPairs.get(key)));

        Path filePath = Path.of(botPropertiesPath + botId + ".properties");
        DirectoryOperations.createDir(filePath);

        try {
            Files.write(filePath, specificBotProperties.entrySet().stream()
                                                                    .map(e -> e.getKey() + "=" + e.getValue())
                                                                    .collect(Collectors.toList()),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Bot properties file created successfully! Bot id: {}", botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createRabbitMQConfigFile(String rabbitExchange, String rabbitConfigurationPath) {

        List<String> content = new ArrayList<>();

        String initialContent = "{\r\n" +
                "\t\"users\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"admin\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"administrator\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"consumer\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"sender\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"vhosts\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"/\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"permissions\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"admin\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \".*\",\r\n" +
                "\t\t\t\"write\": \".*\",\r\n" +
                "\t\t\t\"read\": \".*\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"consumer\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \"\",\r\n" +
                "\t\t\t\"write\": \"\",\r\n" +
                "\t\t\t\"read\": \".*\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"sender\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \"\",\r\n" +
                "\t\t\t\"write\": \".*\",\r\n" +
                "\t\t\t\"read\": \"\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"exchanges\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"" + rabbitExchange + "\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"type\": \"topic\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"internal\": false,\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"queues\": [\r\n";

        String queueTemplate = "\t\t{\r\n" +
                "\t\t\t\"name\": \"%s\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"arguments\": {\r\n" +
                "\t\t\t\t\"x-message-ttl\": 3600000\r\n" +
                "\t\t\t}\r\n" +
                "\t\t}";

        String bindingPrefix = "\t\"bindings\": [\r\n";

        String bindingTemplate = "\t\t{\r\n" +
                "\t\t\t\"source\": \"%s\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"destination\": \"%s\",\r\n" +
                "\t\t\t\"destination_type\": \"queue\",\r\n" +
                "\t\t\t\"routing_key\": \"%s\",\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}";

        String collectorQueue = "\t\t{\r\n" +
                "\t\t\t\"name\": \"collector\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"arguments\": {\r\n" +
                "\t\t\t\t\"x-message-ttl\": 3600000\r\n" +
                "\t\t\t}\r\n" +
                "\t\t}";

        String collectorBinding = "\t\t{\r\n" +
                "\t\t\t\"source\": \"" + rabbitExchange + "\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"destination\": \"collector\",\r\n" +
                "\t\t\t\"destination_type\": \"queue\",\r\n" +
                "\t\t\t\"routing_key\": \"requestToCollector\",\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}";

        content.add(initialContent);

        for (String queue : rabbitQueues.keySet()) {
            String queueContent = String.format(queueTemplate, queue);
            queueContent += ",\r\n";
            content.add(queueContent);
        }

        content.add(collectorQueue);
        content.add("\r\n\t],");

        content.add(bindingPrefix);

        for (Map.Entry<String, List<String>> entry : rabbitQueues.entrySet()) {
            String queue = entry.getKey();
            List<String> bindings = entry.getValue();

            for (String binding : bindings) {
                String bindingContent = String.format(bindingTemplate, rabbitExchange, queue, binding);
                bindingContent += ",\r\n";
                content.add(bindingContent);
            }
        }

        content.add(collectorBinding);
        content.add("\r\n\t]\r\n}");

        Path filePath = Path.of(rabbitConfigurationPath);
        DirectoryOperations.createDir(filePath);
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("RabbitMQ configuration file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createRabbitMQConnectionFile(String rabbitConnectionPath, String rabbitmqUsername, String rabbitmqPassword, String rabbitmqHost, Integer rabbitmqPort, String rabbitmqExchange) {
        Path filePath = Path.of(rabbitConnectionPath);
        DirectoryOperations.createDir(filePath);

        try {
            Files.writeString(filePath, "{\n\t\"username\": \"" + rabbitmqUsername + "\",\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(filePath, "\t\"password\": \"" + rabbitmqPassword + "\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"virtualHost\": \"/\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"host\": \"" + rabbitmqHost + "\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"port\": " + rabbitmqPort + ",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"exchange\": \"" + rabbitmqExchange + "\"\n}", StandardOpenOption.APPEND);

            logger.info("RabbitMQ connection file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createDockerCompose(String dockerComposePath){ 

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

        String templateIntermediateContent = "  %s:\r\n" +
                "    depends_on:\r\n" +
                "      - rabbitmq\r\n" +
                "    restart: unless-stopped\r\n" +
                "    image: %s\r\n" +
                "    environment:\r\n" +
                "      - BOT_PROPERTY_FILE_PATH=/app/volume/src/main/resources/ConfigurationFiles/%s.properties\r\n" +
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

        for (int i = 0; i < botIds.size(); i++) {
            String intermediateContent = String.format(templateIntermediateContent, botIds.get(i), botImages.get(i), botIds.get(i));
            content.add(intermediateContent);
        }

        content.add(finalContent);

        Path filePath = Path.of(dockerComposePath);
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Docker compose file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createDummyDockerfile(String dummyDockerfilePath) {
        Path dockerfilePath = Path.of(dummyDockerfilePath);
        DirectoryOperations.createDir(dockerfilePath);

        try {
            Files.writeString(dockerfilePath, "FROM alpine:3.18.4\n\n", StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(dockerfilePath, "WORKDIR /app\n\n", StandardOpenOption.APPEND);
            Files.writeString(dockerfilePath, "RUN mkdir -p rabbitmq\n", StandardOpenOption.APPEND);

            Set<String> directoriesToCreate = createRequiredDirectories();
            directoriesToCreate.add("src/main/resources");

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

    public static void createBoticaDockerfile(String boticaDockerfilePath, String jarFileName) {
        
        Path scriptPath = Path.of(boticaDockerfilePath);
        DirectoryOperations.createDir(scriptPath);

        String auxJarFileName = jarFileName.contains(".jar") ? jarFileName : jarFileName + ".jar";

        try {
            Files.writeString(scriptPath, "FROM openjdk:11\n\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "WORKDIR /app/volume\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "COPY target/" + auxJarFileName + " /app/" + auxJarFileName + "\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "CMD [\"java\",\"-jar\",\"/app/" + auxJarFileName + "\"]", StandardOpenOption.APPEND);

            logger.info("BOTICA main script created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createInitVolumeScript(String initVolumeScriptPath) {

        String projectName = DirectoryOperations.getProjectName();

        Path scriptPath = Path.of(initVolumeScriptPath);
        DirectoryOperations.createDir(scriptPath);

        try {
            Files.writeString(scriptPath, "#!/bin/bash\n\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "if docker ps -a | grep -q dummy; then\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    echo \"Removing existing container...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    docker rm dummy\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "fi\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "if docker volume ls | grep -q " + projectName + "_botica-volume; then\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    echo \"Removing existing volume...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "    docker volume rm " + projectName + "_botica-volume\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "fi\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "docker container create --name dummy -v " + projectName + "_botica-volume:/app dummy\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker volume create --name " + projectName + "_botica-volume\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "docker cp ./pom.xml dummy:/app/pom.xml\n", StandardOpenOption.APPEND);
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

    public static void createMainScript(String mainScriptPath, String dummyDockerfilePath, String initVolumeScriptPath, String dockerComposePath, String boticaDockerfilePath, String boticaImageName) {
        
        Path scriptPath = Path.of(mainScriptPath);
        DirectoryOperations.createDir(scriptPath);

        String dummyDirectory = dummyDockerfilePath.contains("/") ? dummyDockerfilePath.substring(0, dummyDockerfilePath.lastIndexOf("/")) : ".";
        String boticaDirectory = boticaDockerfilePath.contains("/") ? boticaDockerfilePath.substring(0, boticaDockerfilePath.lastIndexOf("/")) : ".";


        try {
            Files.writeString(scriptPath, "#!/bin/bash\n\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "echo \"Building the image at " + dummyDockerfilePath + "...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker build -t dummy " + dummyDirectory + "\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo \"Building the image at ./Dockerfile...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker build -t " + boticaImageName + " " + boticaDirectory + "\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo \"Starting the data volume...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "chmod +x " + initVolumeScriptPath + "\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, initVolumeScriptPath + "\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo \"Running docker-compose...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker-compose -f " + dockerComposePath + " up -d\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo \"Script completed successfully.\"", StandardOpenOption.APPEND);

            logger.info("BOTICA main script created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
