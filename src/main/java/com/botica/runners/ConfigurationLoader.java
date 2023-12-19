package com.botica.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.property.PropertyManager;

import lombok.Getter;

@Getter
public class ConfigurationLoader extends AbstractLoader {

    private static final Logger logger = LogManager.getLogger(ConfigurationLoader.class);

    String configurationPropertiesFilePath; // The path to the configuration's property file.

    String botsDefinitionPath;          // The path to read the bots definition file.
    String botsPropertiesPath;          // The path to store the bots properties files generated.

    // Settings for connection between bots and RabbitMQ.
    String rabbitMQUsername;            // The RabbitMQ username.
    String rabbitMQPassword;            // The RabbitMQ password.
    String rabbitMQHost;                // The RabbitMQ host.
    Integer rabbitMQPort;               // The RabbitMQ port.
    String rabbitMQExchange;            // The name of the RabbitMQ exchange.

    String rabbitMQConfigurationPath;   // The path to store the configuration file of the RabbitMQ broker.
    String rabbitMQConnectionPath;      // The path to store the connection file of the RabbitMQ broker.

    String dockerComposePath;           // The path to store the docker compose file generated to deploy the bots.

    String dummyDockerfilePath;         // The path to store the dummy dockerfile used to create the volume.
    String boticaDockerfilePath;        // The path to store the Dockerfile used to create the BOTICA image.
    String jarFileName;                 // The name of the jar file generated, used to launch the BOTICA bots.

    String initVolumeScriptPath;        // The path to store the script used to init volume with the necessary data.
    String boticaImageName;             // The name to use for the BOTICA image.
    String mainLaunchScript;            // The path to store the script used to launch the BOTICA bots.

    public ConfigurationLoader (String configurationPropertiesFilePath, boolean reloadBotProperties) {
        if(reloadBotProperties){
            PropertyManager.setUserPropertiesFilePath(null);
        }
		this.configurationPropertiesFilePath = configurationPropertiesFilePath;

        this.propertiesFilePath = configurationPropertiesFilePath;
        this.hasGlobalPropertiesPath = false;
		
		readProperties();
	}

    @Override
    protected void readProperties() {

        logger.info("Loading configuration parameter values");

        botsDefinitionPath = readProperty("bots.definition.path");
        logger.info("Bots definition path: {}", botsDefinitionPath);

        botsPropertiesPath = readProperty("bots.properties.path");
        logger.info("Bots properties path: {}", botsPropertiesPath);

        rabbitMQUsername = readProperty("rabbitmq.username");
        logger.info("RabbitMQ username: {}", rabbitMQUsername);

        rabbitMQPassword = readProperty("rabbitmq.password");
        logger.info("RabbitMQ password: {}", rabbitMQPassword);

        rabbitMQHost = readProperty("rabbitmq.host");
        logger.info("RabbitMQ host: {}", rabbitMQHost);

        rabbitMQPort = Integer.parseInt(readProperty("rabbitmq.port"));
        logger.info("RabbitMQ port: {}", rabbitMQPort);

        rabbitMQExchange = readProperty("rabbitmq.exchange");
        logger.info("RabbitMQ exchange: {}", rabbitMQExchange);

        rabbitMQConfigurationPath = readProperty("rabbitmq.configuration.path");
        logger.info("RabbitMQ configuration path: {}", rabbitMQConfigurationPath);

        rabbitMQConnectionPath = readProperty("rabbitmq.connection.path");
        logger.info("RabbitMQ connection path: {}", rabbitMQConnectionPath);

        dockerComposePath = readProperty("docker.compose.path");
        logger.info("Docker compose path: {}", dockerComposePath);

        dummyDockerfilePath = readProperty("dummy.dockerfile.path");
        logger.info("Dummy dockerfile path: {}", dummyDockerfilePath);

        boticaDockerfilePath = readProperty("botica.dockerfile.path");
        logger.info("BOTICA dockerfile path: {}", boticaDockerfilePath);

        jarFileName = readProperty("jar.file.name");
        logger.info("Jar file name: {}", jarFileName);

        initVolumeScriptPath = readProperty("init.volume.script.path");
        logger.info("Init volume script path: {}", initVolumeScriptPath);

        boticaImageName = readProperty("botica.image.name");
        logger.info("BOTICA image name: {}", boticaImageName);

        mainLaunchScript = readProperty("main.launch.script");
        logger.info("Main launch script: {}", mainLaunchScript);
    }
}
