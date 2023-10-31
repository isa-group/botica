package com.botica.generators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.interfaces.TestCaseExecutorInterface;
import com.botica.utils.RabbitCommunicator;

import es.us.isa.restest.runners.RESTestExecutor;

public class TestCaseExecutor implements TestCaseExecutorInterface {

    private RESTestExecutor executor;
    private String keyToPublish;
    private RabbitCommunicator rabbitCommunicator;

    private static final Logger logger = LogManager.getLogger(TestCaseExecutor.class);

    public TestCaseExecutor(RESTestExecutor executor, String keyToPublish) {
        this.executor = executor;
        this.keyToPublish = keyToPublish;
        this.rabbitCommunicator = new RabbitCommunicator(this.keyToPublish, logger);
    }

    @Override
    public void execute() {
        executor.execute();

        String message = "Test case execution finished";
        rabbitCommunicator.sendMessage(message);

    }
    
}
