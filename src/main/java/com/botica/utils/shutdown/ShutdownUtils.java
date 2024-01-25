package com.botica.utils.shutdown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.*;

public class ShutdownUtils {
    private static final Logger logger = LogManager.getLogger(ShutdownUtils.class);

    private static Boolean alreadyStopping = false;

    public static void shutdown(){
        sendMessage("{\"BoticaShutdownAction\": \"true\",\"order\": \"SystemShutdown\"}\"");
        receiveMessage();
    }

    private static String host = "localhost";
    private static String port = "5671";
    private static String virtualHost = "/";
    private static String userName = "admin";
    private static String password = "testing1";
    private static String exchangeName = "shutdownExchange";
    private static String closingCommandType = "stop";
    private static String timeToWait = "10";
    private static String shutdownQueue = "shutdown";
    private static List<String> botsOfTheSystem = List.of("ex1","ex2","gen4","gen5","re1","re2");

    private static void sendMessage(String message) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(Integer.parseInt(port));
            factory.setVirtualHost(virtualHost);
            factory.setUsername(userName);
            factory.setPassword(password);
            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) { 
                channel.basicPublish(exchangeName, "", null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveMessage() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost(virtualHost);
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setHost(host); 
        factory.setPort(Integer.parseInt(port)); 
            try {
                System.out.println("Trying to conect");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                System.out.println(" [*] Waiting for messages. To exit press Ctrl+C");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    String botId= message.replace("ready ", "");
                    performAction(connection, botId);
                };
                //channel.queueBind(shutdownQueue, "shutdownExchange", "");
                channel.basicConsume(shutdownQueue, true, deliverCallback, consumerTag -> {});
                _wait();
                Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void performAction(Connection connection, String botName) {
        
        botsOfTheSystem.remove(botName);
        Integer counter =botsOfTheSystem.size();
        if (counter==0){
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shutdownCommand();
        }
    }

    private static void _wait(){
        if(!alreadyStopping){
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(Integer.parseInt(timeToWait)));
                shutdownQuestion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).join();}
    }

    private static void shutdownQuestion() {
        try {
            botsOfTheSystem.forEach(x->System.out.println("The bot " + x + " is not responding"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Do you wish to shutdown the system? (yes/no)");

            String response = reader.readLine().trim().toLowerCase();

            if (response.equals("y")) {
                shutdownCommand();
            } else if (response.equals("n")) {
                _wait();
            } else {
                System.out.println("Invalid answer. Please try again with a valid answer (yes/no)");
                shutdownQuestion();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutdownCommand() {
        alreadyStopping=true;
        try {
            switch (closingCommandType) {                    
                case "down":
                    ProcessBuilder downBuilder = new ProcessBuilder("docker-compose", "down");
                    downBuilder.directory(new java.io.File(System.getProperty("user.dir")));
                    // Start the process to run 'docker-compose down'
                    Process downProcess = downBuilder.start();
                    downProcess.waitFor(); // Wait for the process to finish
                    printProcessOutput(downProcess);
                    logger.info("docker-compose down command executed successfully.");
                    downProcess.destroy();
                    System.exit(0);
                    break;
                default: // "stop"
                    ProcessBuilder stopBuilder = new ProcessBuilder("docker-compose", "stop");
                    stopBuilder.directory(new java.io.File(System.getProperty("user.dir")));
                    // Start the process to run 'docker-compose stop'
                    Process stopProcess = stopBuilder.start();
                    stopProcess.waitFor(); // Wait for the process to finish
                    printProcessOutput(stopProcess);
                    System.out.println("docker-compose stop command executed successfully.");
                    stopProcess.destroy();
                    System.exit(0);
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printProcessOutput(Process process) throws IOException {
        // Print the result of the command execution
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            System.out.println("Command Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}
