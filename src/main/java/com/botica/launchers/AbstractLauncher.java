package com.botica.launchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;

import com.botica.rabbitmq.RabbitMQManager;
import com.botica.utils.bot.BotRabbitConfig;
import com.botica.utils.logging.ExceptionUtils;

/**
 * This class serves as the base launcher for bots and provides methods for
 * connecting to RabbitMQ and launching bot-related tasks.
 */
public abstract class AbstractLauncher {

    protected static final Logger logger = LogManager.getLogger(AbstractLauncher.class);

    protected String keyToPublish;                                          // The key to publish to RabbitMQ.
    protected String orderToPublish;                                        // The order to publish to RabbitMQ.
    protected Properties botProperties;                                     // The bot properties.
    protected JSONObject messageData;                                       // The message data.
    protected final RabbitMQManager messageSender = new RabbitMQManager();  // The RabbitMQManager instance.

    //protected final RabbitMQManager closerManager = new RabbitMQManager();

    protected String launcherPackage;                                       // The launcher package name.

    /**
     * Constructor for AbstractLauncher.
     * 
     * @param keyToPublish
     * @param orderToPublish
     * @param botProperties
     */
    protected AbstractLauncher(String keyToPublish, String orderToPublish, Properties botProperties) {
        this.keyToPublish = keyToPublish;
        this.orderToPublish = orderToPublish;
        this.botProperties = botProperties;
    }

    /**
     * Launches a bot with the provided configuration and parameters.
     * 
     * @param botProperties     The bot's properties.
     * @param botRabbitConfig   The BotRabbitConfig instance that contains the bot's RabbitMQ configuration.
     * @param queueName         The name of the RabbitMQ queue.
     * @param bindingKey        The binding key for the RabbitMQ queue.
     * @param autoDelete        Whether the RabbitMQ queue should be auto-deleted.
     * @param autonomyType      The autonomy type associated with the bot.
     * @param order             The order to process in the message in case of a reactive bot.
     */
    public void launchBot(BotRabbitConfig botRabbitConfig, String queueName, List<String> bindingKeys, boolean autoDelete, String autonomyType, String order) {
        
        String botId = botProperties.getProperty("bot.botId");

        try {
            pruebaCierreAsyncorno();
            List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);
            this.messageSender.connect(queueName, bindingKeys, queueOptions, botId);
            if (autonomyType.equals("reactive")) {
                this.messageSender.receiveMessage(queueName, botProperties, botRabbitConfig, order, this.launcherPackage);
            } else if (autonomyType.equals("proactive")) {
                this.messageSender.proactiveAction(botProperties, botRabbitConfig, this.launcherPackage);
            }
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error launching bot: " + botId, e);
        }
    }

    public void pruebaCierreAsyncorno(){
        String botId = botProperties.getProperty("bot.botId");
        String QUEUE_NAME= botId+"Closing";
        String EXCHANGE_NAME="closingExchange";
        ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq"); // Replace with the IP or hostname of your RabbitMQ container
            factory.setPort(5674);
            factory.setVirtualHost("/");
            factory.setUsername("admin");
            factory.setPassword("testing1");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    try{
                    String mensaje = new String(delivery.getBody(), "UTF-8");
                    System.out.println("Mensaje recibido: " + mensaje);
                    closingAction();} finally{
                        try {
                            connection.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
    
                };
                CompletableFuture.runAsync(() -> {
                    try {
                        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }        
    }
    
    // Executes bot action.
    protected abstract void botAction();

    //Condition of closing
    protected abstract Boolean closeCondition();

    // Creates message to send to RabbitMQ.
    protected abstract JSONObject createMessage();

    // Executes bot action and sends message to RabbitMQ.
    public void executeBotActionAndSendMessage() {
        botAction();
        try{
            this.messageSender.sendMessageToExchange(this.keyToPublish, createMessage().toString());
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    //Executes the closing procedure
    public void closingAction() {
        System.out.println("Making Close action");
        Boolean closeCond= closeCondition();
        while(!closeCond){
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                closeCond= closeCondition();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (closeCond==true){
            try{
            this.messageSender.sendMessageToExchange("closerManager", "ready "+botProperties.getProperty("bot.botId"));
            this.messageSender.close();
            System.out.println("Close action Completed");
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
        } 

    }

    // Checks connection to RabbitMQ broker.
    public void checkBrokerConnection() {
        try{
            this.messageSender.checkRabbitMQConnection();
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error checking connection to RabbitMQ", e);
        }
    }

    // Setters

    public void setMessageData(JSONObject messageData) {
        this.messageData = messageData;
    }

    public void setLauncherPackage(String launcherPackage){
        this.launcherPackage = launcherPackage;
    }

}
