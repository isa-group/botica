package com.botica.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.AbstractLauncher;
import com.botica.utils.BotConfig;
import com.botica.utils.JSON;

/**
 * The RunnerBase class serves as the base class for bot runners and provides
 * methods for loading bot data from a JSON file, launching bots, and checking
 * the validity of bot definitions.
 */
public class RunnerBase {

    private static final String BOTS_DEFINITION_FILE_NAME = "bots-definition.json";
    public static final String DEFAULT_BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/" + BOTS_DEFINITION_FILE_NAME;

    protected static final String JSON_ARRAY = "bots";

    private static final Logger logger = LogManager.getLogger(RunnerBase.class);

    /**
     * Loads the bots data from the JSON file.
     * 
     * @param jsonObjectName The name of the JSON object to load.
     * @return A JSONObject containing the bots data.
     * @throws JSONException if there is an error reading the file or parsing the
     *                       JSON.
     */
    protected static JSONObject loadBotsDefinition(String jsonObjectName) throws JSONException {
        String jsonContent = JSON.readFileAsString(DEFAULT_BOTS_DEFINITION_PATH);
        JSONObject obj = new JSONObject(jsonContent);
        return obj.getJSONObject(jsonObjectName);
    }

    protected static void launchBots(JSONObject botDefinition, String botType, AbstractLauncher launcher) { //TODO: DOCKERIZE
        
        if (isValidBotDefinition(botDefinition)) {
        
            JSONArray bots = botDefinition.getJSONArray(JSON_ARRAY);
            String order = botDefinition.getString("order");
            String keyToPublish = botDefinition.getString("keyToPublish");
            String orderToPublish = botDefinition.getString("orderToPublish");

            JSONObject rabbitOptions = botDefinition.getJSONObject("rabbitOptions");

            for (int i = 0; i < bots.length(); i++) {
                JSONObject botData = bots.getJSONObject(i);
                connectBotsToRabbit(botData, order, keyToPublish, orderToPublish, rabbitOptions, botType, launcher);
            }
        } else {
            logger.error("Invalid bot definition");
        }
    }

    protected static void connectBotsToRabbit(JSONObject botData, String order, String keyToPublish,
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

    protected interface RunnerInterface {
        void botsInformation(JSONObject botData, String order, String keyToPublish, String orderToPublis, JSONObject rabbitOptions);
    }
}
