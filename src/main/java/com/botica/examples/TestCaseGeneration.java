package com.botica.examples;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;
public class TestCaseGeneration {

    private static final String BOTS_DEFINITION_FILE_NAME = "bots-definition.json";
    private static final String DEFAULT_BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/" + BOTS_DEFINITION_FILE_NAME;
    
    private static final String JSON_ARRAY = "testCaseGenerators";
    private static final String PROPERTY_FILE_PATH_JSON_KEY = "propertyFilePath";
    private static final String BOT_ID_JSON_KEY = "botId";
    private static final String IS_PERSISTENT_JSON_KEY = "isPersistent";
    
    public static void main(String[] args) {

        try {
            JSONArray testCaseGenerators = loadBotsDefinition();
            launchTestCasesGenerator(testCaseGenerators);
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }

    private static JSONArray loadBotsDefinition() throws JSONException {
        String jsonContent = JSON.readFileAsString(DEFAULT_BOTS_DEFINITION_PATH);
        JSONObject obj = new JSONObject(jsonContent);
        return obj.getJSONArray(JSON_ARRAY);
    }

    private static void launchTestCasesGenerator(JSONArray testCaseGenerators) {
        for (int i = 0; i < testCaseGenerators.length(); i++) {
            JSONObject arrayObject = testCaseGenerators.getJSONObject(i);
            String propertiesPath = arrayObject.getString(PROPERTY_FILE_PATH_JSON_KEY);
            String botId = arrayObject.getString(BOT_ID_JSON_KEY);
            boolean isPersistent = arrayObject.getBoolean(IS_PERSISTENT_JSON_KEY);

            TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
            launcher.launchTestCases(propertiesPath, botId, isPersistent);
        }
    }

}
