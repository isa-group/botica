package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import org.junit.jupiter.api.Test;

public class MainConfigurationFileDeserializationTest {
  @Test
  void testLoadBotLifecycleConfiguration() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String proactive = "{'type': 'proactive', 'initialDelay': 60, 'period': 60}".replace("'", "\"");
    assertThat(mapper.readValue(proactive, BotLifecycleConfiguration.class))
        .isInstanceOf(ProactiveBotLifecycleConfiguration.class)
        .satisfies(
            lifecycle -> {
              assertThat(lifecycle).extracting("initialDelay").isEqualTo(60L);
              assertThat(lifecycle).extracting("period").isEqualTo(60L);
            });

    String reactive = "{'type': 'reactive', 'order': 'order'}".replace("'", "\"");
    assertThat(mapper.readValue(reactive, BotLifecycleConfiguration.class))
        .isInstanceOf(ReactiveBotLifecycleConfiguration.class)
        .extracting("order")
        .isEqualTo("order");
  }
}
