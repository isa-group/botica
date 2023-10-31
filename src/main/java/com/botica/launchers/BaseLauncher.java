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

public class BaseLauncher {

    public static final Logger logger = Logger.getLogger(BaseLauncher.class.getName());
    private static final String BOT_ID_JSON_KEY = "botId";
    protected final RabbitMQManager messageSender = new RabbitMQManager();

    protected void launchBot(JSONObject botData, BotConfig botConfig, String queueName, String bindingKey, boolean autoDelete) {
        
        String botId = botData.getString(BOT_ID_JSON_KEY);

        try {
            connectToRabbitMQ(queueName, bindingKey, botId, autoDelete);
            messageSender.receiveMessage(queueName, botData, botConfig.getBotType(), botConfig.getOrder(), botConfig.getKeyToPublish(), botConfig.getOrderToPublish());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void connectToRabbitMQ(String queueName, String bindingKey, String botId, boolean autoDelete) throws IOException, TimeoutException {
        List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
        messageSender.connect(queueName, bindingKey, queueOptions);
        logger.log(Level.INFO, "{0} connected to RabbitMQ", new Object[]{botId});
    }

}
