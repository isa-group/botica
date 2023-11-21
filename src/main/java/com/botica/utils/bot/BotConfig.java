package com.botica.utils.bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.property.PropertyManager;

/*
 * This class contains the specific data for a bot.
 */
public class BotConfig {

    private static final Logger logger = LogManager.getLogger(BotConfig.class);

    private String botId;               // The bot's id.
    private Boolean isPersistent;       // Whether the bot is persistent.
    private Properties botProperties;      // The bot's properties.

    /**
     * Constructor for BotConfig.
     * 
     * @param botPropertyFilePath The path to the bot's property file.
     */
    public BotConfig(String botPropertyFilePath) {
        this.botId = PropertyManager.readProperty(botPropertyFilePath, "bot.botId");
        logger.info("Bot id: {}", botId);

        if (PropertyManager.readProperty(botPropertyFilePath, "bot.isPersistent") != null) {
            this.isPersistent = Boolean.parseBoolean(PropertyManager.readProperty(botPropertyFilePath, "bot.isPersistent"));
        }
        logger.info("Is persistent: {}", isPersistent);

        botProperties = new Properties();
        try (FileInputStream defaultProperties = new FileInputStream(botPropertyFilePath)) {
            botProperties.load(defaultProperties);
            botProperties.keySet().removeIf(key -> !key.toString().startsWith("bot.") || key.toString().equals("bot.botId") || key.toString().equals("bot.isPersistent"));
        } catch (IOException e) {
            logger.error("Error reading property file: {}", e.getMessage());
            logger.error("Exception: ", e);
        }
        logger.info("Bot properties: {}", botProperties);
    }

    //Getters

    public String getBotId() {
        return botId;
    }

    public Boolean getIsPersistent() {
        return isPersistent;
    }

    public Properties getBotProperties() {
        return botProperties;
    }
}
