package com.botica.runners;

import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseGeneratorLauncher;

public class TestCaseGeneration extends RunnerBase{

    private static final String JSON_OBJECT = "testCaseGenerators";

    public static void main(String[] args) {

        try {
            JSONObject testCaseGenerators = loadBotsDefinition(JSON_OBJECT);

            launchBots(testCaseGenerators, (botData, o, k, op) -> {
                TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher(k, op);
                launcher.launchTestGenerator(botData, o);
            });
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }
}
