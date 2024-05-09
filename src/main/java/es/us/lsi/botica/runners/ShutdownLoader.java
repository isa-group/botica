package es.us.lsi.botica.runners;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.us.lsi.botica.utils.property.PropertyManager;

import lombok.Getter;

@Getter
public class ShutdownLoader extends AbstractLoader {
    private static final Logger logger = LogManager.getLogger(ShutdownLoader.class);

    String collectorPropertiesFilePath;

    List<String> botsToShutDown;
    String host;
    String shutdownCommandType;
    Integer timeToWait;
    String shutdownQueue;

    // TODO: REVIEW IN NEXT MEEETING
    // String exchangeName;

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

        String botsToShutDownString = readProperty("bots.of.the.system");
        logger.info("Bots to shut down: {}", botsToShutDown);

        // Convert the string into a list of strings
        botsToShutDown = new ArrayList<>();
        if (botsToShutDownString != null) {
            String[] bindingsArray = botsToShutDownString.split(",");
            for (String binding : bindingsArray) {
                botsToShutDown.add(binding.trim());
            }
        }

        host = readProperty("host");
        logger.info("Host: {}", host);

        // TODO: REVIEW IN NEXT MEEETING
        //exchangeName = readProperty("exchange.name");
        //logger.info("Exchange name: {}", exchangeName);

        shutdownCommandType = readProperty("shutdown.command.type");
        logger.info("Shutdown command type: {}", shutdownCommandType);

        timeToWait = Integer.parseInt(readProperty("time.to.wait"));
        logger.info("Time to wait: {}", timeToWait);

        shutdownQueue = readProperty("shutdown.queue");
        logger.info("Shutdown queue: {}", shutdownQueue);

    }
}
