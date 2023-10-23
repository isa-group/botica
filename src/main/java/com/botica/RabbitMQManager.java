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

import org.json.JSONObject;

public class RabbitMQManager {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private final String queueName;

    private String server_username;
    private String server_password;
    private String server_virtualHost;
    private String server_host;
    private int server_port;
    private String server_exchange;

    public RabbitMQManager(String queueName){
        this(queueName, null, null, null, null, 0);
    }

    public RabbitMQManager(String queueName, String username, String password, String virtualHost, String host, int port) {
        this.queueName = queueName;
        factory = new ConnectionFactory();

        loadServerConfig();

        factory.setUsername(username != null ? username : server_username);
        factory.setPassword(password != null ? password : server_password);
        factory.setVirtualHost(virtualHost != null ? virtualHost : server_virtualHost);
        factory.setHost(host != null ? host : server_host);
        factory.setPort(port != 0 ? port : server_port);
    }

    public void connect() throws IOException, TimeoutException {
        try{
            connection = factory.newConnection();
            channel = connection.createChannel();

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-message-ttl", 3600000);

            channel.queueDeclare(queueName, true, false, false, arguments);
        } catch (Exception e) {
            System.out.println("Error connecting to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        try{
            channel.basicPublish(server_exchange, queueName, null, message.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessageToExchange(String routingKey, String message) throws IOException {
        try{
            channel.basicPublish(server_exchange, routingKey, null, message.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void receiveMessage(String propertyFilePath, String botId, String botType) throws IOException {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
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
            String json_path = "conf/server_config.json";
            String json_content = JSON.readFileAsString(json_path);
            JSONObject obj = new JSONObject(json_content);

            server_username = obj.getString("username");
            server_password = obj.getString("password");
            server_virtualHost = obj.getString("virtualHost");
            server_host = obj.getString("host");
            server_port = obj.getInt("port");
            server_exchange = obj.getString("exchange");

        } catch (Exception e) {
            System.out.println("Error reading server_config.json");
            e.printStackTrace();
        }
    }    

}
