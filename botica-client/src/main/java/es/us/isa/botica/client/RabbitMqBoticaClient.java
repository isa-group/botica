package es.us.isa.botica.client;

import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_ORDERS_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_PROTOCOL_IN_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_PROTOCOL_OUT_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_TYPE_ORDERS_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.CONTAINER_NAME;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.ORDER_EXCHANGE;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;

import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.protocol.Packet;
import es.us.isa.botica.protocol.PacketConverter;
import es.us.isa.botica.protocol.PacketListener;
import es.us.isa.botica.rabbitmq.RabbitMqClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

public class RabbitMqBoticaClient implements BoticaClient {
  private final BotTypeConfiguration typeConfiguration;
  private final BotInstanceConfiguration botConfiguration;
  private final PacketConverter packetConverter;

  private final RabbitMqClient rabbitClient;
  private final Map<String, List<OrderListener>> orderListeners = new HashMap<>();
  private final Map<Class<?>, List<PacketListener<?>>> packetListeners = new HashMap<>();

  public RabbitMqBoticaClient(
      BotTypeConfiguration typeConfiguration,
      BotInstanceConfiguration botConfiguration,
      PacketConverter packetConverter) {
    this.typeConfiguration = typeConfiguration;
    this.botConfiguration = botConfiguration;
    this.packetConverter = packetConverter;
    this.rabbitClient = new RabbitMqClient();
  }

  @Override
  public void connect(BrokerConfiguration brokerConfiguration) throws TimeoutException {
    RabbitMqConfiguration configuration = (RabbitMqConfiguration) brokerConfiguration;
    this.rabbitClient.connect(
        configuration.getUsername(), configuration.getPassword(), CONTAINER_NAME);

    String botOrdersName = String.format(BOT_ORDERS_FORMAT, botConfiguration.getId());
    String botTypeOrdersName = String.format(BOT_TYPE_ORDERS_FORMAT, typeConfiguration.getId());
    this.createOwnQueues(botOrdersName);

    new Thread(() -> this.listenToOrders(botOrdersName)).start();
    new Thread(() -> this.listenToOrders(botTypeOrdersName)).start();
    new Thread(this::listenToPackets).start();
  }

  private void createOwnQueues(String botOrdersName) {
    String protocolIn = String.format(BOT_PROTOCOL_IN_FORMAT, botConfiguration.getId());

    this.rabbitClient.createQueue(botOrdersName);
    // bot_type orders queue is already created by director
    this.rabbitClient.createQueue(protocolIn);
    this.rabbitClient.bind(ORDER_EXCHANGE, botOrdersName, botOrdersName);
    this.rabbitClient.bind(PROTOCOL_EXCHANGE, protocolIn, protocolIn);
  }

  private void listenToOrders(String queue) {
    this.rabbitClient.subscribe(
        queue,
        message -> {
          JSONObject root = new JSONObject(message);
          this.callOrderListeners(root.getString("order"), root.getString("message"));
        });
  }

  private void callOrderListeners(String order, String message) {
    List<OrderListener> listeners = this.orderListeners.get(order);
    if (listeners == null) return;
    for (OrderListener listener : listeners) {
      listener.onMessageReceived(order, message);
    }
  }

  private void listenToPackets() {
    this.rabbitClient.subscribe(
        String.format(BOT_PROTOCOL_IN_FORMAT, botConfiguration.getId()), this::callPacketListeners);
  }

  @SuppressWarnings("unchecked")
  private <P extends Packet> void callPacketListeners(String rawPacket) {
    Packet packet = this.packetConverter.deserialize(rawPacket);
    List<PacketListener<?>> listeners = this.packetListeners.get(packet.getClass());
    if (listeners == null) {
      return;
    }
    for (PacketListener<?> listener : listeners) {
      ((PacketListener<P>) listener).onPacketReceived((P) packet);
    }
  }

  @Override
  public void registerOrderListener(String order, OrderListener listener) {
    this.orderListeners.computeIfAbsent(order, k -> new ArrayList<>()).add(listener);
  }

  @Override
  public void publishOrder(String key, String order, String message) {
    String json = new JSONObject(Map.of("order", order, "message", message)).toString();
    this.rabbitClient.publish(ORDER_EXCHANGE, key, json);
  }

  @Override
  public <P extends Packet> void registerPacketListener(
      Class<P> packetClass, PacketListener<P> listener) {
    this.packetListeners.computeIfAbsent(packetClass, c -> new ArrayList<>()).add(listener);
  }

  @Override
  public void sendPacket(Packet packet) {
    this.rabbitClient.publish(
        PROTOCOL_EXCHANGE,
        String.format(BOT_PROTOCOL_OUT_FORMAT, botConfiguration.getId()),
        packetConverter.serialize(packet));
  }

  @Override
  public void close() {
    this.rabbitClient.closeConnection();
  }
}
