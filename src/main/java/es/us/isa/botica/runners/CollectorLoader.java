package es.us.isa.botica.runners;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.us.isa.botica.utils.property.PropertyManager;

import lombok.Getter;

@Getter
public class CollectorLoader extends AbstractLoader {

    private static final Logger logger = LogManager.getLogger(CollectorLoader.class);

    String collectorPropertiesFilePath; // The path to the collector's property file.

    List<String> pathsToObserve;    // The paths to observe.
    String localPathToCopy;         // The local path to copy the data collected.
    String containerName;           // The name of the container to launch.
    String imageName;               // The name of the image to use.
    String windowsDockerHost;       // The docker host to use.

    Integer initialDelayToCollect;  // The initial delay to start collecting data.
    Integer periodToCollect;        // The period to collect data.

    public CollectorLoader (String collectorPropertiesFilePath, boolean reloadBotProperties) {
        if(reloadBotProperties){
            PropertyManager.setUserPropertiesFilePath(null);
        }
        this.collectorPropertiesFilePath = collectorPropertiesFilePath;

        this.propertiesFilePath = collectorPropertiesFilePath;
        this.hasGlobalPropertiesPath = false;

        readProperties();
    }

    @Override
    protected void readProperties() {

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
}
