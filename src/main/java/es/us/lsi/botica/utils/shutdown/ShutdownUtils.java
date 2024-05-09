package es.us.lsi.botica.utils.shutdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import es.us.lsi.botica.rabbitmq.RabbitMQManager;
import es.us.lsi.botica.runners.CollectorLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.us.lsi.botica.utils.collector.CollectorUtils;
import com.rabbitmq.client.*;

public class ShutdownUtils {
    
    private static final Logger logger = LogManager.getLogger(ShutdownUtils.class);

    private static final String SHUTDOWN_EXCHANGE_NAME = "shutdown_exchange";

    private static Boolean alreadyStopped = false;

    public static void shutdown(List<String> botsOfTheSystem, String shutdownCommandType, Integer timeToWait, String shutdownQueue, String host, CollectorLoader collectorLoader) {

        String message = "{\"BoticaShutdownAction\": \"true\",\"order\": \"SystemShutdown\"}\"";

        sendMessage(message);
        receiveMessage(shutdownQueue, botsOfTheSystem, timeToWait, shutdownCommandType, collectorLoader);
    }

    public static void sendMessage(String message) {
        try {

            RabbitMQManager messageSender = new RabbitMQManager("localhost");

            List<Boolean> queueOptions = Arrays.asList(true, false, true);
            messageSender.connect("", null, queueOptions);
            messageSender.sendMessageToExchange(SHUTDOWN_EXCHANGE_NAME, "", message);
            messageSender.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessage(String shutdownQueue, List<String> botsOfTheSystem, Integer timeToWait, String shutdownCommandType, CollectorLoader collectorLoader) {
        
        RabbitMQManager messageSender = new RabbitMQManager("localhost");

        List<Boolean> queueOptions = Arrays.asList(true, false, true);

        try {
            messageSender.connect("", null, queueOptions);
            Connection connection = messageSender.getConnection();
            System.out.println(" [*] Waiting for messages. To exit press Ctrl+C");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                logger.info(" [x] Received '" + message + "'");
                String botId= message.replace("ready ", "");
                performAction(connection, botId, botsOfTheSystem, shutdownCommandType);
            };
            messageSender.consumeChannel(shutdownQueue, deliverCallback);
            _wait(botsOfTheSystem, timeToWait, shutdownCommandType, collectorLoader);
            Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void performAction(Connection connection, String botId, List<String> botsOfTheSystem, String shutdownCommandType) {
        
        botsOfTheSystem.remove(botId);

        if (botsOfTheSystem.size() == 0){
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shutdownCommand(shutdownCommandType);
        }
    }

    private static void _wait(List<String> botsOfTheSystem, Integer timeToWait, String shutdownCommandType, CollectorLoader collectorLoader) {
        if(!alreadyStopped){
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(timeToWait));
                    shutdownQuestion(botsOfTheSystem, timeToWait, shutdownCommandType, collectorLoader);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).join();
        }
    }

    private static void shutdownQuestion(List<String> botsOfTheSystem, Integer timeToWait, String shutdownCommandType, CollectorLoader collectorLoader) {
        try {
            botsOfTheSystem.forEach(x -> logger.info("The bot " + x + " is not responding"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Do you wish to shutdown the system? (yes/no)");

            String response = reader.readLine().trim().toLowerCase();

            if (response.equals("yes")) {
                CollectorUtils.collectData(collectorLoader);
                shutdownCommand(shutdownCommandType);
            } else if (response.equals("no")) {
                _wait(botsOfTheSystem, timeToWait, shutdownCommandType, collectorLoader);
            } else {
                System.out.println("Invalid answer. Please try again with a valid answer (yes/no)");
                shutdownQuestion(botsOfTheSystem, timeToWait, shutdownCommandType, collectorLoader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutdownCommand(String shutdownCommandType) {
        alreadyStopped = true;
        try {
            String command = "down".equals(shutdownCommandType) ? "down" : "stop";

            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", command);
            processBuilder.directory(new java.io.File(System.getProperty("user.dir")));

            Process process = processBuilder.start();
            process.waitFor();

            printProcessOutput(process);
            logger.info("docker-compose {} command executed successfully.", command);

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
