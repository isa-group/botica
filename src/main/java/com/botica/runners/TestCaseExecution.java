package com.botica.runners;

import org.json.JSONException;
import org.json.JSONObject;

import com.botica.launchers.TestCaseExecutorLauncher;

public class TestCaseExecution extends RunnerBase{

    private static final String JSON_OBJECT = "testCaseExecutors";
    private static final String BOT_TYPE = JSON_OBJECT.substring(0, JSON_OBJECT.length() - 1);

    public static void main(String[] args) {

        try {
            JSONObject botDefinition = loadBotsDefinition(JSON_OBJECT);
            String keyToPublish = botDefinition.getString("keyToPublish");
            String orderToPublish = botDefinition.getString("orderToPublish");
            launchBots(botDefinition, BOT_TYPE, new TestCaseExecutorLauncher(keyToPublish, orderToPublish));
        } catch (JSONException e) {
            throw new JSONException("Error reading file: " + DEFAULT_BOTS_DEFINITION_PATH);
        }
    }
}
