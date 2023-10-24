package com.botica;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.botica.launchers.TestCaseGeneratorLauncher;
import com.botica.utils.JSON;
import com.botica.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class RabbitMQManager {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private String serverUsername;
    private String serverPassword;
    private String serverVirtualHost;
    private String serverHost;
    private int serverPort;
    private String serverExchange;

    private static final Logger logger = LogManager.getLogger(RabbitMQManager.class);
    private static final String CONFIG_FILE_NAME = "server-config.json";
    private static final String DEFAULT_CONFIG_PATH = "conf/" + CONFIG_FILE_NAME;
    private static final int MESSAGE_TTL = 3600000;

    private static final String PROPERTY_FILE_PATH_JSON_KEY = "propertyFilePath";
    private static final String BOT_ID_JSON_KEY = "botId";
    private static final String IS_PERSISTENT_JSON_KEY = "isPersistent";

    public RabbitMQManager(){
        this(null, null, null, null, 0);
    }

    public RabbitMQManager(String username, String password, String virtualHost, String host, int port) {
        factory = new ConnectionFactory();

        loadServerConfig();

        factory.setUsername(username != null ? username : serverUsername);
        factory.setPassword(password != null ? password : serverPassword);
        factory.setVirtualHost(virtualHost != null ? virtualHost : serverVirtualHost);
        factory.setHost(host != null ? host : serverHost);
        factory.setPort(port != 0 ? port : serverPort);
    }

    public void connect(String queueName, String bindingKey, List<Boolean> queueOptions) throws IOException, TimeoutException {
        try{
            connection = factory.newConnection();
            channel = connection.createChannel();

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-message-ttl", MESSAGE_TTL);

            channel.queueDeclare(queueName, queueOptions.get(0), queueOptions.get(1), queueOptions.get(2), arguments);
            if (bindingKey != null){
                channel.queueBind(queueName, serverExchange, bindingKey);
            }

        } catch (IOException | TimeoutException e) {
            Utils.handleException(logger, "Error connecting to RabbitMQ", e);
        }
    }

    public void sendMessageToExchange(String routingKey, String message) throws IOException {
        try{
            channel.basicPublish(serverExchange, routingKey, null, message.getBytes());
        } catch (Exception e) {
            Utils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    public void receiveMessage(String queueName, JSONObject botData, String botType, String order, String keyToPublish) throws IOException {

        String propertyFilePath = botData.getString(PROPERTY_FILE_PATH_JSON_KEY);
        String botId = botData.getString(BOT_ID_JSON_KEY);
        boolean isPersistent = botData.getBoolean(IS_PERSISTENT_JSON_KEY);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            logger.info(" [x] Received '{}':'{}'", delivery.getEnvelope().getRoutingKey(), message);

            if (message.contains(order)){
                if (botType.equals("testCaseGenerator")) {
                    TestCaseGeneratorLauncher.generateTestCases(propertyFilePath, botId, keyToPublish);
                } else if(botType.equals("testExecutor")) {
                    //TODO: Implement testExecutor
                }
                disconnectBot(isPersistent);
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
            String jsonContent = JSON.readFileAsString(DEFAULT_CONFIG_PATH);
            JSONObject obj = new JSONObject(jsonContent);

            serverUsername = obj.getString("username");
            serverPassword = obj.getString("password");
            serverVirtualHost = obj.getString("virtualHost");
            serverHost = obj.getString("host");
            serverPort = obj.getInt("port");
            serverExchange = obj.getString("exchange");

        } catch (Exception e) {
            Utils.handleException(logger, "Error reading " + CONFIG_FILE_NAME, e);
        }
    }

    private void disconnectBot(boolean isPersistent) throws IOException{
        if (!isPersistent){
            try {
                close();
            } catch (TimeoutException e) {
                Utils.handleException(logger, "Error closing channel and connection", e);
            }
        }
    }

}
