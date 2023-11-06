package com.botica.generators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.botica.interfaces.TestCaseExecutorInterface;
import com.botica.utils.RESTestUtil;
import com.botica.utils.RabbitCommunicator;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is responsible for executing test cases and sending messages
 * through RabbitMQ with relevant information about the execution status.
 */
public class TestCaseExecutor implements TestCaseExecutorInterface {

    private RESTestExecutor executor;
    private String propertyFilePath;
    private String testCasesPath;
    private String keyToPublish;
    private String orderToPublish;
    private RabbitCommunicator rabbitCommunicator;

    private static final Logger logger = LogManager.getLogger(TestCaseExecutor.class);

    /**
     * Constructor for the TestCaseExecutor class.
     * 
     * @param executor          The RESTestExecutor class used to execute test cases.
     * @param propertyFilePath  The path to the property file.
     * @param testCasesPath     The path to the test cases generated.
     * @param keyToPublish      The binding key to publish a message to the RabbitMQ
     *                          broker.
     * @param orderToPublish    The order to publish in the message.
     */
    public TestCaseExecutor(RESTestExecutor executor, String propertyFilePath, String testCasesPath, String keyToPublish, String orderToPublish) {
        this.executor = executor;
        this.propertyFilePath = propertyFilePath;
        this.testCasesPath = testCasesPath;
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
        this.rabbitCommunicator = new RabbitCommunicator(this.keyToPublish, logger);
    }

    /**
     * Executes the test cases and sends a completion message through RabbitMQ.
     */
    @Override
    public void execute() {
        
        String allureResultsDirPath = RESTestUtil.readProperty(propertyFilePath, "allure.results.dir");
        String experimentName = RESTestUtil.readProperty(propertyFilePath, "experiment.name");
        System.setProperty("allure.results.directory", allureResultsDirPath + "/" + experimentName);
        executor.execute();

        JSONObject message = new JSONObject();
        message.put("order", orderToPublish);
        message.put("propertyFilePath", propertyFilePath);
        message.put("testCasesPath", testCasesPath);

        rabbitCommunicator.sendMessage(message.toString());

    }
    
}
