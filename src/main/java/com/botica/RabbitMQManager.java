package com.botica;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQManager {
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private final String queueName;

    public RabbitMQManager(String queueName, String username, String password, String virtualHost, String host, int port) {
        this.queueName = queueName;
        factory = new ConnectionFactory();

        factory.setUsername(username != null ? username : "admin");
        factory.setPassword(password != null ? password : "testing1");
        factory.setVirtualHost(virtualHost != null ? virtualHost : "/");
        factory.setHost(host != null ? host : "localhost");
        factory.setPort(port != 0 ? port : 5672);
    }

    public void connect() throws IOException, TimeoutException {
        try{
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (Exception e) {
            System.out.println("Error connecting to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        try{
            channel.basicPublish("restest_exchange", queueName, null, message.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public void sendMessageToExchange(String routingKey, String message) throws IOException {
        try{
            channel.basicPublish("restest_exchange", routingKey, null, message.getBytes());
        } catch (Exception e) {
            System.out.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }
    }

    public String receiveMessage() throws IOException {
        GetResponse response = channel.basicGet(queueName, true); // true to confirm message
        if (response != null) {
            return new String(response.getBody());
        }
        return null;
    }

    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }
}
