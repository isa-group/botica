package com.botica.runners;

import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestReportGeneratorLauncher;

public class TestReportGeneration extends RunnerBase {
    
    private static final String JSON_OBJECT = "testReporters";

    public static void main(String[] args) {

        try {
            JSONObject testReporters = loadBotsDefinition(JSON_OBJECT);

            launchBots(testReporters, (botData, o, k, op) -> {
                TestReportGeneratorLauncher launcher = new TestReportGeneratorLauncher(k, op);
                launcher.launchTestReportGenerator(botData, o);
            });
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }
}
