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

/**
 * The RabbitMQManager class manages connections and interactions with RabbitMQ.
 * It handles the connection to RabbitMQ, sending and receiving messages, and
 * provides functionality to connect, send, and receive messages from the
 * RabbitMQ server. It reads server configuration from a JSON file and allows
 * dynamic configuration for specific connections.
 */
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

    /**
     * Constructor for RabbitMQManager.
     *
     * @param username    The username for RabbitMQ connection.
     * @param password    The password for RabbitMQ connection.
     * @param virtualHost The virtual host for RabbitMQ connection.
     * @param host        The host (server) for RabbitMQ connection.
     * @param port        The port for RabbitMQ connection.
     */
    public RabbitMQManager(String username, String password, String virtualHost, String host, int port) {
        factory = new ConnectionFactory();

        loadServerConfig();

        factory.setUsername(username != null ? username : serverUsername);
        factory.setPassword(password != null ? password : serverPassword);
        factory.setVirtualHost(virtualHost != null ? virtualHost : serverVirtualHost);
        factory.setHost(host != null ? host : serverHost);
        factory.setPort(port != 0 ? port : serverPort);
    }

    /**
     * Connect to RabbitMQ server with specified queue options.
     *
     * @param queueName         The name of the queue to declare.
     * @param bindingKey        The binding key to bind the queue to the exchange.
     * @param queueOptions      A list of queue options (durable, exclusive, autoDelete).
     * @throws IOException      If an I/O error occurs while connecting.
     * @throws TimeoutException If a timeout occurs while connecting.
     */
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

    /**
     * Send a message to the RabbitMQ exchange.
     *
     * @param routingKey The routing key for the message.
     * @param message    The message to send.
     * @throws IOException If an I/O error occurs while sending the message.
     */
    public void sendMessageToExchange(String routingKey, String message) throws IOException {
        try{
            channel.basicPublish(serverExchange, routingKey, null, message.getBytes());
        } catch (Exception e) {
            Utils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    /**
     * Receive and process messages from the specified queue.
     *
     * @param queueName    The name of the queue to receive messages from.
     * @param botData      A JSON object containing bot-specific data.
     * @param botType      The type of the bot (e.g., "testCaseGenerator" or "testExecutor").
     * @param order        The order (command) to process when received in a message.
     * @param keyToPublish The binding key to publish  a message to the RabbitMQ broker.
     * @throws IOException If an I/O error occurs while receiving messages.
     */
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

    /**
     * Close the RabbitMQ channel and connection if not set to be persistent.
     *
     * @throws IOException      If an I/O error occurs while closing the channel and
     *                          connection.
     * @throws TimeoutException If a timeout occurs while closing the channel and
     *                          connection.
     */
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
