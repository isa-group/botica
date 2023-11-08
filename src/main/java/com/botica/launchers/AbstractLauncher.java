package com.botica.launchers;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;

import com.botica.RabbitMQManager;
import com.botica.utils.bot.BotConfig;
import com.botica.utils.logging.ExceptionUtils;

/**
 * This class serves as the base launcher for bots and provides methods for
 * connecting to RabbitMQ and launching bot-related tasks.
 */
public abstract class AbstractLauncher {

    protected String keyToPublish;
    protected String orderToPublish;
    protected final RabbitMQManager messageSender = new RabbitMQManager();

    protected static final Logger logger = LogManager.getLogger(AbstractLauncher.class);
    private static final String BOT_ID_JSON_KEY = "botId";

    protected AbstractLauncher(String keyToPublish, String orderToPublish) {
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
    }

    /**
     * Launches a bot with the provided configuration and parameters.
     * 
     * @param botData    The JSON object containing bot data.
     * @param botConfig  The BotConfig instance with bot configuration.
     * @param queueName  The name of the RabbitMQ queue.
     * @param bindingKey The binding key for the RabbitMQ queue.
     * @param autoDelete Whether the RabbitMQ queue should be auto-deleted.
     */
    public void launchBot(JSONObject botData, BotConfig botConfig, String queueName, List<String> bindingKeys, boolean autoDelete) {
        
        String botId = botData.getString(BOT_ID_JSON_KEY);

        try {
            List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
            messageSender.connect(queueName, bindingKeys, queueOptions, botId);
            messageSender.receiveMessage(queueName, botData, botConfig);
        } catch (Exception e) {
            logger.error("Error launching bot: {}", botId, e);
        }
    }
    
    protected abstract void botAction();

    protected abstract JSONObject createMessage();

    public void executeBotActionAndSendMessage() {
        botAction();
        try{
            messageSender.sendMessageToExchange(this.keyToPublish, createMessage().toString());
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

}
