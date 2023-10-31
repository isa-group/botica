package com.botica.launchers;

import org.json.JSONObject;

import com.botica.generators.TestCaseExecutor;
import es.us.isa.restest.runners.RESTestExecutor;
public class TestCaseExecutorLauncher extends BaseLauncher{

    private static final String BOT_TYPE = "testCaseExecutor";
    private static final String BINDING_KEY = "testCasesGenerated";

    /**
     * Launches test case generator based on bot data provided, and sends and 
     * receives messages through RabbitMQ.
     *
     * @param botData      The JSON object containing bot data.
     * @param order        The order that identifies the message sent.
     * @param keyToPublish The binding key for publishing messages to RabbitMQ.
     */
    public void launchTestExecutor(JSONObject botData, String order, String keyToPublish, String orderToPublish) {
        
        String queueName = BOT_TYPE;
        launchBot(botData, queueName, BINDING_KEY, order, keyToPublish, orderToPublish, false, BOT_TYPE);
    }

    /**
     * Generates test cases based on the specified generator type.
     *
     * @param propertyFilePath The path to the property file for test case
     *                         generator.
     * @param botId            The test case generator identifier.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     */
    public static void executeTestCases(String propertyFilePath, String keyToPublish) {

        RESTestExecutor executor = new RESTestExecutor(propertyFilePath);

        TestCaseExecutor testExecutor = new TestCaseExecutor(executor, keyToPublish);

        testExecutor.execute();
    }
}
