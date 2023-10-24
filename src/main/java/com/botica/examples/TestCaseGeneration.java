package com.botica.examples;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;

/**
 * This class is responsible for loading a configuration file, extracting information about test case generators and launching them.
 * It reads a JSON configuration file, processes the information, and launches test case generators based on the configuration.
 * The configuration file should specify the test case generators, their specific order, and the binding key to publish.
 * The generated test cases are launched using the 'TestCaseGeneratorLauncher'.
 */
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

    /**
     * Loads the bots data from the JSON file.
     * 
     * @return A JSONObject containing the bots data.
     * @throws JSONException if there is an error reading the file or parsing the
     *                       JSON.
     */
    private static JSONObject loadBotsDefinition() throws JSONException {
        String jsonContent = JSON.readFileAsString(DEFAULT_BOTS_DEFINITION_PATH);
        JSONObject obj = new JSONObject(jsonContent);
        return obj.getJSONObject(JSON_OBJECT);
    }

    /**
     * Launches test case generators based on the bots data.
     * 
     * @param testCaseGenerators The JSONObject containing the bots data.
     */
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
