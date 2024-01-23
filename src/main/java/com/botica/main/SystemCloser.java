package com.botica.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Channel;


public class SystemCloser {
    private static String pathOfCloserProperties="src\\main\\resources\\BOTICAConfig\\closer.properties";
    
    //Properties read from file
    private static List<String> botsOfTheSystem= new ArrayList<>();
    private static String host;
    private static String port;
    private static String virtualHost;
    private static String exchangeName;
    private static String userName;
    private static String password;
    private static String closingCommandType;
    private static String timeToWait;


    private static Boolean alreadyStopping= false; 

    private static final Logger logger = LogManager.getLogger(SystemCloser.class);
    private static Integer counter=0;

    public static void main(String[] args) {
            //botsOfTheSystem = readElementsFromFile();
            loadConfiguration(pathOfCloserProperties);
            System.out.println("botsOfTheSystem:" + botsOfTheSystem);
            System.out.println("host:" + host);
            System.out.println("port:" + port);
            System.out.println("virtualHost:" + virtualHost);
            System.out.println("exchangeName:" + exchangeName);
            System.out.println("userName:" + userName);
            System.out.println("password:" + password);
            System.out.println("closingCommandType:" + closingCommandType);
            closing();
    }

    private static void loadConfiguration(String rutaArchivo) {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(rutaArchivo)) {
            properties.load(input);
            String[] bots=getPropertyAsString(properties, "botsOfTheSystem").split(",");
            for (String bot : bots) {
                botsOfTheSystem.add(bot);
            }
            host = getPropertyAsString(properties, "host");
            port = getPropertyAsString(properties, "port");
            virtualHost = getPropertyAsString(properties, "virtualHost");
            exchangeName = getPropertyAsString(properties, "exchangeName");
            userName = getPropertyAsString(properties, "userName");
            password = getPropertyAsString(properties, "password");
            closingCommandType = getPropertyAsString(properties, "closingCommandType");
            timeToWait= getPropertyAsString(properties, "timeToWait");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getPropertyAsString(Properties properties, String key) {
        return properties.getProperty(key, "");
    }

    private static void closing(){
        sendMessage("{\"BoticaCloseAction\": \"true\",\"order\": \"SystemClosing\"}\"");
        receiveMessage();
    }

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
        String queueName="closerManagerQ";
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
                channel.queueBind(queueName, "closingExchange", "");
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
                espera();
                Thread.sleep(Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void performAction(Connection connection, String botName) {
        
        botsOfTheSystem.remove(botName);
        counter =botsOfTheSystem.size();
        if (counter==0){
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            closingCommand();
        }
    }

    private static void espera(){
        if(!alreadyStopping){
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(Integer.parseInt(timeToWait))); // Esperar un minuto
                preguntarApagado();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).join();}
    }

    private static void preguntarApagado() {
        try {
            botsOfTheSystem.forEach(x->System.out.println("The bot "+x+" is not responding"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Do you wish to close the system? (yes/no)");

            String respuesta = reader.readLine().trim().toLowerCase();

            if (respuesta.startsWith("y")) {
                closingCommand();
            } else {
                // Reiniciar el proceso de espera
                espera();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closingCommand() {
        alreadyStopping=true;
        try {
            switch (closingCommandType) {
                case "stop":
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
                case "down":
                    ProcessBuilder downBuilder = new ProcessBuilder("docker-compose", "down");
                    downBuilder.directory(new java.io.File(System.getProperty("user.dir")));
                    // Start the process to run 'docker-compose down'
                    Process downProcess = downBuilder.start();
                    downProcess.waitFor(); // Wait for the process to finish
                    printProcessOutput(downProcess);
                    System.out.println("docker-compose down command executed successfully.");
                    downProcess.destroy();
                    System.exit(0);
                    break;
                default:
                    ProcessBuilder stopBuilderD = new ProcessBuilder("docker-compose", "stop");
                    stopBuilderD.directory(new java.io.File(System.getProperty("user.dir")));
                    // Start the process to run 'docker-compose stop'
                    Process stopProcessD = stopBuilderD.start();
                    stopProcessD.waitFor(); // Wait for the process to finish
                    printProcessOutput(stopProcessD);
                    System.out.println("not valid command specifies defaulting to docker-compose stop command, executed successfully.");
                    stopProcessD.destroy();
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