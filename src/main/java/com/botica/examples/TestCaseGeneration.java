package com.botica.examples;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;
public class TestCaseGeneration {

    private static final String BOTS_DEFINITION_FILE_NAME = "bots-definition.json";
    private static final String DEFAULT_BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/" + BOTS_DEFINITION_FILE_NAME;
    
    private static final String JSON_OBJECT = "testCaseGenerators";
    private static final String JSON_ARRAY = "bots";
    
    public static void main(String[] args) {

        try {
            JSONObject testCaseGenerators = loadBotsDefinition();
            launchTestCaseGenerators(testCaseGenerators);
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }

    private static JSONObject loadBotsDefinition() throws JSONException {
        String jsonContent = JSON.readFileAsString(DEFAULT_BOTS_DEFINITION_PATH);
        JSONObject obj = new JSONObject(jsonContent);
        return obj.getJSONObject(JSON_OBJECT);
    }

    private static void launchTestCaseGenerators(JSONObject testCaseGenerators) {

        JSONArray bots = testCaseGenerators.getJSONArray(JSON_ARRAY);
        String order = testCaseGenerators.getString("order");
        String keyToPublish = testCaseGenerators.getString("keyToPublish");

        for (int i = 0; i < bots.length(); i++) {
            JSONObject botData = bots.getJSONObject(i);

            TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
            launcher.launchTestCases(botData, order, keyToPublish);
        }
    }

}
