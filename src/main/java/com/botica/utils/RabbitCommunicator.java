package com.botica.utils;

import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Logger;
import com.botica.RabbitMQManager;

public class RabbitCommunicator {

    private RabbitMQManager messageSender = new RabbitMQManager();
    private String keyToPublish;
    private Logger logger;

    public RabbitCommunicator(String keyToPublish, Logger logger) {
        this.keyToPublish = keyToPublish;
        this.logger = logger;
    }

    /**
     * Sends a message through RabbitMQ with relevant information.
     * 
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        try {
            List<Boolean> queueOptions = Arrays.asList(true, false, true);
            messageSender.connect("", null, queueOptions);
            messageSender.sendMessageToExchange(keyToPublish, message);
            logger.info("Message sent to RabbitMQ: {}", message);
            messageSender.close();
        } catch (Exception e) {
            Utils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }
}
