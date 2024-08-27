package es.us.isa.botica.director.broker;

import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_TYPE_ORDERS_BROADCAST_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_TYPE_ORDERS_DISTRIBUTED_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.ORDER_EXCHANGE;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotSubscribeConfiguration;
import es.us.isa.botica.configuration.bot.BotSubscribeConfiguration.RoutingStrategy;
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
import java.util.Set;
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
    definitions.add("exchanges", new Exchange(ORDER_EXCHANGE, "topic"));
    definitions.add("exchanges", new Exchange(PROTOCOL_EXCHANGE, "topic"));
    definitions.add("queues", this.buildQueues());
    definitions.add("bindings", this.buildBindings());

    return definitions.render();
  }

  private String buildBotTypeDistributedQueue(String botType) {
    return String.format(BOT_TYPE_ORDERS_DISTRIBUTED_FORMAT, botType);
  }

  private String buildBotTypeBroadcastQueue(String botType) {
    return String.format(BOT_TYPE_ORDERS_BROADCAST_FORMAT, botType);
  }

  private Authentication buildAuthentication() {
    RabbitMqConfiguration rabbitConfiguration =
        (RabbitMqConfiguration) configuration.getBrokerConfiguration();
    return new Authentication(rabbitConfiguration.getUsername(), rabbitConfiguration.getPassword());
  }

  private List<Queue> buildQueues() {
    return this.configuration.getBotTypes().values().stream()
        .map(this::buildQueues)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<Queue> buildQueues(BotTypeConfiguration botType) {
    List<Queue> queues = new ArrayList<>(2);
    Set<RoutingStrategy> strategies =
        botType.getSubscribeConfigurations().stream()
            .map(BotSubscribeConfiguration::getStrategy)
            .collect(Collectors.toSet());

    if (strategies.contains(RoutingStrategy.DISTRIBUTED)) {
      String name = buildBotTypeDistributedQueue(botType.getId());
      queues.add(new Queue(name, false));
    }
    if (strategies.contains(RoutingStrategy.BROADCAST)) {
      String name = buildBotTypeBroadcastQueue(botType.getId());
      queues.add(new Queue(name, true));
    }
    return queues;
  }

  private List<Binding> buildBindings() {
    List<Binding> bindings = new ArrayList<>();
    for (BotTypeConfiguration botType : configuration.getBotTypes().values()) {
      for (BotSubscribeConfiguration subscription : botType.getSubscribeConfigurations()) {
        String queue =
            subscription.getStrategy() == RoutingStrategy.DISTRIBUTED
                ? buildBotTypeDistributedQueue(botType.getId())
                : buildBotTypeBroadcastQueue(botType.getId());
        bindings.add(new Binding(ORDER_EXCHANGE, queue, subscription.getKey()));
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

  private static class Queue {
    public final String name;
    public final boolean isStream;

    private Queue(String name, boolean isStream) {
      this.name = name;
      this.isStream = isStream;
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
