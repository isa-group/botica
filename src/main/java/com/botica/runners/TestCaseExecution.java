package com.botica.runners;

import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseExecutorLauncher;

public class TestCaseExecution extends RunnerBase{

    private static final String JSON_OBJECT = "testCaseExecutors";

    public static void main(String[] args) {

        try {
            JSONObject testCaseExecutors = loadBotsDefinition(JSON_OBJECT);
            launchBots(testCaseExecutors, (botData, o, k, op) -> {
                TestCaseExecutorLauncher launcher = new TestCaseExecutorLauncher(k, op);
                launcher.launchTestExecutor(botData, o);
            });
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }
}
