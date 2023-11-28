package com.botica.runners;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.launchers.AbstractLauncher;
import com.botica.utils.bot.BotRabbitConfig;
import com.botica.utils.bot.BotHandler;
import com.botica.utils.property.PropertyManager;


/**
 * This class loads the properties files and connects the bot to RabbitMQ.
 */
public class BOTICALoader {

    private static final Logger logger = LogManager.getLogger(BOTICALoader.class);

    String botPropertiesFilePath;   // The path to the bot's property file.

    String botType;                 // The type of bot.
    String order;                   // The order associated with the bot.
    String keyToPublish;            // The binding key for publishing messages.
    String orderToPublish;          // The order to be sent in the message.
    String mainQueue;               // The name of the RabbitMQ queue.
    List<String> bindings;          // The list of bindings for the RabbitMQ queue.
    boolean queueByBot;             // Whether create a queue by bot.
    Properties botProperties;       // The bot's properties.

    public BOTICALoader (String botPropertiesFilePath, boolean reloadBotProperties) {
        if(reloadBotProperties){
            PropertyManager.setUserPropertiesFilePath(null);
        }
		this.botPropertiesFilePath = botPropertiesFilePath;
		
		readProperties();
	}

    // Read the parameter values from the .properties file. If the value is not
    // found, the system looks for it in the global .properties file
    // (config.properties)
    private void readProperties() {

        logger.info("Loading configuration parameter values");

        botType = readProperty("botType");
        logger.info("Bot type: {}", botType);

        order = readProperty("order");
        logger.info("Order: {}", order);

        keyToPublish = readProperty("keyToPublish");
        logger.info("Key to publish: {}", keyToPublish);

        orderToPublish = readProperty("orderToPublish");
        logger.info("Order to publish: {}", orderToPublish);

        mainQueue = readProperty("rabbitOptions.mainQueue");
        logger.info("Main queue: {}", mainQueue);

        // Read the bindings from the .properties file
        // The list of bindings is a comma-separated list of strings
        String bindingsString = readProperty("rabbitOptions.bindings");
        logger.info("Bindings: {}", bindingsString);
        
        // Convert the string into a list of strings
        bindings = new ArrayList<>();
        if (bindingsString != null) {
            String[] bindingsArray = bindingsString.split(",");
            for (String binding : bindingsArray) {
                bindings.add(binding.trim());
            }
        }
        if (readProperty("rabbitOptions.queueByBot") != null) {
            queueByBot = Boolean.parseBoolean(readProperty("rabbitOptions.queueByBot"));
        }
        logger.info("Queue by bot: {}", queueByBot);

        botProperties = new Properties();
        try (FileInputStream defaultProperties = new FileInputStream(botPropertiesFilePath)) {
            botProperties.load(defaultProperties);
            botProperties.keySet().removeIf(key -> !key.toString().startsWith("bot."));
        } catch (Exception e) {
            logger.error("Error reading property file: {}", e.getMessage());
            logger.error("Exception: ", e);
        }
    }

    // Read the parameter values from the user property file (if provided). If the
    // value is not found, look for it in the global .properties file
    // (config.properties)
    private String readProperty(String propertyName) {

        // Read property from user property file (if provided)
        String value = PropertyManager.readProperty(botPropertiesFilePath, propertyName);

        // If null, read property from global property file (config.properties)
        if (value == null)
            value = PropertyManager.readProperty(propertyName);

        return value;
    }

    /**
     * Connects the bot to RabbitMQ.
     */
    public void connectBotToRabbit() {

        AbstractLauncher launcher = BotHandler.handleLauncherType(botType, keyToPublish, orderToPublish, botProperties);
        
        String botId = botProperties.getProperty("bot.botId");

        BotRabbitConfig botRabbitConfig = new BotRabbitConfig(botType, order, keyToPublish, orderToPublish);
        if (queueByBot) {
            String bindingKey = mainQueue + "." + botId;
            List<String> bindingKeys = new ArrayList<>();
            bindingKeys.add(bindingKey);
            
            launcher.launchBot(botRabbitConfig, botId, bindingKeys, true);
        } else {
            launcher.launchBot(botRabbitConfig, mainQueue, bindings, false);
        }
    }

}