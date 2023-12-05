package com.botica.main;

import com.botica.utils.configuration.CreateConfiguration;

public class InitConfiguration {

    private static final String BOTS_DEFINITION_PATH = "src/main/java/com/botica/bots/bots-definition.json";    // The path to the bots definition file.
    private static final String BOTS_PROPERTIES_PATH = "src/main/resources/ConfigurationFiles/";                // The path to the bots properties files.
    private static final String RABBITMQ_EXCHANGE = "restest_exchange";                                         // The name of the RabbitMQ exchange.
    private static final String RABBITMQ_CONFIGURATION_PATH = "rabbitmq/definitions.json";                      // The path to the RabbitMQ configuration file.
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";                                     // The path to the docker compose file.
    private static final String DUMMY_DOCKERFILE_PATH = "docker/Dockerfile";                                    // The path to the dummy dockerfile.
    private static final String BOTICA_DOCKERFILE_PATH = "Dockerfile";                                          // The path to the botica dockerfile.
    private static final String JAR_FILE_NAME = "botica";                                                       // The name of the jar file.
    private static final String INIT_VOLUME_SCRIPT_PATH = "docker/init_volume.sh";                              // The path to the init volume script.
    private static final String BOTICA_IMAGE_NAME = "bot-ica";                                                   // The name of the botica image.
    private static final String MAIN_LAUNCH_SCRIPT = "launch_botica.sh";                                        // The path to the main launch script.

    public static void main(String[] args) {
        CreateConfiguration.createBotPropertiesFiles(BOTS_DEFINITION_PATH, BOTS_PROPERTIES_PATH);
        CreateConfiguration.createRabbitMQConfigFile(RABBITMQ_EXCHANGE, RABBITMQ_CONFIGURATION_PATH);
        CreateConfiguration.createDockerCompose(DOCKER_COMPOSE_PATH);
        CreateConfiguration.createDummyDockerfile(DUMMY_DOCKERFILE_PATH);
        CreateConfiguration.createBoticaDockerfile(BOTICA_DOCKERFILE_PATH, JAR_FILE_NAME);
        CreateConfiguration.createInitVolumeScript(INIT_VOLUME_SCRIPT_PATH);
        CreateConfiguration.createMainScript(MAIN_LAUNCH_SCRIPT, DUMMY_DOCKERFILE_PATH, INIT_VOLUME_SCRIPT_PATH, DOCKER_COMPOSE_PATH, BOTICA_DOCKERFILE_PATH, BOTICA_IMAGE_NAME);
    }
}
