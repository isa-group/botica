package com.botica.utils.bot;

import java.util.Properties;

import org.json.JSONObject;
import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.launchers.AbstractLauncher;
import com.botica.launchers.TestCaseExecutorLauncher;
import com.botica.launchers.TestReportGeneratorLauncher;

/**
 * This class handles bot messages and creates bot launchers.
 */
public class BotHandler {

    private static final String PROPERTY_FILE_PATH_JSON_KEY = "propertyFilePath";
    private static final String TEST_CASES_PATH = "testCasesPath";

    private BotHandler() {
    }

    /**
     * Handles a bot message.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botConfig       The bot-specific configuration.
     * @param messageData     The message data.
     */
    public static void handleBotMessage(BotRabbitConfig botRabbitConfig, BotConfig botConfig, JSONObject messageData) {
        
        String botId = botConfig.getBotId();
        String botType = botRabbitConfig.getBotType();
        String keyToPublish = botRabbitConfig.getKeyToPublish();
        String orderToPublish = botRabbitConfig.getOrderToPublish();
        
        if (botType.equals("TestCaseGenerator")) {
            Properties botProperties = botConfig.getBotProperties();
            String propertyFilePath = botProperties.getProperty("bot.propertyFilePath");
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

    /**
     * Handles a bot data to create a specific launcher.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botConfig       The bot-specific configuration.
     * @param messageData     The message data.
     */
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
