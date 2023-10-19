package com.botica.examples;

import org.json.JSONArray;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;

import es.us.isa.restest.util.RESTestException;

public class TestCaseGeneration3 {

    public static final String PROPERTY_FILE_PATH="src/main/resources/Examples/Ex1_RandomGeneration/user_config.properties";		// Path to user properties file with configuration options
    public static final String BOT_ID = "bot_1";

    public static void main(String[] args) throws RESTestException {

        String propertiesPath = null;
        String botId = null;

        try {
            String json_path = "src/main/java/com/botica/examples/bots.json";
            String json_content = JSON.readFileAsString(json_path);
            JSONObject obj = new JSONObject(json_content);

            JSONArray testCaseGenerators = obj.getJSONArray("testCaseGenerator");

            JSONObject array_object = testCaseGenerators.getJSONObject(2);
            propertiesPath = array_object.getString("PROPERTY_FILE_PATH");
            botId = array_object.getString("BOT_ID");

            TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
            launcher.launchTestCases(propertiesPath, botId);

        } catch (Exception e) {
            System.out.println("Error reading server_config.json");
            e.printStackTrace();
        }
    }
}
