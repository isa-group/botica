package com.botica.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;
public class TestCaseGeneration {

    private static Logger logger = LogManager.getLogger(TestCaseGeneration.class);
    public static void main(String[] args) {

        String propertiesPath = null;
        String botId = null;
        boolean isPersistent = false;

        try {
            String jsonPath = "src/main/java/com/botica/bots/bots-definition.json";
            String jsonContent = JSON.readFileAsString(jsonPath);
            JSONObject obj = new JSONObject(jsonContent);

            JSONArray testCaseGenerators = obj.getJSONArray("testCaseGenerators");

            for(int i = 0; i < testCaseGenerators.length(); i++){
                JSONObject arrayObject = testCaseGenerators.getJSONObject(i);
                propertiesPath = arrayObject.getString("propertyFilePath");
                botId = arrayObject.getString("botId");
                isPersistent = arrayObject.getBoolean("isPersistent");

                TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
                launcher.launchTestCases(propertiesPath, botId, isPersistent);
            }    
        } catch (Exception e) {
            logger.error("Error reading bots-definition.json");
            e.printStackTrace();
        }
      
    }
}
