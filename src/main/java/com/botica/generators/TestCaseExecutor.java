package com.botica.generators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.interfaces.TestCaseExecutorInterface;
import com.botica.utils.RabbitCommunicator;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is responsible for executing test cases and sending messages
 * through RabbitMQ with relevant information about the execution status.
 */
public class TestCaseExecutor implements TestCaseExecutorInterface {

    private RESTestExecutor executor;
    private String keyToPublish;
    private RabbitCommunicator rabbitCommunicator;

    private static final Logger logger = LogManager.getLogger(TestCaseExecutor.class);

    /**
     * Constructor for the TestCaseExecutor class.
     * 
     * @param executor     The RESTestExecutor class used to execute test cases.
     * @param keyToPublish The binding key to publish a message to the RabbitMQ
     *                     broker.
     */
    public TestCaseExecutor(RESTestExecutor executor, String keyToPublish) {
        this.executor = executor;
        this.keyToPublish = keyToPublish;
        this.rabbitCommunicator = new RabbitCommunicator(this.keyToPublish, logger);
    }

    /**
     * Executes the test cases and sends a completion message through RabbitMQ.
     */
    @Override
    public void execute() {
        executor.execute();

        String message = "Test case execution finished";
        rabbitCommunicator.sendMessage(message);

    }
    
}
