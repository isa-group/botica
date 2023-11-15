package com.botica.utils.bot;

import org.json.JSONObject;
import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.launchers.AbstractLauncher;
import com.botica.launchers.TestCaseExecutorLauncher;
import com.botica.launchers.TestReportGeneratorLauncher;

public class BotHandler {

    private static final String PROPERTY_FILE_PATH_JSON_KEY = "propertyFilePath";
    private static final String TEST_CASES_PATH = "testCasesPath";

    private BotHandler() {
    }

    public static void handleBotMessage(BotRabbitConfig botRabbitConfig, BotConfig botConfig, JSONObject messageData) {
        
        String botId = botConfig.getBotId();
        String botType = botRabbitConfig.getBotType();
        String keyToPublish = botRabbitConfig.getKeyToPublish();
        String orderToPublish = botRabbitConfig.getOrderToPublish();
        
        if (botType.equals("TestCaseGenerator")) {
            String propertyFilePath = botConfig.getPropertyFilePath();
            TestCaseGeneratorLauncher testCaseGeneratorLauncher = new TestCaseGeneratorLauncher(propertyFilePath, botId, keyToPublish, orderToPublish);
            testCaseGeneratorLauncher.executeBotActionAndSendMessage();
        } else if (botType.equals("TestCaseExecutor")) {
            String propertyFilePath = messageData.getString(PROPERTY_FILE_PATH_JSON_KEY);
            String testCasesPath = messageData.getString(TEST_CASES_PATH);
            TestCaseExecutorLauncher testCaseExecutorLauncher = new TestCaseExecutorLauncher(propertyFilePath, testCasesPath, keyToPublish, orderToPublish);
            testCaseExecutorLauncher.executeBotActionAndSendMessage();
        } else if (botType.equals("TestReporter")) {
            String propertyFilePath = messageData.getString(PROPERTY_FILE_PATH_JSON_KEY);
            String testCasesPath = messageData.getString(TEST_CASES_PATH);
            TestReportGeneratorLauncher testReportGeneratorLauncher = new TestReportGeneratorLauncher(propertyFilePath, testCasesPath, keyToPublish, orderToPublish);
            testReportGeneratorLauncher.executeBotActionAndSendMessage();
        }
    }

    public static AbstractLauncher handleLauncherType(String botType, String keyToPublish, String orderToPublish) {
        
        if (botType.equals("TestCaseGenerator")) {
            return new TestCaseGeneratorLauncher(keyToPublish, orderToPublish);
        } else if (botType.equals("TestCaseExecutor")) {
            return new TestCaseExecutorLauncher(keyToPublish, orderToPublish);
        } else if (botType.equals("TestReporter")) {
            return new TestReportGeneratorLauncher(keyToPublish, orderToPublish);
        }

        //TODO: Review
        return null;
    }
}
