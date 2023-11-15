package com.botica.utils.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.property.PropertyManager;

public class BotConfig {

    private static final Logger logger = LogManager.getLogger(BotConfig.class);

    private String botId;
    private String propertyFilePath;
    private Boolean isPersistent;

    public BotConfig(String botPropertyFilePath) {

        this.botId = PropertyManager.readProperty(botPropertyFilePath, "bot.botId");
        logger.info("Bot id: {}", botId);

        this.propertyFilePath = PropertyManager.readProperty(botPropertyFilePath, "bot.propertyFilePath");
        logger.info("Bot property file path: {}", propertyFilePath);
        
        if (PropertyManager.readProperty(botPropertyFilePath, "bot.isPersistent") != null) {
            this.isPersistent = Boolean.parseBoolean(PropertyManager.readProperty(botPropertyFilePath, "bot.isPersistent"));
        }
        logger.info("Is persistent: {}", isPersistent);
    }

    //Getters
    public String getBotId() {
        return botId;
    }

    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    public Boolean getIsPersistent() {
        return isPersistent;
    }
}
