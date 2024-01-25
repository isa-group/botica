package com.botica.runners;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.property.PropertyManager;

import lombok.Getter;

@Getter
public class ShutdownLoader extends AbstractLoader {
    private static final Logger logger = LogManager.getLogger(ShutdownLoader.class);

    String collectorPropertiesFilePath;

    List<String> botsToShutdown;

    String host;
    // TODO: REVIEW IN NEXT MEEETING
    //String exchangeName;
    String shutdownCommandType;
    Integer timeToWait;
    String shutdownQueue;

    public ShutdownLoader(String collectorPropertiesFilePath, boolean reloadBotProperties) {
        if (reloadBotProperties) {
            PropertyManager.setUserPropertiesFilePath(null);
        }
        this.collectorPropertiesFilePath = collectorPropertiesFilePath;

        this.propertiesFilePath = collectorPropertiesFilePath;
        this.hasGlobalPropertiesPath = true;

        readProperties();
    }

    @Override
    protected void readProperties() {

        logger.info("Loading shutdown parameter values");

        String botsToShutdownString = readProperty("botsOfTheSystem");
        logger.info("Bots of the system: {}", botsToShutdown);

        // Convert the string into a list of strings
        botsToShutdown = new ArrayList<>();
        if (botsToShutdownString != null) {
            String[] bindingsArray = botsToShutdownString.split(",");
            for (String binding : bindingsArray) {
                botsToShutdown.add(binding.trim());
            }
        }

        host = readProperty("host");
        logger.info("Host: {}", host);

        // TODO: REVIEW IN NEXT MEEETING
        //exchangeName = readProperty("exchangeName");
        //logger.info("Exchange name: {}", exchangeName);

        shutdownCommandType = readProperty("shutdownCommandType");
        logger.info("Shutdown command type: {}", shutdownCommandType);

        timeToWait = Integer.parseInt(readProperty("timeToWait"));
        logger.info("Time to wait: {}", timeToWait);

        shutdownQueue = readProperty("shutdownQueue");
        logger.info("Shutdown queue: {}", shutdownQueue);

    }
}
