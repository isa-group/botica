package com.botica.examples;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.RabbitMQManager;
import com.botica.utils.Utils;

public class ReceiveMessageFromBot {

    private static final Logger logger = LogManager.getLogger(ReceiveMessageFromBot.class);
    private static final String BINDING_KEY = "testCasesGenerated";

    public static void main(String[] argv) throws Exception {

        RabbitMQManager messageSender = new RabbitMQManager();

        try{
            List<Boolean> queueOptions = Arrays.asList(true, false, true);
            String queueName = messageSender.connect("", BINDING_KEY, queueOptions);
            messageSender.receiveMessage(queueName);
        } catch (Exception e) {
            Utils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }
}
