package com.botica.bots;
import org.json.JSONObject;

import com.botica.utils.RESTestUtil;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is responsible for executing test cases and sending messages
 * through RabbitMQ with relevant information about the execution status.
 */
public class TestCaseExecutor extends AbstractBot {

    private RESTestExecutor executor;
    private String propertyFilePath;
    private String testCasesPath;

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
        super(keyToPublish, orderToPublish);
        this.executor = executor;
        this.propertyFilePath = propertyFilePath;
        this.testCasesPath = testCasesPath;
    }

    @Override
    protected void botAction() {
        String allureResultsDirPath = RESTestUtil.readProperty(propertyFilePath, "allure.results.dir");
        String experimentName = RESTestUtil.readProperty(propertyFilePath, "experiment.name");
        System.setProperty("allure.results.directory", allureResultsDirPath + "/" + experimentName);
        executor.execute();
    }

    @Override
    protected JSONObject createMessage(){
        JSONObject message = new JSONObject();
        message.put("order", orderToPublish);
        message.put("propertyFilePath", propertyFilePath);
        message.put("testCasesPath", testCasesPath);

        return message;
    }
    
}
