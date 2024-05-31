package es.us.isa.botica.utils.configuration;

import es.us.isa.botica.broker.RabbitMqConfigurationGenerator;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import es.us.isa.botica.utils.directory.DirectoryOperations;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateConfiguration {

    public static final String BOTICA_CONFIG_SECRET = "botica-config";
    private static final String RABBIT_DEFINITIONS_SECRET = "rabbit-definitions";

    private static final Logger logger = LogManager.getLogger(CreateConfiguration.class);
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";

    private CreateConfiguration() {
    }

    public static void createConfiguration(File file) {
        ConfigurationFileLoader loader = new JacksonConfigurationFileLoader();
        createConfiguration(loader.load(file, MainConfiguration.class), file);
    }

    public static void createConfiguration(MainConfiguration mainConfiguration, File file) {
        try {
            new RabbitMqConfigurationGenerator(mainConfiguration).generateDefinitionsFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createDockerCompose(mainConfiguration, file);
        createUnixMainScript();
        createWindowsMainScript();
    }

    private static void createDockerCompose(MainConfiguration mainConfiguration, File file) {
        List<String> content = new ArrayList<>();

        String initialContent =
                "services:\r\n" +
                "  rabbitmq:\r\n" +
                "    image: \"rabbitmq:3.12-management\"\r\n" +
                "    ports:\r\n" +
                "      - %d:5672\r\n" +
                "    environment:\r\n" +
                "      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbitmq_management load_definitions \"/run/secrets/" + RABBIT_DEFINITIONS_SECRET + "\"\r\n" +
                "    secrets:\r\n" +
                "      - " + RABBIT_DEFINITIONS_SECRET + "\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network";

        String intermediateContentTemplate = "  %s:\r\n" +
                "    depends_on:\r\n" +
                "      - rabbitmq\r\n" +
                "    restart: unless-stopped\r\n" +
                "    image: %s\r\n" +
                "    secrets:\r\n" +
                "      - " + BOTICA_CONFIG_SECRET + "\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network\r\n" +
                "    volumes:\r\n" +
                "      - botica-volume:/app/shared";

        String finalContentTemplate = "\nvolumes:\r\n" +
                "  botica-volume:\r\n\n" +
                "networks:\r\n" +
                "  rabbitmq-network:\r\n" +
                "    driver: bridge\r\n\n" +
                "secrets:\r\n" +
                "  " + BOTICA_CONFIG_SECRET + ":\r\n" +
                "    file: ./%s\n" +
                "  " + RABBIT_DEFINITIONS_SECRET + ":\r\n" +
                "    file: ./%s";

        RabbitMqConfiguration rabbitMqConfiguration = (RabbitMqConfiguration) mainConfiguration.getBrokerConfiguration();
        content.add(String.format(initialContent, rabbitMqConfiguration.getPort()));

        mainConfiguration.getBotTypes().values().forEach(type -> {
            type.getInstances().values().forEach(instance -> {
                String intermediateContent = String.format(intermediateContentTemplate, instance.getId(), type.getImage());
                content.add(intermediateContent);

                type.getMounts().forEach(mount -> {
                    content.add("      - type: bind\r\n" +
                                "        source: " + mount.getSource() + "\r\n" +
                                "        target: " + mount.getTarget() + "\r\n" +
                                "        bind:\r\n" +
                                "           create_host_path: true");
                });
                List<String> environment = new ArrayList<>(instance.getEnvironment());
                environment.add("BOTICA_BOT_TYPE=" + type.getName());
                environment.add("BOTICA_BOT_ID=" + instance.getId());

                content.add("    environment:");
                environment.forEach(env -> content.add("      - " + env));
            });
        });

        String finalContent = String.format(finalContentTemplate,
                file.getPath(),
                RabbitMqConfigurationGenerator.DEFINITIONS_TARGET_PATH);
        content.add(finalContent);

        Path filePath = Path.of(DOCKER_COMPOSE_PATH);
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Docker compose file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createUnixMainScript() {
        Path scriptPath = Path.of("launch-botica.sh");
        DirectoryOperations.createDir(scriptPath);

        try {
            Files.writeString(scriptPath, "#!/bin/bash\n\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "echo \"Running docker compose...\"\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker compose -f " + DOCKER_COMPOSE_PATH + " up -d\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo \"Script completed successfully.\"", StandardOpenOption.APPEND);

            try {
                Files.setPosixFilePermissions(scriptPath, PosixFilePermissions.fromString("rwxr-xr-x"));
            } catch (Exception e) {
              logger.warn("Couldn't set permissions to the BOTICA Unix main script. Please, set them manually in case you need to execute it.");
            }

            logger.info("BOTICA Unix main script created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createWindowsMainScript() {
        Path scriptPath = Path.of("launch-botica.bat");
        DirectoryOperations.createDir(scriptPath);

        try {
            Files.writeString(scriptPath, "@echo off\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(scriptPath, "echo Running docker compose...\n", StandardOpenOption.APPEND);
            Files.writeString(scriptPath, "docker compose -f " + DOCKER_COMPOSE_PATH + " up -d\n\n", StandardOpenOption.APPEND);

            Files.writeString(scriptPath, "echo Script completed successfully.", StandardOpenOption.APPEND);

            try {
                Files.setPosixFilePermissions(scriptPath, PosixFilePermissions.fromString("rwxr-xr-x"));
            } catch (Exception e) {
                logger.warn("Couldn't set permissions to the BOTICA Windows main script. Please, set them manually in case you need to execute it.");
            }

            logger.info("BOTICA Windows main script created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
