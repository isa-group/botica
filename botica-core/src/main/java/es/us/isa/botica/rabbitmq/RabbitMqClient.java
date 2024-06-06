package es.us.isa.botica.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMqClient {
  private static final Logger logger = LoggerFactory.getLogger(RabbitMqClient.class);
  private static final int RETRY_SECONDS = 5;
  private static final int MAX_ATTEMPTS = 7;

  private Connection connection;
  private Channel mainChannel;

  public void connect(String username, String password, String host) throws TimeoutException {
    connect(username, password, host, ConnectionFactory.USE_DEFAULT_PORT);
  }

  public void connect(String username, String password, String host, int port)
      throws TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setHost(host);
    factory.setPort(port);
    factory.setAutomaticRecoveryEnabled(true);
    this.connect(factory);
  }

  private void connect(ConnectionFactory connectionFactory) throws TimeoutException {
    int attempts = 0;
    while (this.connection == null) {
      try {
        this.connection = connectionFactory.newConnection();
        this.mainChannel = this.connection.createChannel();
      } catch (IOException | TimeoutException e) {
        if (attempts++ > MAX_ATTEMPTS) {
          throw new TimeoutException("Couldn't establish connection with RabbitMQ");
        }
        logger.error(
            "Couldn't establish connection with RabbitMQ: {}. Retrying in {} seconds...",
            e.getMessage(),
            RETRY_SECONDS);
        sleep(RETRY_SECONDS);
      }
    }
  }

  public void createQueue(String queue) {
    try {
      this.mainChannel.queueDeclare(queue, false, true, true, null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void bind(String exchange, String key, String queue) {
    try {
      this.mainChannel.queueBind(queue, exchange, key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void publish(String exchange, String key, String message) {
    try {
      this.mainChannel.basicPublish(exchange, key, null, message.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void subscribe(String queue, Consumer<String> consumer) {
    try (Channel channel = this.connection.createChannel()) {
      channel.basicConsume(
          queue,
          (consumerTag, message) -> consumer.accept(new String(message.getBody())),
          consumerTag -> {});
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  public void closeConnection() {
    try {
      this.connection.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
