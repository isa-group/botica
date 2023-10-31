package com.botica.runners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.utils.JSON;

public class RunnerBase {
    private static final String BOTS_DEFINITION_FILE_NAME = "bots-definition.json";
    public static final String DEFAULT_BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/" + BOTS_DEFINITION_FILE_NAME;

    protected static final String JSON_ARRAY = "bots";

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

    protected static void launchBots(JSONObject botDefinition, LauncherInterface launcher) {
        
        //TODO: Add validation to check if the JSON object contains the required keys.
        
        JSONArray bots = botDefinition.getJSONArray(JSON_ARRAY);
        String order = botDefinition.getString("order");
        String keyToPublish = botDefinition.getString("keyToPublish");
        String orderToPublish = botDefinition.getString("orderToPublish");

        for (int i = 0; i < bots.length(); i++) {
            JSONObject botData = bots.getJSONObject(i);
            launcher.launchBot(botData, order, keyToPublish, orderToPublish);
        }
    }

    protected interface LauncherInterface {
        void launchBot(JSONObject botData, String order, String keyToPublish, String orderToPublish);
    }
}
