package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.InvalidBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.UnmanagedBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.configuration.broker.InvalidBrokerConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Configuration Deserialization Tests")
class MainConfigurationDeserializationTest {
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  // --- BrokerConfiguration Deserialization Tests ---

  @Test
  @DisplayName("Should deserialize into InvalidBrokerConfiguration for an unknown broker type")
  void deserialize_unknownBrokerType_returnsInvalidBrokerConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json = "{\"type\": \"unknown-broker\"}";

    // Act
    BrokerConfiguration result = objectMapper.readValue(json, BrokerConfiguration.class);

    // Assert
    assertThat(result).isInstanceOf(InvalidBrokerConfiguration.class);
  }

  @Test
  @DisplayName("Should correctly deserialize into RabbitMqConfiguration with all properties")
  void deserialize_rabbitMqBrokerType_returnsPopulatedRabbitMqConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json =
        "{\"type\": \"rabbitmq\", \"username\": \"user\", \"password\": \"pass\", \"port\": 5672}";

    // Act
    BrokerConfiguration result = objectMapper.readValue(json, BrokerConfiguration.class);

    // Assert
    assertThat(result)
        .isInstanceOf(RabbitMqConfiguration.class)
        .satisfies(
            broker -> {
              RabbitMqConfiguration rabbitConfig = (RabbitMqConfiguration) broker;
              assertThat(rabbitConfig.getUsername()).isEqualTo("user");
              assertThat(rabbitConfig.getPassword()).isEqualTo("pass");
              assertThat(rabbitConfig.getPort()).isEqualTo(5672);
            });
  }

  // --- BotLifecycleConfiguration Deserialization Tests ---

  @Test
  @DisplayName(
      "Should deserialize into InvalidBotLifecycleConfiguration for an unknown lifecycle type")
  void deserialize_unknownLifecycleType_returnsInvalidBotLifecycleConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json = "{\"type\": \"unknown-lifecycle\"}";

    // Act
    BotLifecycleConfiguration result =
        objectMapper.readValue(json, BotLifecycleConfiguration.class);

    // Assert
    assertThat(result).isInstanceOf(InvalidBotLifecycleConfiguration.class);
  }

  @Test
  @DisplayName("Should correctly deserialize into ProactiveBotLifecycleConfiguration")
  void deserialize_proactiveLifecycleType_returnsPopulatedProactiveConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json = "{\"type\": \"proactive\", \"initialDelay\": 120, \"period\": 300}";

    // Act
    BotLifecycleConfiguration result =
        objectMapper.readValue(json, BotLifecycleConfiguration.class);

    // Assert
    assertThat(result)
        .isInstanceOf(ProactiveBotLifecycleConfiguration.class)
        .satisfies(
            lifecycle -> {
              ProactiveBotLifecycleConfiguration proactiveConfig =
                  (ProactiveBotLifecycleConfiguration) lifecycle;
              assertThat(proactiveConfig.getInitialDelay()).isEqualTo(120L);
              assertThat(proactiveConfig.getPeriod()).isEqualTo(300L);
            });
  }

  @Test
  @DisplayName("Should correctly deserialize into ReactiveBotLifecycleConfiguration")
  void deserialize_reactiveLifecycleType_returnsPopulatedReactiveConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json = "{\"type\": \"reactive\", \"defaultAction\": \"some-action\"}";

    // Act
    BotLifecycleConfiguration result =
        objectMapper.readValue(json, BotLifecycleConfiguration.class);

    // Assert
    assertThat(result)
        .isInstanceOf(ReactiveBotLifecycleConfiguration.class)
        .satisfies(
            lifecycle -> {
              ReactiveBotLifecycleConfiguration reactiveConfig =
                  (ReactiveBotLifecycleConfiguration) lifecycle;
              assertThat(reactiveConfig.getDefaultAction()).isEqualTo("some-action");
            });
  }

  @Test
  @DisplayName("Should correctly deserialize into UnmanagedBotLifecycleConfiguration")
  void deserialize_unmanagedLifecycleType_returnsUnmanagedConfiguration()
      throws JsonProcessingException {
    // Arrange
    String json = "{\"type\": \"unmanaged\"}";

    // Act
    BotLifecycleConfiguration result =
        objectMapper.readValue(json, BotLifecycleConfiguration.class);

    // Assert
    assertThat(result).isInstanceOf(UnmanagedBotLifecycleConfiguration.class);
  }
}
