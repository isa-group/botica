package es.us.isa.botica.director.broker;

import static es.us.isa.botica.director.broker.RabbitMqConfigurationGeneratorTestData.BINDING_FORMAT;
import static es.us.isa.botica.director.broker.RabbitMqConfigurationGeneratorTestData.EXCHANGE_FORMAT;
import static es.us.isa.botica.director.broker.RabbitMqConfigurationGeneratorTestData.QUEUE_FORMAT;
import static es.us.isa.botica.director.broker.RabbitMqConfigurationGeneratorTestData.STREAM_QUEUE_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.ORDER_EXCHANGE;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;
import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotSubscribeConfiguration;
import es.us.isa.botica.configuration.bot.BotSubscribeConfiguration.RoutingStrategy;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
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

    assertThat(content)
        .contains(
            "  \"queues\": [\n"
                + String.format(QUEUE_FORMAT, "bot_type.foo.orders.distributed")
                + ",\n"
                + String.format(QUEUE_FORMAT, "bot_type.bar.orders.distributed")
                + ",\n"
                + String.format(STREAM_QUEUE_FORMAT, "bot_type.bar.orders.broadcast")
                + "\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentExchanges() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();

    assertThat(content)
        .contains(
            "  \"exchanges\": [\n"
                + String.format(EXCHANGE_FORMAT, ORDER_EXCHANGE)
                + ",\n"
                + String.format(EXCHANGE_FORMAT, PROTOCOL_EXCHANGE)
                + "\n"
                + "  ]");
  }

  @Test
  void testBuildDefinitionsContentBindings() {
    RabbitMqConfigurationGenerator configurationGenerator =
        new RabbitMqConfigurationGenerator(buildConfiguration());
    String content = configurationGenerator.buildDefinitionsContent();

    assertThat(content)
        .contains(
            "  \"bindings\": [\n"
                + String.format(
                    BINDING_FORMAT, ORDER_EXCHANGE, "bot_type.foo.orders.distributed", "foo_key")
                + ",\n"
                + String.format(
                    BINDING_FORMAT, ORDER_EXCHANGE, "bot_type.foo.orders.distributed", "bar_key")
                + ",\n"
                + String.format(
                    BINDING_FORMAT, ORDER_EXCHANGE, "bot_type.bar.orders.distributed", "bar_key")
                + ",\n"
                + String.format(
                    BINDING_FORMAT, ORDER_EXCHANGE, "bot_type.bar.orders.broadcast", "qux_key")
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
    var fooKeySubscription = buildSubscription("foo_key", RoutingStrategy.DISTRIBUTED);
    var barKeySubscription = buildSubscription("bar_key", RoutingStrategy.DISTRIBUTED);
    var quxKeySubscription = buildSubscription("qux_key", RoutingStrategy.BROADCAST);

    BotTypeConfiguration fooBotType = new BotTypeConfiguration();
    fooBotType.setId("foo");
    fooBotType.setSubscribeConfigurations(List.of(fooKeySubscription, barKeySubscription));

    BotTypeConfiguration barBotType = new BotTypeConfiguration();
    barBotType.setId("bar");
    barBotType.setSubscribeConfigurations(List.of(barKeySubscription, quxKeySubscription));

    Map<String, BotTypeConfiguration> bots = new LinkedHashMap<>();
    bots.put("foo", fooBotType);
    bots.put("bar", barBotType);
    return bots;
  }

  private static BotSubscribeConfiguration buildSubscription(String key, RoutingStrategy strategy) {
    BotSubscribeConfiguration subscribeConfiguration = new BotSubscribeConfiguration();
    subscribeConfiguration.setKey(key);
    subscribeConfiguration.setStrategy(strategy);
    return subscribeConfiguration;
  }
}
