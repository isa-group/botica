package com.botica.launchers;

import org.json.JSONObject;

import com.botica.generators.TestCaseExecutor;
import com.botica.utils.BotConfig;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is a launcher for executing test cases based on bot data and
 * generating test cases of specified types.
 */
public class TestCaseExecutorLauncher extends BaseLauncher{

    private static final String BOT_TYPE = "testCaseExecutor";
    private static final String BINDING_KEY = "testCasesGenerated";

    /**
     * Launches test case generator based on bot data provided, and sends and 
     * receives messages through RabbitMQ.
     *
     * @param botData           The JSON object containing bot data.
     * @param order             The order that identifies the message received.
     * @param keyToPublish      The binding key for publishing messages to RabbitMQ.
     * @param orderToPublish    The order to send in the message.
     */
    public void launchTestExecutor(JSONObject botData, String order, String keyToPublish, String orderToPublish) {
        
        BotConfig botConfig = new BotConfig(null, order, keyToPublish, orderToPublish, BOT_TYPE);
        String queueName = BOT_TYPE;
        launchBot(botData, botConfig, queueName, BINDING_KEY, false);
    }

    /**
     * Generates test cases based on the specified generator type.
     *
     * @param propertyFilePath The path to the property file for test case
     *                         generator.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     */
    public static void executeTestCases(String propertyFilePath, String keyToPublish) {

        RESTestExecutor executor = new RESTestExecutor(propertyFilePath);

        TestCaseExecutor testExecutor = new TestCaseExecutor(executor, keyToPublish);

        testExecutor.execute();
    }
}
