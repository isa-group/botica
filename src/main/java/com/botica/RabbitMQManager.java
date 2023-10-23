package com.botica;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class RabbitMQManager {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private final String queueName;

    private String serverUsername;
    private String serverPassword;
    private String serverVirtualHost;
    private String serverHost;
    private int serverPort;
    private String serverExchange;

    private static Logger logger = LogManager.getLogger(RabbitMQManager.class);

    public RabbitMQManager(String queueName){
        this(queueName, null, null, null, null, 0);
    }

    public RabbitMQManager(String queueName, String username, String password, String virtualHost, String host, int port) {
        this.queueName = queueName;
        factory = new ConnectionFactory();

        loadServerConfig();

        factory.setUsername(username != null ? username : serverUsername);
        factory.setPassword(password != null ? password : serverPassword);
        factory.setVirtualHost(virtualHost != null ? virtualHost : serverVirtualHost);
        factory.setHost(host != null ? host : serverHost);
        factory.setPort(port != 0 ? port : serverPort);
    }

    public void connect() throws IOException, TimeoutException {
        try{
            connection = factory.newConnection();
            channel = connection.createChannel();

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-message-ttl", 3600000);

            channel.queueDeclare(queueName, true, false, false, arguments);
        } catch (Exception e) {
            logger.error("Error connecting to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        try{
            channel.basicPublish(serverExchange, queueName, null, message.getBytes());
        } catch (Exception e) {
            logger.error("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessageToExchange(String routingKey, String message) throws IOException {
        try{
            channel.basicPublish(serverExchange, routingKey, null, message.getBytes());
        } catch (Exception e) {
            logger.error("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void receiveMessage(String propertyFilePath, String botId, String botType) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            logger.info(" [x] Received '" +
                delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            if (botType.equals("testCaseGenerator")) {
                if (message.contains("generateTestCases") && message.contains(botId)) {
                    TestCaseGeneratorLauncher.generateTestCases(propertyFilePath, botId);
                }
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    private void loadServerConfig() {
        try {
            String jsonPath = "conf/server-config.json";
            String jsonContent = JSON.readFileAsString(jsonPath);
            JSONObject obj = new JSONObject(jsonContent);

            serverUsername = obj.getString("username");
            serverPassword = obj.getString("password");
            serverVirtualHost = obj.getString("virtualHost");
            serverHost = obj.getString("host");
            serverPort = obj.getInt("port");
            serverExchange = obj.getString("exchange");

        } catch (Exception e) {
            logger.error("Error reading server-config.json");
            e.printStackTrace();
        }
    }    

}
