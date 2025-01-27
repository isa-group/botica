package es.us.isa.botica.director.protocol;

import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_PROTOCOL_IN_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.DIRECTOR_PROTOCOL;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.protocol.Packet;
import es.us.isa.botica.protocol.PacketConverter;
import es.us.isa.botica.protocol.client.BotPacket;
import es.us.isa.botica.protocol.query.QueryHandler;
import es.us.isa.botica.protocol.query.RequestPacket;
import es.us.isa.botica.protocol.query.ResponsePacket;
import es.us.isa.botica.rabbitmq.RabbitMqClient;
import es.us.isa.botica.util.ExecutorUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RabbitMQ botica server implementation.
 *
 * @author Alberto Mimbrero
 */
public class RabbitMqBoticaServer implements BoticaServer {
  private static final Logger log = LoggerFactory.getLogger(RabbitMqBoticaServer.class);

  private final MainConfiguration mainConfiguration;
  private final PacketConverter packetConverter;

  private final QueryHandler queryHandler;
  private final RabbitMqClient rabbitClient;
  private final Map<Class<?>, List<PacketListener<?>>> packetListeners = new HashMap<>();

  public RabbitMqBoticaServer(
      MainConfiguration mainConfiguration, PacketConverter packetConverter) {
    this.mainConfiguration = mainConfiguration;
    this.packetConverter = packetConverter;

    this.queryHandler = new QueryHandler(ExecutorUtils.newDaemonSingleThreadScheduledExecutor());
    this.rabbitClient = new RabbitMqClient();
  }

  @Override
  public void start() throws TimeoutException {
    RabbitMqConfiguration configuration =
        (RabbitMqConfiguration) this.mainConfiguration.getBrokerConfiguration();

    log.info("Waiting for RabbitMQ to start...");
    this.rabbitClient.connect(
        configuration.getUsername(),
        configuration.getPassword(),
        "localhost",
        configuration.getPort());

    this.installProtocol();
  }

  private void installProtocol() {
    this.rabbitClient.createQueue(DIRECTOR_PROTOCOL);
    this.rabbitClient.bind(PROTOCOL_EXCHANGE, DIRECTOR_PROTOCOL, DIRECTOR_PROTOCOL);
    this.rabbitClient.subscribe(DIRECTOR_PROTOCOL, this::callPacketListeners);
  }

  @SuppressWarnings("unchecked")
  private <P extends Packet> void callPacketListeners(String rawPacket) {
    BotPacket wrapper = (BotPacket) this.packetConverter.deserialize(rawPacket);
    String botId = wrapper.getBotId();
    Packet packet = wrapper.getPacket();

    List<PacketListener<?>> listeners = this.packetListeners.get(packet.getClass());
    if (listeners == null) {
      return;
    }
    for (PacketListener<?> listener : listeners) {
      ((PacketListener<P>) listener).onPacketReceived(botId, (P) packet);
    }
  }

  @Override
  public boolean isConnected() {
    return this.rabbitClient.isConnected();
  }

  @Override
  public <P extends Packet> void registerPacketListener(
      Class<P> packetClass, PacketListener<P> listener) {
    this.ensureResponsePacketListener(packetClass);
    this.packetListeners.get(packetClass).add(listener);
  }

  private <P extends Packet> void ensureResponsePacketListener(Class<P> packetClass) {
    List<PacketListener<?>> listeners =
        this.packetListeners.computeIfAbsent(packetClass, c -> new ArrayList<>());
    if (!listeners.isEmpty() || !ResponsePacket.class.isAssignableFrom(packetClass)) {
      return;
    }

    listeners.add(
        (sourceBotId, response) -> {
          try {
            this.queryHandler.acceptResponse((ResponsePacket) response);
          } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
          }
        });
  }

  @Override
  public void sendPacket(Packet packet, String botId) {
    this.rabbitClient.publish(
        PROTOCOL_EXCHANGE,
        String.format(BOT_PROTOCOL_IN_FORMAT, botId),
        packetConverter.serialize(packet));
  }

  @Override
  public <ResponsePacketT extends ResponsePacket> void sendPacket(
      RequestPacket<ResponsePacketT> packet,
      String botId,
      PacketListener<ResponsePacketT> callback,
      ResponseTimeoutCallback timeoutCallback) {
    this.sendPacket(packet, botId, callback, timeoutCallback, 3, TimeUnit.SECONDS);
  }

  @Override
  public <ResponsePacketT extends ResponsePacket> void sendPacket(
      RequestPacket<ResponsePacketT> packet,
      String botId,
      PacketListener<ResponsePacketT> callback,
      ResponseTimeoutCallback timeoutCallback,
      long timeout,
      TimeUnit timeoutUnit) {
    this.ensureResponsePacketListener(packet.getResponsePacketClass());
    this.queryHandler.registerQuery(
        packet,
        response -> callback.onPacketReceived(botId, response),
        () -> timeoutCallback.onResponseTimeout(botId),
        timeout,
        timeoutUnit);
    this.sendPacket(packet, botId);
  }

  @Override
  public void close() {
    this.rabbitClient.closeConnection();
  }
}
