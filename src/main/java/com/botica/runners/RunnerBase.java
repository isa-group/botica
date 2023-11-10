package com.botica.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.AbstractLauncher;
import com.botica.utils.bot.BotConfig;
import com.botica.utils.java.DynamicJavaExecution;
import com.botica.utils.json.JSONUtils;

/**
 * The RunnerBase class serves as the base class for bot runners and provides
 * methods for loading bot data from a JSON file, launching bots, and checking
 * the validity of bot definitions.
 */
public class RunnerBase {

    private static final String BOTS_DEFINITION_FILE_NAME = "bots-definition.json";
    public static final String DEFAULT_BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/" + BOTS_DEFINITION_FILE_NAME;

    private static final String JSON_ARRAY = "bots";

    private static final String PATH = "src/main/java/com/botica/temp/";

    private static final Logger logger = LogManager.getLogger(RunnerBase.class);

    public RunnerBase() {
    }

    /**
     * Loads the bots data from the JSON file.
     * 
     * @param jsonObjectName The name of the JSON object to load.
     * @return A JSONObject containing the bots data.
     * @throws JSONException if there is an error reading the file or parsing the
     *                       JSON.
     */
    protected static JSONObject loadBotsDefinition(String jsonObjectName) throws JSONException {
        String jsonContent = JSONUtils.readFileAsString(DEFAULT_BOTS_DEFINITION_PATH);
        JSONObject obj = new JSONObject(jsonContent);
        return obj.getJSONObject(jsonObjectName);
    }

    protected static void launchBots(JSONObject botDefinition, String botType, AbstractLauncher launcher) {
        
        if (isValidBotDefinition(botDefinition)) {
        
            JSONArray bots = botDefinition.getJSONArray(JSON_ARRAY);
            String order = botDefinition.getString("order");
            String keyToPublish = botDefinition.getString("keyToPublish");
            String orderToPublish = botDefinition.getString("orderToPublish");

            JSONObject rabbitOptions = botDefinition.getJSONObject("rabbitOptions");

            for (int i = 0; i < bots.length(); i++) {
                JSONObject botData = bots.getJSONObject(i);
                
                createFileToLaunchBot(botData, order, keyToPublish, orderToPublish, rabbitOptions, botType, launcher);
                //TODO: REMOVE THIS LINE
                //connectBotsToRabbit(botData, order, keyToPublish, orderToPublish, rabbitOptions, botType, launcher);
            }
        } else {
            logger.error("Invalid bot definition");
        }
    }

    private static void createFileToLaunchBot(JSONObject botData, String order, String keyToPublish,
            String orderToPublish, JSONObject rabbitOptions, String botType, AbstractLauncher launcher) {

        String botId = botData.getString("botId");
        String code = generateBotDataCode(botData);
        code += generateOrderAndKeyCode(order, keyToPublish, orderToPublish);
        code += generateRabbitOptionsCode(rabbitOptions);
        code += generateBotTypeAndLauncherCode(botType, launcher);

        String javaCode = DynamicJavaExecution.generateJavaCode(botId, code);

        createDirectoryIfNotExists();
        String fileName = "BotLauncher_" + botId + ".java";
        String filePath = PATH + fileName;

        writeJavaCodeToFile(filePath, javaCode);
        DynamicJavaExecution.compileJavaFile(filePath);
    }

    private static String generateBotDataCode(JSONObject botData) {
        StringBuilder codeBuilder = new StringBuilder("\t\tJSONObject botData = new JSONObject();\n");
        for (String key : botData.keySet()) {
            Object value = botData.get(key);
            String valueString = (value instanceof String) ? "\"" + value + "\"" : value.toString();
            codeBuilder.append("\t\tbotData.put(\"").append(key).append("\", ").append(valueString).append(");\n");
        }
        return codeBuilder.toString();
    }

    private static String generateOrderAndKeyCode(String order, String keyToPublish, String orderToPublish) {
        return "\t\tString order = \"" + order + "\";\n" +
                "\t\tString keyToPublish = \"" + keyToPublish + "\";\n" +
                "\t\tString orderToPublish = \"" + orderToPublish + "\";\n";
    }

    private static String generateRabbitOptionsCode(JSONObject rabbitOptions) {
        StringBuilder codeBuilder = new StringBuilder("\t\tJSONObject rabbitOptions = new JSONObject();\n");
        for (String key : rabbitOptions.keySet()) {
            Object value = rabbitOptions.get(key);
            if (value instanceof String) {
                codeBuilder.append("\t\trabbitOptions.put(\"").append(key).append("\", \"").append(value)
                        .append("\");\n");
            } else if (value instanceof JSONArray) {
                codeBuilder.append(generateJsonArrayCode(key, (JSONArray) value));
            } else {
                codeBuilder.append("\t\trabbitOptions.put(\"").append(key).append("\", ").append(value).append(");\n");
            }
        }
        return codeBuilder.toString();
    }

    private static String generateJsonArrayCode(String key, JSONArray array) {
        StringBuilder codeBuilder = new StringBuilder("\t\tJSONArray ").append(key)
                .append("JSONArray = new JSONArray();\n");
        for (int i = 0; i < array.length(); i++) {
            codeBuilder.append("\t\t").append(key).append("JSONArray.put(\"").append(array.get(i)).append("\");\n");
        }
        return codeBuilder.append("\t\trabbitOptions.put(\"").append(key).append("\", ").append(key)
                .append("JSONArray);\n").toString();
    }

    private static String generateBotTypeAndLauncherCode(String botType, AbstractLauncher launcher) {
        String launcherName = launcher.getClass().getName();
        return "\t\tString botType = \"" + botType + "\";\n" +
                "\t\tRunnerBase.connectBotsToRabbit(botData, order, keyToPublish, orderToPublish, rabbitOptions, botType, new "
                + launcherName + "(keyToPublish, orderToPublish));\n";
    }

    private static void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(PATH));
        } catch (IOException e) {
            logger.error("Error creating directory: {}", e.getMessage());
        }
    }

    private static void writeJavaCodeToFile(String filePath, String javaCode) {
        try {
            DynamicJavaExecution.writeToFile(filePath, javaCode);
        } catch (IOException e) {
            logger.error("Error writing file: {}", e.getMessage());
        }
    }
    public static void connectBotsToRabbit(JSONObject botData, String order, String keyToPublish,
            String orderToPublish, JSONObject rabbitOptions, String botType, AbstractLauncher launcher) {
        
        String id = botData.getString("botId");
        String mainQueue = rabbitOptions.getString("mainQueue");
        boolean queueByBot = rabbitOptions.getBoolean("queueByBot");

        List<String> bindings = rabbitOptions.getJSONArray("bindings").toList()
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        BotConfig botConfig = new BotConfig(id, order, keyToPublish, orderToPublish, botType);
        if (queueByBot) {
            String bindingKey = botType + "." + id;
            List<String> bindingKeys = new ArrayList<>();
            bindingKeys.add(bindingKey);
            
            launcher.launchBot(botData, botConfig, id, bindingKeys, true);
        } else {
            launcher.launchBot(botData, botConfig, mainQueue, bindings, false);
        }
    }

    private static boolean isValidBotDefinition(JSONObject botDefinition) {
        return botDefinition.has(JSON_ARRAY) &&
                botDefinition.has("order") &&
                botDefinition.has("keyToPublish") &&
                botDefinition.has("orderToPublish") &&
                botDefinition.has("rabbitOptions");
    }

}
