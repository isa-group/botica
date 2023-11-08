package com.botica.examples;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.RabbitMQManager;
import com.botica.utils.logging.ExceptionUtils;

public class SendOrderToBot {

    private static final Logger logger = LogManager.getLogger(SendOrderToBot.class);
    private static final String KEY_TO_PUBLISH = "testCaseGenerator.gen_4";
    private static final String MESSAGE = "{\"order\": \"generateTestCases\"}";

    public static void main(String[] argv) throws Exception {

        RabbitMQManager messageSender = new RabbitMQManager();

        try{
            messageSender.sendMessageToExchange(KEY_TO_PUBLISH, MESSAGE);
            logger.info("Message sent to RabbitMQ: {}", MESSAGE);
            messageSender.close();
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }
}
