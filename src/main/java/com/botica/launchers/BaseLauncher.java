package com.botica.launchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.botica.RabbitMQManager;
import com.botica.utils.BotConfig;

/**
 * This class serves as the base launcher for bots and provides methods for
 * connecting to RabbitMQ and launching bot-related tasks.
 */
public class BaseLauncher {

    public static final Logger logger = Logger.getLogger(BaseLauncher.class.getName());
    private static final String BOT_ID_JSON_KEY = "botId";
    protected final RabbitMQManager messageSender = new RabbitMQManager();

    /**
     * Launches a bot with the provided configuration and parameters.
     * 
     * @param botData    The JSON object containing bot data.
     * @param botConfig  The BotConfig instance with bot configuration.
     * @param queueName  The name of the RabbitMQ queue.
     * @param bindingKey The binding key for the RabbitMQ queue.
     * @param autoDelete Whether the RabbitMQ queue should be auto-deleted.
     */
    protected void launchBot(JSONObject botData, BotConfig botConfig, String queueName, String bindingKey, boolean autoDelete) {
        
        String botId = botData.getString(BOT_ID_JSON_KEY);

        try {
            connectToRabbitMQ(queueName, bindingKey, botId, autoDelete);
            messageSender.receiveMessage(queueName, botData, botConfig);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Connects to RabbitMQ with the specified parameters.
     * 
     * @param queueName  The name of the RabbitMQ queue.
     * @param bindingKey The binding key for the RabbitMQ queue.
     * @param botId      The identifier of the bot.
     * @param autoDelete Whether the RabbitMQ queue should be auto-deleted.
     * @throws IOException
     * @throws TimeoutException
     */
    private void connectToRabbitMQ(String queueName, String bindingKey, String botId, boolean autoDelete) throws IOException, TimeoutException {
        List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
        messageSender.connect(queueName, bindingKey, queueOptions);
        logger.log(Level.INFO, "{0} connected to RabbitMQ", new Object[]{botId});
    }

}
