package com.botica.launchers;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;

import com.botica.RabbitMQManager;
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
    protected Properties botProperties;                                     // The bot properties.
    protected JSONObject messageData;                                       // The message data.
    protected final RabbitMQManager messageSender = new RabbitMQManager();  // The RabbitMQManager instance.

    protected String launcherPackage;                                       // The launcher package name.

    /**
     * Constructor for AbstractLauncher.
     * 
     * @param keyToPublish
     * @param orderToPublish
     * @param botProperties
     */
    protected AbstractLauncher(String keyToPublish, String orderToPublish, Properties botProperties) {
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
        this.botProperties = botProperties;
    }

    /**
     * Launches a bot with the provided configuration and parameters.
     * 
     * @param botProperties     The bot's properties.
     * @param botRabbitConfig   The BotRabbitConfig instance that contains the bot's RabbitMQ configuration.
     * @param queueName         The name of the RabbitMQ queue.
     * @param bindingKey        The binding key for the RabbitMQ queue.
     * @param autoDelete        Whether the RabbitMQ queue should be auto-deleted.
     * @param autonomyType      The autonomy type associated with the bot.
     * @param order             The order to process in the message in case of a reactive bot.
     */
    public void launchBot(BotRabbitConfig botRabbitConfig, String queueName, List<String> bindingKeys, boolean autoDelete, String autonomyType, String order) {
        
        String botId = botProperties.getProperty("bot.botId");

        try {
            List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
            this.messageSender.connect(queueName, bindingKeys, queueOptions, botId);
            if (autonomyType.equals("reactive")) {
                this.messageSender.receiveMessage(queueName, botProperties, botRabbitConfig, order, this.launcherPackage);
            } else if (autonomyType.equals("proactive")) {
                this.messageSender.proactiveAction(botProperties, botRabbitConfig, this.launcherPackage);
            }
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error launching bot: " + botId, e);
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

    // Checks connection to RabbitMQ broker.
    public void checkBrokerConnection() {
        try{
            this.messageSender.checkRabbitMQConnection();
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error checking connection to RabbitMQ", e);
        }
    }

    // Setters

    public void setMessageData(JSONObject messageData) {
        this.messageData = messageData;
    }

    public void setLauncherPackage(String launcherPackage){
        this.launcherPackage = launcherPackage;
    }

}
