package es.us.isa.botica.broker;

import static es.us.isa.botica.broker.RabbitMqMessageBroker.BOT_MESSAGES_EXCHANGE;
import static es.us.isa.botica.broker.RabbitMqMessageBroker.INTERNAL_EXCHANGE;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

/**
 * RabbitMQ configuration file generator.
 *
 * @author Alberto Mimbrero
 */
public class RabbitMqConfigurationGenerator {

  public static final Path DEFINITIONS_TARGET_PATH = Path.of(".botica/rabbitmq/definitions.json");
  private static final String DEFINITIONS_TEMPLATE_PATH = "templates/rabbitmq/definitions.json.stg";

  private final MainConfiguration configuration;

  public RabbitMqConfigurationGenerator(MainConfiguration configuration) {
    this.configuration = configuration;
  }

  public void generateDefinitionsFile() throws IOException {
    generateDefinitionsFile(DEFINITIONS_TARGET_PATH);
  }

  public void generateDefinitionsFile(Path path) throws IOException {
    String contents = this.buildDefinitionsContent();
    Files.createDirectories(path.getParent());
    Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
  }

  @VisibleForTesting
  String buildDefinitionsContent() {
    URL url = this.getClass().getClassLoader().getResource(DEFINITIONS_TEMPLATE_PATH);
    ST definitions = new STGroupFile(url).getInstanceOf("file");

    definitions.add("authentication", this.buildAuthentication());
    definitions.add("exchanges", new Exchange(BOT_MESSAGES_EXCHANGE, "topic"));
    definitions.add("exchanges", new Exchange(INTERNAL_EXCHANGE, "topic"));
    definitions.add("queues", this.buildQueues());
    definitions.add("bindings", this.buildBindings());

    return definitions.render();
  }

  private Authentication buildAuthentication() {
    RabbitMqConfiguration rabbitConfiguration =
        (RabbitMqConfiguration) configuration.getBrokerConfiguration();
    return new Authentication(rabbitConfiguration.getUsername(), rabbitConfiguration.getPassword());
  }

  private List<String> buildQueues() {
    return configuration.getBotTypes().stream()
        .map(BotTypeConfiguration::getName)
        .collect(Collectors.toList());
  }

  private List<Binding> buildBindings() {
    List<Binding> bindings = new ArrayList<>();
    for (BotTypeConfiguration botType : configuration.getBotTypes()) {
      for (String key : botType.getSubscribeKeys()) {
        bindings.add(new Binding(BOT_MESSAGES_EXCHANGE, botType.getName(), key));
      }
    }
    return bindings;
  }

  private static class Authentication {
    public final String username, password;

    private Authentication(String username, String password) {
      this.username = username;
      this.password = password;
    }
  }

  private static class Exchange {
    public final String name, type;

    private Exchange(String name, String type) {
      this.name = name;
      this.type = type;
    }
  }

  private static class Binding {
    public final String source, destination, routingKey;

    private Binding(String source, String destination, String routingKey) {
      this.source = source;
      this.destination = destination;
      this.routingKey = routingKey;
    }
  }
}
