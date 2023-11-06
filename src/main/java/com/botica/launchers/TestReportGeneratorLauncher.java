package com.botica.launchers;

import org.json.JSONObject;

import com.botica.bots.TestReportGenerator;
import com.botica.utils.BotConfig;

/**
 * This class is a launcher for generating test reports.
 */
public class TestReportGeneratorLauncher extends BaseLauncher {
    private static final String BOT_TYPE = "testReporter";
    private static final String BINDING_KEY = "testCasesExecuted";

    //TODO: Refactor method checking other launchers
    /**
     * Launches test report generator based on bot data provided, and sends and 
     * receives messages through RabbitMQ.
     *
     * @param botData           The JSON object containing bot data.
     * @param order             The order that identifies the message received.
     * @param keyToPublish      The binding key for publishing messages to RabbitMQ.
     * @param orderToPublish    The order to send in the message.
     */
    public void launchTestReportGenerator(JSONObject botData, String order, String keyToPublish, String orderToPublish) {
        
        BotConfig botConfig = new BotConfig(null, order, keyToPublish, orderToPublish, BOT_TYPE);
        String queueName = BOT_TYPE;
        launchBot(botData, botConfig, queueName, BINDING_KEY, false);
    }

    /**
     * Generates test reports.
     *
     * @param propertyFilePath The path to the property file.
     * @param botId            The test report generator identifier.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     */
    public static void generateTestReport(String propertyFilePath, String testCasesPath, String keyToPublish, String orderToPublish) {
    
        TestReportGenerator testReportGenerator = new TestReportGenerator(propertyFilePath, testCasesPath, keyToPublish, orderToPublish);

        testReportGenerator.executeBotActionAndSendMessage();
    
    }
}
