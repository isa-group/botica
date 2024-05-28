package es.us.isa.botica.utils.configuration;

import es.us.isa.botica.configuration.MainConfigurationFile;
import es.us.isa.botica.configuration.bot.BotConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotPublishConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import es.us.isa.botica.utils.directory.DirectoryOperations;
import java.io.File;
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
        createConfiguration(loader.load(file, MainConfigurationFile.class));
    }

    public static void createConfiguration(MainConfigurationFile mainConfigurationFile) {
        createRabbitMQConfigFile(mainConfigurationFile);
        createRabbitMQPortsConfigurationFile(mainConfigurationFile);
        createRabbitMQConnectionFile(mainConfigurationFile);
        createDockerCompose(mainConfigurationFile);
        createUnixMainScript();
        createWindowsMainScript();
    }

    private static void createRabbitMQConfigFile(MainConfigurationFile mainConfigurationFile) {
        List<String> content = new ArrayList<>();

        String initialContent = "{\r\n" +
                "\t\"users\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"admin\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"administrator\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"consumer\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"sender\",\r\n" +
                "\t\t\t\"password\": \"testing1\",\r\n" +
                "\t\t\t\"tags\": \"\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"vhosts\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"/\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"permissions\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"admin\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \".*\",\r\n" +
                "\t\t\t\"write\": \".*\",\r\n" +
                "\t\t\t\"read\": \".*\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"consumer\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \"\",\r\n" +
                "\t\t\t\"write\": \"\",\r\n" +
                "\t\t\t\"read\": \".*\"\r\n" +
                "\t\t},\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"user\": \"sender\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"configure\": \"\",\r\n" +
                "\t\t\t\"write\": \".*\",\r\n" +
                "\t\t\t\"read\": \"\"\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"exchanges\": [\r\n" +
                "\t\t{\r\n" +
                "\t\t\t\"name\": \"" + BOTICA_EXCHANGE + "\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"type\": \"topic\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"internal\": false,\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}\r\n" +
                "\t],\r\n" +
                "\t\"queues\": [\r\n";

        String queueTemplate = "\t\t{\r\n" +
                "\t\t\t\"name\": \"%s\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"arguments\": {\r\n" +
                "\t\t\t\t\"x-message-ttl\": 3600000\r\n" +
                "\t\t\t}\r\n" +
                "\t\t}";

        String bindingPrefix = "\t\"bindings\": [\r\n";

        String bindingTemplate = "\t\t{\r\n" +
                "\t\t\t\"source\": \"%s\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"destination\": \"%s\",\r\n" +
                "\t\t\t\"destination_type\": \"queue\",\r\n" +
                "\t\t\t\"routing_key\": \"%s\",\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}";

        String shutdownQueue = "\t\t{\r\n" +
                "\t\t\t\"name\": \"shutdown\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"durable\": true,\r\n" +
                "\t\t\t\"auto_delete\": false,\r\n" +
                "\t\t\t\"arguments\": {\r\n" +
                "\t\t\t\t\"x-message-ttl\": 3600000\r\n" +
                "\t\t\t}\r\n" +
                "\t\t}";

        String shutdownBinding = "\t\t{\r\n" +
                "\t\t\t\"source\": \"" + BOTICA_EXCHANGE + "\",\r\n" +
                "\t\t\t\"vhost\": \"/\",\r\n" +
                "\t\t\t\"destination\": \"shutdown\",\r\n" +
                "\t\t\t\"destination_type\": \"queue\",\r\n" +
                "\t\t\t\"routing_key\": \"shutdownManager\",\r\n" +
                "\t\t\t\"arguments\": {}\r\n" +
                "\t\t}";

        content.add(initialContent);

        mainConfigurationFile.getBots().stream()
                .map(BotConfiguration::getPublishConfiguration)
                .map(BotPublishConfiguration::getKey)
                .forEach(queueName -> {
                    String queueContent = String.format(queueTemplate, queueName);
                    queueContent += ",\r\n";
                    content.add(queueContent);
                });

        content.add(shutdownQueue);
        content.add("\r\n\t],");

        content.add(bindingPrefix);

        mainConfigurationFile.getBots().forEach(botConfiguration -> {
            botConfiguration.getSubscribeKeys().forEach(queue -> {
                String bindingContent = String.format(bindingTemplate, BOTICA_EXCHANGE, botConfiguration.getName(), queue);
                bindingContent += ",\r\n";
                content.add(bindingContent);
            });
        });

        content.add(shutdownBinding);
        content.add("\r\n\t]\r\n}");

        RabbitMqConfiguration rabbitMqConfiguration = (RabbitMqConfiguration) mainConfigurationFile.getBrokerConfiguration();
        Path filePath = Path.of(rabbitMqConfiguration.getConfigurationPaths().getDefinitions());
        DirectoryOperations.createDir(filePath);
        try {
            // Create the file if it doesn't exist, or overwrite it if it does
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("RabbitMQ broker configuration file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRabbitMQPortsConfigurationFile(MainConfigurationFile mainConfigurationFile){
        RabbitMqConfiguration rabbitMqConfiguration = (RabbitMqConfiguration) mainConfigurationFile.getBrokerConfiguration();
        Path confPath = Path.of(rabbitMqConfiguration.getConfigurationPaths().getMain());
        DirectoryOperations.createDir(confPath);

        try {
            Files.writeString(confPath, "listeners.tcp.default = " + rabbitMqConfiguration.getPort(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Files.writeString(confPath, "\nmanagement.tcp.port = " + rabbitMqConfiguration.getUiPort(), StandardOpenOption.APPEND);

            logger.info("RabbitMQ ports configuration file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRabbitMQConnectionFile(MainConfigurationFile mainConfigurationFile) {
        RabbitMqConfiguration config = (RabbitMqConfiguration) mainConfigurationFile.getBrokerConfiguration();
        Path filePath = Path.of(config.getConfigurationPaths().getConnection());
        DirectoryOperations.createDir(filePath);

        try {
            Files.writeString(filePath, "{\n\t\"username\": \"" + config.getUsername() + "\",\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(filePath, "\t\"password\": \"" + config.getPassword() + "\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"virtualHost\": \"/\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"host\": \"" + config.getHost() + "\",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"port\": " + config.getPort() + ",\n", StandardOpenOption.APPEND);
            Files.writeString(filePath, "\t\"exchange\": \"" + BOTICA_EXCHANGE + "\"\n}", StandardOpenOption.APPEND);

            logger.info("RabbitMQ connection file created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDockerCompose(MainConfigurationFile mainConfigurationFile) {
        List<String> content = new ArrayList<>();

        String initialContentTemplate =
                "services:\r\n" +
                "  rabbitmq:\r\n" +
                "    image: \"rabbitmq:3.12-management\"\r\n" +
                "    ports:\r\n" +
                "      - \"%s:%s\"\r\n" +
                "      - \"%s:%s\"\r\n" +
                "    environment:\r\n" +
                "      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbitmq_management load_definitions \"/run/secrets/rabbit_config\"\r\n" +
                "    secrets:\r\n" +
                "      - rabbit_config\r\n" +
                "    volumes:\r\n" +
                "      - ./%s:/etc/rabbitmq/rabbitmq.conf\r\n" +
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

        RabbitMqConfiguration rabbitMqConfiguration = (RabbitMqConfiguration) mainConfigurationFile.getBrokerConfiguration();
        String initialContent = String.format(initialContentTemplate,
                rabbitMqConfiguration.getPort(), rabbitMqConfiguration.getPort(),
                rabbitMqConfiguration.getUiPort(), rabbitMqConfiguration.getUiPort(),
                rabbitMqConfiguration.getConfigurationPaths().getMain());
        content.add(initialContent);

        mainConfigurationFile.getBots().forEach(bot -> {
            bot.getInstances().forEach(instance -> {
                String intermediateContent = String.format(intermediateContentTemplate, instance.getId(), bot.getImage());
                content.add(intermediateContent);

                bot.getMounts().forEach(mount -> {
                    content.add("      - type: bind\r\n" +
                                "        source: " + mount.getSource() + "\r\n" +
                                "        target: " + mount.getTarget() + "\r\n" +
                                "        bind:\r\n" +
                                "           create_host_path: true");
                });
                content.add("    environment:");

                buildEnvironmentVariables(bot, instance).forEach(env -> content.add("      - " + env));
            });
        });

        String finalContent = String.format(finalContentTemplate, rabbitMqConfiguration.getConfigurationPaths().getDefinitions());
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

    private static List<String> buildEnvironmentVariables(BotConfiguration bot, BotInstanceConfiguration instance) {
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

    private static List<String> getLifecycleVariables(BotConfiguration bot, BotInstanceConfiguration instance) {
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
