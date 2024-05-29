package es.us.isa.botica.utils.shutdown;

import com.rabbitmq.client.*;
import es.us.isa.botica.broker.RabbitMqMessageBroker;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.rabbitmq.RabbitMQManager;
import es.us.isa.botica.runners.ShutdownLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownUtils {

    private static final Logger logger = LogManager.getLogger(ShutdownUtils.class);

    private static Boolean alreadyStopped = false;

    public static void shutdown(ShutdownLoader shutdownLoader) {
        String message = "{\"BoticaShutdownAction\": \"true\",\"order\": \"SystemShutdown\"}\"";
        sendMessage(message);

        List<String> botIds = shutdownLoader.getConfigurationFile().getBots().stream()
                .flatMap(bot -> bot.getInstances().stream())
                .map(BotInstanceConfiguration::getId)
                .collect(Collectors.toList());
        receiveMessage(botIds);
    }

    public static void sendMessage(String message) {
        try {
            RabbitMQManager messageSender = new RabbitMQManager("localhost");

            List<Boolean> queueOptions = Arrays.asList(true, false, true);
            messageSender.connect("", null, queueOptions);
            messageSender.sendMessageToExchange(RabbitMqMessageBroker.INTERNAL_EXCHANGE, "", message);
            messageSender.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessage(List<String> botIds) {
        RabbitMQManager messageSender = new RabbitMQManager("localhost");

        List<Boolean> queueOptions = Arrays.asList(true, false, true);

        try {
            messageSender.connect("", null, queueOptions);
            Connection connection = messageSender.getConnection();
            System.out.println(" [*] Waiting for messages. To exit press Ctrl+C");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                logger.info(" [x] Received '" + message + "'");
                String botId = message.replace("ready ", "");
                performAction(connection, botId, botIds);
            };
            messageSender.consumeChannel("shutdown", deliverCallback);
            _wait(botIds);
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void performAction(Connection connection, String botId, List<String> botsOfTheSystem) {

        botsOfTheSystem.remove(botId);

        if (botsOfTheSystem.size() == 0) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shutdownCommand();
        }
    }

    private static void _wait(List<String> botIds) {
        if (!alreadyStopped) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    shutdownQuestion(botIds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).join();
        }
    }

    private static void shutdownQuestion(List<String> botIds) {
        try {
            botIds.forEach(x -> logger.info("The bot " + x + " is not responding"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Do you wish to shutdown the system? (yes/no)");

            String response = reader.readLine().trim().toLowerCase();

            if (response.equals("yes")) {
                shutdownCommand();
            } else if (response.equals("no")) {
                _wait(botIds);
            } else {
                System.out.println("Invalid answer. Please try again with a valid answer (yes/no)");
                shutdownQuestion(botIds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutdownCommand() {
        alreadyStopped = true;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "compose", "stop");
            processBuilder.directory(new java.io.File(System.getProperty("user.dir")));

            Process process = processBuilder.start();
            process.waitFor();

            printProcessOutput(process);
            logger.info("docker compose stop command executed successfully.");

            process.destroy();
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            System.out.println("Command Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
