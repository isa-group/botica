package es.us.isa.botica.broker;

import static es.us.isa.botica.broker.RabbitMqMessageBroker.BOT_MESSAGES_EXCHANGE;
import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.MainConfigurationFile;
import es.us.isa.botica.configuration.bot.BotConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
                + String.format(format, "foo")
                + ",\n"
                + String.format(format, "bar")
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
                + String.format(format, BOT_MESSAGES_EXCHANGE)
                + ",\n"
                + String.format(format, RabbitMqMessageBroker.INTERNAL_EXCHANGE)
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
                + String.format(format, BOT_MESSAGES_EXCHANGE, "foo", "foo_key")
                + ",\n"
                + String.format(format, BOT_MESSAGES_EXCHANGE, "foo", "bar_key")
                + ",\n"
                + String.format(format, BOT_MESSAGES_EXCHANGE, "bar", "bar_key")
                + ",\n"
                + String.format(format, BOT_MESSAGES_EXCHANGE, "bar", "qux_key")
                + "\n"
                + "  ]");
  }

  private static MainConfigurationFile buildConfiguration() {
    MainConfigurationFile configuration = new MainConfigurationFile();

    RabbitMqConfiguration rabbitConfiguration = new RabbitMqConfiguration();
    rabbitConfiguration.setUsername("username");
    rabbitConfiguration.setPassword("password");

    configuration.setBrokerConfiguration(rabbitConfiguration);
    configuration.setBots(buildBotConfigurations());
    return configuration;
  }

  private static List<BotConfiguration> buildBotConfigurations() {
    BotConfiguration fooBot = new BotConfiguration();
    fooBot.setName("foo");
    fooBot.setSubscribeKeys(List.of("foo_key", "bar_key"));

    BotConfiguration barBot = new BotConfiguration();
    barBot.setName("bar");
    barBot.setSubscribeKeys(List.of("bar_key", "qux_key"));

    return List.of(fooBot, barBot);
  }
}
