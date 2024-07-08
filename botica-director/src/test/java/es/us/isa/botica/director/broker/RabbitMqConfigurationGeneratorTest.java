package es.us.isa.botica.director.broker;

import static es.us.isa.botica.rabbitmq.RabbitMqConstants.ORDER_EXCHANGE;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;
import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RabbitMqConfigurationGeneratorTest {
  private static Path definitionsFilePath;

  @BeforeAll
  static void beforeAll() {
    String tmpDir = System.getProperty("java.io.tmpdir");
    definitionsFilePath =
        Path.of(tmpDir, RabbitMqConfigurationGenerator.DEFINITIONS_TARGET_PATH.toString());
  }

  @Test
  void testGenerateDefinitionsFile() throws IOException {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());

    Files.deleteIfExists(definitionsFilePath);
    configurationGenerator.generateDefinitionsFile(definitionsFilePath);
    assertThat(definitionsFilePath.toFile()).exists().isNotEmpty();
  }

  @Test
  void testBuildDefinitionsContent() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();

    assertThat(content)
        .contains(
            "  \"permissions\": [\n"
                + "    {\n"
                + "      \"user\": \"username\",\n"
                + "      \"vhost\": \"/\",\n"
                + "      \"configure\": \".*\",\n"
                + "      \"write\": \".*\",\n"
                + "      \"read\": \".*\"\n"
                + "    }\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentUsers() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();

    assertThat(content)
        .contains(
            "  \"users\": [\n"
                + "    {\n"
                + "      \"name\": \"username\",\n"
                + "      \"password\": \"password\",\n"
                + "      \"tags\": \"administrator\"\n"
                + "    }\n"
                + "  ],");
    assertThat(content)
        .contains(
            "  \"permissions\": [\n"
                + "    {\n"
                + "      \"user\": \"username\",\n"
                + "      \"vhost\": \"/\",\n"
                + "      \"configure\": \".*\",\n"
                + "      \"write\": \".*\",\n"
                + "      \"read\": \".*\"\n"
                + "    }\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentQueues() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();
    String format =
        "    {\n"
            + "      \"name\": \"%s\",\n"
            + "      \"vhost\": \"/\",\n"
            + "      \"durable\": true,\n"
            + "      \"auto_delete\": false,\n"
            + "      \"arguments\": {\n"
            + "        \"x-message-ttl\": 3600000\n"
            + "      }\n"
            + "    }";

    assertThat(content)
        .contains(
            "  \"queues\": [\n"
                + String.format(format, "bot_type.foo.orders")
                + ",\n"
                + String.format(format, "bot_type.bar.orders")
                + "\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentExchanges() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();
    String format =
        "    {\n"
            + "      \"name\": \"%s\",\n"
            + "      \"vhost\": \"/\",\n"
            + "      \"type\": \"topic\",\n"
            + "      \"durable\": true,\n"
            + "      \"auto_delete\": false,\n"
            + "      \"internal\": false,\n"
            + "      \"arguments\": {}\n"
            + "    }";

    assertThat(content)
        .contains(
            "  \"exchanges\": [\n"
                + String.format(format, ORDER_EXCHANGE)
                + ",\n"
                + String.format(format, PROTOCOL_EXCHANGE)
                + "\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentBindings() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();
    String format =
        "    {\n"
            + "      \"source\": \"%s\",\n"
            + "      \"vhost\": \"/\",\n"
            + "      \"destination\": \"%s\",\n"
            + "      \"destination_type\": \"queue\",\n"
            + "      \"routing_key\": \"%s\",\n"
            + "      \"arguments\": {}\n"
            + "    }";

    assertThat(content)
        .contains(
            "  \"bindings\": [\n"
                + String.format(format, ORDER_EXCHANGE, "bot_type.foo.orders", "foo_key")
                + ",\n"
                + String.format(format, ORDER_EXCHANGE, "bot_type.foo.orders", "bar_key")
                + ",\n"
                + String.format(format, ORDER_EXCHANGE, "bot_type.bar.orders", "bar_key")
                + ",\n"
                + String.format(format, ORDER_EXCHANGE, "bot_type.bar.orders", "qux_key")
                + "\n"
                + "  ]");
  }

  private static MainConfiguration buildConfiguration() {
    MainConfiguration configuration = new MainConfiguration();

    RabbitMqConfiguration rabbitConfiguration = new RabbitMqConfiguration();
    rabbitConfiguration.setUsername("username");
    rabbitConfiguration.setPassword("password");

    configuration.setBrokerConfiguration(rabbitConfiguration);
    configuration.setBotTypes(buildBotConfigurations());
    return configuration;
  }

  private static Map<String, BotTypeConfiguration> buildBotConfigurations() {
    BotTypeConfiguration fooBotType = new BotTypeConfiguration();
    fooBotType.setId("foo");
    ReactiveBotLifecycleConfiguration fooLifecycleConfiguration =
        new ReactiveBotLifecycleConfiguration();
    fooLifecycleConfiguration.setSubscribeKeys(List.of("foo_key", "bar_key"));
    fooBotType.setLifecycleConfiguration(fooLifecycleConfiguration);

    BotTypeConfiguration barBotType = new BotTypeConfiguration();
    barBotType.setId("bar");
    ReactiveBotLifecycleConfiguration barLifecycleConfiguration =
        new ReactiveBotLifecycleConfiguration();
    barLifecycleConfiguration.setSubscribeKeys(List.of("bar_key", "qux_key"));
    barBotType.setLifecycleConfiguration(barLifecycleConfiguration);

    Map<String, BotTypeConfiguration> bots = new LinkedHashMap<>();
    bots.put("foo", fooBotType);
    bots.put("bar", barBotType);
    return bots;
  }
}
