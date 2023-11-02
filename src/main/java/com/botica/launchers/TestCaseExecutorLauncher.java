package com.botica.launchers;

import static es.us.isa.restest.util.FileManager.createDir;
import static es.us.isa.restest.util.FileManager.deleteDir;

import org.json.JSONObject;

import com.botica.generators.TestCaseExecutor;
import com.botica.utils.BotConfig;
import com.botica.utils.RESTestUtil;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is a launcher for executing test cases based on bot data and
 * generating test cases of specified types.
 */
public class TestCaseExecutorLauncher extends BaseLauncher{

    private static final String BOT_TYPE = "testCaseExecutor";
    private static final String BINDING_KEY = "testCasesGenerated";

    /**
     * Launches test case executor based on bot data provided, and sends and 
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
     * Executes test cases based.
     *
     * @param propertyFilePath The path to the property file for test case
     *                         executor.
     * @param testCasesPath    The path to the test cases generated.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     * @param orderToPublish   The order to send in the message.
     */
    public static void executeTestCases(String propertyFilePath, String testCasesPath, String keyToPublish, String orderToPublish) {

        RESTestExecutor executor = new RESTestExecutor(propertyFilePath);

        TestCaseExecutor testExecutor = new TestCaseExecutor(executor, propertyFilePath, testCasesPath, keyToPublish, orderToPublish);

        // TODO: Check if is correct
        // Create directories to store test data extracted from the execution
        String experimentName = RESTestUtil.readProperty(propertyFilePath, "experiment.name");

        String testDataDir = RESTestUtil.readProperty(propertyFilePath, "data.tests.dir") + "/" + experimentName;
        String coverageDataDir = RESTestUtil.readProperty(propertyFilePath, "data.coverage.dir") + "/" + experimentName;

        //TODO: Check if "deletepreviousresults" is not null
        if (Boolean.parseBoolean(RESTestUtil.readProperty(propertyFilePath, "deletepreviousresults"))) {
            deleteDir(testDataDir);
            deleteDir(coverageDataDir);
        }

        createDir(testDataDir);
        createDir(coverageDataDir);
        //

        testExecutor.execute();
    }
}
