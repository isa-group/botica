package es.us.isa.botica.runners;

import java.util.ArrayList;
import java.util.List;

import es.us.isa.botica.utils.property.PropertyManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

@Getter
public class ShutdownLoader extends AbstractLoader {
    private static final Logger logger = LogManager.getLogger(ShutdownLoader.class);

    List<String> botsToShutDown;
    String host;
    String shutdownCommandType;
    Integer timeToWait;
    String shutdownQueue;

    // TODO: REVIEW IN NEXT MEEETING
    // String exchangeName;

    public ShutdownLoader(String propertiesFilePath, boolean reloadBotProperties) {
        if (reloadBotProperties) {
            PropertyManager.setUserPropertiesFilePath(null);
        }

        this.propertiesFilePath = propertiesFilePath;
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
