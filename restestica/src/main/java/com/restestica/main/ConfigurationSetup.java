package com.restestica.main;

import com.botica.utils.configuration.CreateConfiguration;

public class ConfigurationSetup {

    private static final String BOTS_DEFINITION_PATH = "src/main/java/com/restestica/bots/bots-definition.json";    // The path to read the bots definition file.
    
    private static final String BOTS_PROPERTIES_PATH = "src/main/resources/ConfigurationFiles/";                // The path to store the bots properties files generated.

    // Settings for connection between bots and RabbitMQ.
    private static final String RABBITMQ_USERNAME = "admin";                                                    // The RabbitMQ username.
    private static final String RABBITMQ_PASSWORD = "testing1";                                                 // The RabbitMQ password.
    private static final String RABBITMQ_HOST = "rabbitmq";                                                     // The RabbitMQ host.
    private static final Integer RABBITMQ_PORT = 5672;                                                          // The RabbitMQ port.
    private static final String RABBITMQ_EXCHANGE = "restest_exchange";                                         // The name of the RabbitMQ exchange.


    private static final String RABBITMQ_CONFIGURATION_PATH = "rabbitmq/definitions.json";                      // The path to store the configuration file of the RabbitMQ broker.
    private static final String RABBITMQ_CONNECTION_PATH = "rabbitmq/server-config.json";                       // The path to store the connection file of the RabbitMQ broker.

    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";                                     // The path to store the docker compose file generated to deploy the bots.

    private static final String DUMMY_DOCKERFILE_PATH = "docker/Dockerfile";                                    // The path to store the dummy dockerfile used to create the volume.
    private static final String BOTICA_DOCKERFILE_PATH = "Dockerfile";                                          // The path to store the Dockerfile used to create the BOTICA image.
    private static final String JAR_FILE_NAME = "restestica";                                                   // The name of the jar file generated, used to launch the BOTICA bots.

    private static final String INIT_VOLUME_SCRIPT_PATH = "docker/init_volume.sh";                              // The path to store the script used to init volume with the necessary data.
    private static final String BOTICA_IMAGE_NAME = "bot-ica";                                                  // The name to use for the BOTICA image.
    private static final String MAIN_LAUNCH_SCRIPT = "launch_botica.sh";                                        // The path to store the script used to launch the BOTICA bots.

    public static void main(String[] args) {
        CreateConfiguration.createBotPropertiesFiles(BOTS_DEFINITION_PATH, BOTS_PROPERTIES_PATH);
        CreateConfiguration.createRabbitMQConfigFile(RABBITMQ_EXCHANGE, RABBITMQ_CONFIGURATION_PATH);
        CreateConfiguration.createRabbitMQConnectionFile(RABBITMQ_CONNECTION_PATH, RABBITMQ_USERNAME, RABBITMQ_PASSWORD, RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_EXCHANGE);
        CreateConfiguration.createDockerCompose(DOCKER_COMPOSE_PATH);
        CreateConfiguration.createDummyDockerfile(DUMMY_DOCKERFILE_PATH);
        CreateConfiguration.createBoticaDockerfile(BOTICA_DOCKERFILE_PATH, JAR_FILE_NAME);
        CreateConfiguration.createInitVolumeScript(INIT_VOLUME_SCRIPT_PATH);
        CreateConfiguration.createMainScript(MAIN_LAUNCH_SCRIPT, DUMMY_DOCKERFILE_PATH, INIT_VOLUME_SCRIPT_PATH, DOCKER_COMPOSE_PATH, BOTICA_DOCKERFILE_PATH, BOTICA_IMAGE_NAME);
    }
}
