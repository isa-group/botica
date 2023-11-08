package com.botica.utils.bot;

import org.json.JSONObject;
import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.launchers.TestCaseExecutorLauncher;
import com.botica.launchers.TestReportGeneratorLauncher;

public class BotHandler {

    private static final String PROPERTY_FILE_PATH_JSON_KEY = "propertyFilePath";
    private static final String TEST_CASES_PATH = "testCasesPath";

    private BotHandler() {
    }

    public static void handleBotMessage(BotConfig botConfig, JSONObject botData, JSONObject messageData) {
        
        String botId = botConfig.getBotId();
        String keyToPublish = botConfig.getKeyToPublish();
        String orderToPublish = botConfig.getOrderToPublish();
        String botType = botConfig.getBotType();
        
        if (botType.equals("testCaseGenerator")) {
            String propertyFilePath = botData.getString(PROPERTY_FILE_PATH_JSON_KEY);
            TestCaseGeneratorLauncher testCaseGeneratorLauncher = new TestCaseGeneratorLauncher(propertyFilePath, botId, keyToPublish, orderToPublish);
            testCaseGeneratorLauncher.executeBotActionAndSendMessage();
        } else if (botType.equals("testCaseExecutor")) {
            String propertyFilePath = messageData.getString(PROPERTY_FILE_PATH_JSON_KEY);
            String testCasesPath = messageData.getString(TEST_CASES_PATH);
            TestCaseExecutorLauncher testCaseExecutorLauncher = new TestCaseExecutorLauncher(propertyFilePath, testCasesPath, keyToPublish, orderToPublish);
            testCaseExecutorLauncher.executeBotActionAndSendMessage();
        } else if (botType.equals("testReporter")) {
            String propertyFilePath = messageData.getString(PROPERTY_FILE_PATH_JSON_KEY);
            String testCasesPath = messageData.getString(TEST_CASES_PATH);
            TestReportGeneratorLauncher testReportGeneratorLauncher = new TestReportGeneratorLauncher(propertyFilePath, testCasesPath, keyToPublish, orderToPublish);
            testReportGeneratorLauncher.executeBotActionAndSendMessage();
        }
    }
}
