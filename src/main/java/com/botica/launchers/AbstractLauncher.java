package com.botica.launchers;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;

import com.botica.RabbitMQManager;
import com.botica.utils.bot.BotConfig;
import com.botica.utils.bot.BotRabbitConfig;
import com.botica.utils.logging.ExceptionUtils;

/**
 * This class serves as the base launcher for bots and provides methods for
 * connecting to RabbitMQ and launching bot-related tasks.
 */
public abstract class AbstractLauncher {

    protected static final Logger logger = LogManager.getLogger(AbstractLauncher.class);

    protected String keyToPublish;                                          // The key to publish to RabbitMQ.
    protected String orderToPublish;                                        // The order to publish to RabbitMQ.
    protected final RabbitMQManager messageSender = new RabbitMQManager();  // The RabbitMQManager instance.

    /**
     * Constructor for AbstractLauncher.
     * 
     * @param keyToPublish
     * @param orderToPublish
     */
    protected AbstractLauncher(String keyToPublish, String orderToPublish) {
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
    }

    /**
     * Launches a bot with the provided configuration and parameters.
     * 
     * @param botConfig         The BotConfig instance that contains the bot-specific configuration.
     * @param botRabbitConfig   The BotRabbitConfig instance that contains the bot's RabbitMQ configuration.
     * @param queueName         The name of the RabbitMQ queue.
     * @param bindingKey        The binding key for the RabbitMQ queue.
     * @param autoDelete        Whether the RabbitMQ queue should be auto-deleted.
     */
    public void launchBot(BotConfig botConfig, BotRabbitConfig botRabbitConfig, String queueName, List<String> bindingKeys, boolean autoDelete) {
        
        String botId = botConfig.getBotId();

        try {
            List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
            this.messageSender.connect(queueName, bindingKeys, queueOptions, botId);
            this.messageSender.receiveMessage(queueName, botConfig, botRabbitConfig);
        } catch (Exception e) {
            logger.error("Error launching bot: {}", botId, e);
        }
    }
    
    // Executes bot action.
    protected abstract void botAction();

    // Creates message to send to RabbitMQ.
    protected abstract JSONObject createMessage();

    // Executes bot action and sends message to RabbitMQ.
    public void executeBotActionAndSendMessage() {
        botAction();
        try{
            this.messageSender.sendMessageToExchange(this.keyToPublish, createMessage().toString());
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

}
