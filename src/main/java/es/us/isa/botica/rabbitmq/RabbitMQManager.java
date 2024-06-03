package es.us.isa.botica.rabbitmq;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import es.us.isa.botica.broker.RabbitMqMessageBroker;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.utils.bot.BotHandler;
import es.us.isa.botica.utils.bot.BotRabbitConfig;
import es.us.isa.botica.utils.logging.ExceptionUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    private static final Logger logger = LogManager.getLogger(RabbitMQManager.class);
    private static final String RABBIT_CONTAINER_NAME = "rabbitmq";
    private static final int MESSAGE_TTL = 3600000;

    private final ConnectionFactory factory;    // The ConnectionFactory instance.
    private Connection connection;              // The Connection instance.
    private Channel channel;                    // The Channel instance.

    public RabbitMQManager(RabbitMqConfiguration configuration) {
        this(RABBIT_CONTAINER_NAME, configuration);
    }

    public RabbitMQManager(String host, RabbitMqConfiguration configuration) {
        this(configuration.getUsername(), configuration.getPassword(), "/", host, configuration.getPort());
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
        this.factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
    }

    /**
     * Connect to RabbitMQ server with specified queue options.
     *
     * @param queueName         The name of the queue to declare.
     * @param queueOptions      A list of queue options (durable, exclusive, autoDelete).
     * @throws IOException      If an I/O error occurs while connecting.
     * @throws TimeoutException If a timeout occurs while connecting.
     */
    public String connect(String queueName, List<String> bindingKeys, List<Boolean> queueOptions) throws IOException, TimeoutException {
        String queue = null;

        try{
            connection = factory.newConnection();
            channel = connection.createChannel();

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-message-ttl", MESSAGE_TTL);

            DeclareOk queueDeclared = channel.queueDeclare(queueName, queueOptions.get(0), queueOptions.get(1), queueOptions.get(2), arguments);
            queue = queueDeclared.getQueue();
            if (bindingKeys != null){
                for (String bindingKey : bindingKeys){
                    channel.queueBind(queueName, RabbitMqMessageBroker.BOT_MESSAGES_EXCHANGE, bindingKey);
                }
            }
        } catch (IOException | TimeoutException e) {
            ExceptionUtils.throwRuntimeErrorException("Error with the connection between the bot and RabbitMQ", e);
        }
        return queue;
    }

    /**
     * Connects to RabbitMQ with the specified parameters.
     * 
     * @param queueName  The name of the RabbitMQ queue.
     * @param botId      The identifier of the bot.
     * @throws IOException
     * @throws TimeoutException
     */
    public void connect(String queueName, List<String> bindingKeys, List<Boolean> queueOptions, String botId) throws IOException, TimeoutException {
        connect(queueName, bindingKeys, queueOptions);
        logger.info("Bot {} connected to RabbitMQ", botId);
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
            List<Boolean> queueOptions = Arrays.asList(true, false, true);
            String queue = connect("", null, queueOptions);
            channel.basicPublish(RabbitMqMessageBroker.BOT_MESSAGES_EXCHANGE, routingKey, null, message.getBytes());
            channel.queueDelete(queue);
            logger.info("Message sent to RabbitMQ: {}", message);
            close(); // TODO: Review
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    public void sendMessageToExchange(String exchangeName, String routingKey, String message) throws IOException {
        try {
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
            logger.info("Message sent to RabbitMQ: {}", message);
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    /**
     * Receive and process messages from the specified queue.
     *
     * @param queueName         The name of the queue to receive messages from.
     * @param botRabbitConfig   A BotRabbitConfig object containing RabbitMQ bot-specific configuration.
     * @param order             The order to process.
     * @throws IOException If an I/O error occurs while receiving messages.
     */
    public void receiveMessage(String queueName, BotRabbitConfig botRabbitConfig, String order, String launchersPackage) throws IOException {
        boolean isPersistent = botRabbitConfig.isPersistent();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            logger.info(" [x] Received '{}':'{}'", delivery.getEnvelope().getRoutingKey(), message);

            String messageOrder = new JSONObject(message).getString("order");

            if (messageOrder.contains(order)){
                JSONObject messageData = new JSONObject(message);
                BotHandler.handleReactiveBotAction(botRabbitConfig, launchersPackage, messageData);
                disconnectBot(isPersistent);
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    /**
     * Realise an action and send a message without receiving any order.
     *
     * @param botRabbitConfig
     * @throws IOException
     */
    public void proactiveAction(BotRabbitConfig botRabbitConfig, String launchersPackage) throws IOException {
        boolean isPersistent = botRabbitConfig.isPersistent();
        BotHandler.handleProactiveBotAction(botRabbitConfig, launchersPackage);
        disconnectBot(isPersistent);
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

    /**
     * Check the connection with the RabbitMQ broker.
     *
     * @throws IOException      If an I/O error occurs while checking the connection.
     * @throws TimeoutException If a timeout occurs while checking the connection.
     */
    public void checkRabbitMQConnection() throws IOException, TimeoutException {
        try {
            connection = factory.newConnection();
            connection.close();
        } catch (IOException | TimeoutException e) {
            ExceptionUtils.throwRuntimeErrorException("Error with the connection between the bot and RabbitMQ", e);
        }
    }

    private void disconnectBot(boolean isPersistent) throws IOException{
        if (!isPersistent){
            try {
                close();
            } catch (TimeoutException e) {
                ExceptionUtils.handleException(logger, "Error closing channel and connection", e);
            }
        }
    }

    public void prepareShutdownConnection(String queueName, DeliverCallback deliverCallback){
        try {
            Connection connection = this.factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, true, null);
            channel.queueBind(queueName, RabbitMqMessageBroker.INTERNAL_EXCHANGE, "shutdown");

            CompletableFuture.runAsync(() -> {
                try {
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException | TimeoutException e) {
            ExceptionUtils.handleException(logger, "Error closing channel and connection", e);
        }
    }

    public void consumeChannel(String shutdownQueue, DeliverCallback deliverCallback) throws IOException {
        this.channel.basicConsume(shutdownQueue, true, deliverCallback, consumerTag -> {
        });
    }

    public Connection getConnection(){
        return this.connection;
    }
}
