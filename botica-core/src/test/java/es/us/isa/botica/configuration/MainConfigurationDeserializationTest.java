package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.InvalidBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.configuration.broker.InvalidBrokerConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import org.junit.jupiter.api.Test;

public class MainConfigurationDeserializationTest {

  @Test
  void name() {
    System.out.println(byte.class.equals(byte.class.getComponentType()));
  }

  @Test
  void testLoadBrokerConfigurationInvalidType() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String json = "{'type': 'foo'}".replace("'", "\"");
    assertThat(mapper.readValue(json, BrokerConfiguration.class))
        .isInstanceOf(InvalidBrokerConfiguration.class);
  }

  @Test
  void testLoadBrokerConfigurationRabbitMq() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String json =
        "{'type': 'rabbitmq', 'username': 'username', 'password': 'password', 'port': 5672}"
            .replace("'", "\"");
    assertThat(mapper.readValue(json, BrokerConfiguration.class))
        .isInstanceOf(RabbitMqConfiguration.class)
        .satisfies(
            broker -> {
              assertThat(broker).extracting("username").isEqualTo("username");
              assertThat(broker).extracting("password").isEqualTo("password");
              assertThat(broker).extracting("port").isEqualTo(5672);
            });
  }

  @Test
  void testLoadBotLifecycleConfigurationInvalidType() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String json = "{'type': 'foo'}".replace("'", "\"");
    assertThat(mapper.readValue(json, BotLifecycleConfiguration.class))
            .isInstanceOf(InvalidBotLifecycleConfiguration.class);
  }

  @Test
  void testLoadBotLifecycleConfigurationProactive() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String json = "{'type': 'proactive', 'initialDelay': 60, 'period': 60}".replace("'", "\"");
    assertThat(mapper.readValue(json, BotLifecycleConfiguration.class))
        .isInstanceOf(ProactiveBotLifecycleConfiguration.class)
        .satisfies(
            lifecycle -> {
              assertThat(lifecycle).extracting("initialDelay").isEqualTo(60L);
              assertThat(lifecycle).extracting("period").isEqualTo(60L);
            });
  }

  @Test
  void testLoadBotLifecycleConfigurationReactive() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String json = "{'type': 'reactive', 'order': 'order'}".replace("'", "\"");
    assertThat(mapper.readValue(json, BotLifecycleConfiguration.class))
        .isInstanceOf(ReactiveBotLifecycleConfiguration.class)
        .extracting("order")
        .isEqualTo("order");
  }
}
