package com.botica.runners;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.property.PropertyManager;

import lombok.Getter;

@Getter
public class CollectorLoader {

    private static final Logger logger = LogManager.getLogger(CollectorLoader.class);

    String collectorPropertiesFilePath; // The path to the collector's property file.

    List<String> pathsToObserve;    // The paths to observe.
    String localPathToCopy;         // The local path to copy the data collected.
    String containerName;           // The name of the container to launch.
    String imageName;               // The name of the image to use.
    String windowsDockerHost;       // The docker host to use.

    Integer initialDelayToCollect;  // The initial delay to start collecting data.
    Integer periodToCollect;        // The period to collect data.

    public CollectorLoader (String configurationPropertiesFilePath, boolean reloadBotProperties) {
        if(reloadBotProperties){
            PropertyManager.setUserPropertiesFilePath(null);
        }
        this.collectorPropertiesFilePath = configurationPropertiesFilePath;

        readProperties();
    }

    private void readProperties() {

        logger.info("Loading configuration parameter values");

        // Read the pathsToObserve from the .properties file
        // The list of pathsToObserve is a comma-separated list of strings
        String pathsToObserveString = readProperty("paths.to.observe");
        logger.info("Paths to observe: {}", pathsToObserveString);
        
        // Convert the string into a list of strings
        pathsToObserve = new ArrayList<>();
        if (pathsToObserveString != null) {
            String[] bindingsArray = pathsToObserveString.split(",");
            for (String binding : bindingsArray) {
                pathsToObserve.add(binding.trim());
            }
        }

        localPathToCopy = readProperty("local-path.to.copy");
        logger.info("Local path to copy: {}", localPathToCopy);

        containerName = readProperty("container.name");
        logger.info("Container name: {}", containerName);

        imageName = readProperty("image.name");
        logger.info("Image name: {}", imageName);

        windowsDockerHost = readProperty("windows.docker.host");
        logger.info("Windows docker host: {}", windowsDockerHost);

        initialDelayToCollect = Integer.parseInt(readProperty("initial-delay.to.collect"));
        logger.info("Initial delay to collect: {}", initialDelayToCollect);

        periodToCollect = Integer.parseInt(readProperty("period.to.collect"));
        logger.info("Period to collect: {}", periodToCollect);
    }

    private String readProperty(String propertyName) {

        // Read property from user property file (if provided)
        String value = PropertyManager.readProperty(collectorPropertiesFilePath, propertyName);

        // If null, read property from global property file (config.properties)
        if (value == null)
            value = PropertyManager.readProperty(propertyName);

        return value;
    }

}
