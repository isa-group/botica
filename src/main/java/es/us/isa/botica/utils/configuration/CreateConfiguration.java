package es.us.isa.botica.utils.configuration;

import es.us.isa.botica.broker.RabbitMqConfigurationGenerator;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
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
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateConfiguration {

    private static final String BOTICA_EXCHANGE = "botica";
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";

    protected static final Logger logger = LogManager.getLogger(CreateConfiguration.class);

    private CreateConfiguration() {
    }

    public static void createConfiguration(File file) {
        ConfigurationFileLoader loader = new JacksonConfigurationFileLoader();
        createConfiguration(loader.load(file, MainConfiguration.class));
    }

    public static void createConfiguration(MainConfiguration mainConfiguration) {
        try {
            new RabbitMqConfigurationGenerator(mainConfiguration).generateDefinitionsFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createDockerCompose(mainConfiguration);
        createUnixMainScript();
        createWindowsMainScript();
    }

    private static void createDockerCompose(MainConfiguration mainConfiguration) {
        List<String> content = new ArrayList<>();

        String initialContent =
                "services:\r\n" +
                "  rabbitmq:\r\n" +
                "    image: \"rabbitmq:3.12-management\"\r\n" +
                "    ports:\r\n" +
                "      - %d:5672\r\n" +
                "    environment:\r\n" +
                "      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbitmq_management load_definitions \"/run/secrets/rabbit_config\"\r\n" +
                "    secrets:\r\n" +
                "      - rabbit_config\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network";

        String intermediateContentTemplate = "  %s:\r\n" +
                "    depends_on:\r\n" +
                "      - rabbitmq\r\n" +
                "    restart: unless-stopped\r\n" +
                "    image: %s\r\n" +
                "    networks:\r\n" +
                "      - rabbitmq-network\r\n" +
                "    volumes:\r\n" +
                "      - botica-volume:/app/shared\r\n" +
                "      - type: bind\r\n" +
                "        source: ./rabbitmq/server-config.json\r\n" +
                "        target: /app/rabbitmq/server-config.json";

        String finalContentTemplate = "\nvolumes:\r\n" +
                "  botica-volume:\r\n\n" +
                "networks:\r\n" +
                "  rabbitmq-network:\r\n" +
                "    driver: bridge\r\n\n" +
                "secrets:\r\n" +
                "  rabbit_config:\r\n" +
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
                content.add("    environment:");

                buildEnvironmentVariables(type, instance).forEach(env -> content.add("      - " + env));
            });
        });

        String finalContent = String.format(finalContentTemplate, RabbitMqConfigurationGenerator.DEFINITIONS_TARGET_PATH);
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

    private static List<String> buildEnvironmentVariables(BotTypeConfiguration bot, BotInstanceConfiguration instance) {
        List<String> environment = new ArrayList<>(instance.getEnvironment());

        // provisional env variables (legacy)
        environment.add("BOTICA_BOT_TYPE=" + bot.getName());
        environment.addAll(getLifecycleVariables(bot, instance));
        environment.add("BOTICA_BOT_PUBLISH_KEY=" + bot.getPublishConfiguration().getKey());
        environment.add("BOTICA_BOT_PUBLISH_ORDER=" + bot.getPublishConfiguration().getOrder());
        environment.add("BOTICA_BOT_SUBSCRIBE_KEYS=" + String.join(",", bot.getSubscribeKeys()));

        environment.add("BOTICA_BOT_ID=" + instance.getId());
        environment.add("BOTICA_BOT_PERSISTENT=" + instance.isPersistent());
        return environment;
    }

    private static List<String> getLifecycleVariables(BotTypeConfiguration bot, BotInstanceConfiguration instance) {
        List<String> environment = new ArrayList<>(instance.getEnvironment());

        BotLifecycleConfiguration lifecycle = Optional.ofNullable(instance.getLifecycleConfiguration())
                .orElseGet(bot::getLifecycleConfiguration);

        environment.add("BOTICA_BOT_AUTONOMY_TYPE=" + lifecycle.getType().getName());
        if (lifecycle instanceof ProactiveBotLifecycleConfiguration) {
            ProactiveBotLifecycleConfiguration proactiveLifecycle = (ProactiveBotLifecycleConfiguration) lifecycle;
            environment.add("BOTICA_BOT_AUTONOMY_INITIAL_DELAY=" + proactiveLifecycle.getInitialDelay());
            environment.add("BOTICA_BOT_AUTONOMY_PERIOD=" + proactiveLifecycle.getPeriod());
        } else if (lifecycle instanceof ReactiveBotLifecycleConfiguration) {
            ReactiveBotLifecycleConfiguration reactiveLifecycle = (ReactiveBotLifecycleConfiguration) lifecycle;
            environment.add("BOTICA_BOT_AUTONOMY_ORDER=" + reactiveLifecycle.getOrder());
        }

        return environment;
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
