package es.us.isa.botica.director.protocol;

import static es.us.isa.botica.rabbitmq.RabbitMqConstants.BOT_PROTOCOL_IN_FORMAT;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.DIRECTOR_PROTOCOL;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.PROTOCOL_EXCHANGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.captor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.protocol.Packet;
import es.us.isa.botica.protocol.PacketConverter;
import es.us.isa.botica.protocol.TestPlainPacket;
import es.us.isa.botica.protocol.TestRequestPacket;
import es.us.isa.botica.protocol.TestResponsePacket;
import es.us.isa.botica.protocol.client.BotPacket;
import es.us.isa.botica.protocol.query.QueryHandler;
import es.us.isa.botica.rabbitmq.RabbitMqClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class RabbitMqBoticaServerTest {
  @Mock private MainConfiguration mainConfiguration;
  @Mock private PacketConverter packetConverter;
  @Mock private ExecutorService executorService;
  @Mock private QueryHandler queryHandler;
  @Mock private RabbitMqClient rabbitClient;
  @Mock private RabbitMqConfiguration rabbitMqConfiguration;

  private RabbitMqBoticaServer server;

  @BeforeEach
  void setUp() {
    lenient().when(mainConfiguration.getBrokerConfiguration()).thenReturn(rabbitMqConfiguration);

    lenient()
        .doAnswer(
            invocation -> {
              Runnable runnable = invocation.getArgument(0);
              runnable.run();
              return null;
            })
        .when(executorService)
        .submit(any(Runnable.class));

    server =
        new RabbitMqBoticaServer(
            mainConfiguration, packetConverter, executorService, queryHandler, rabbitClient);
  }

  @Test
  @DisplayName("start should connect to RabbitMQ and install protocol")
  void start_connectsAndInstallsProtocol() throws TimeoutException {
    // Arrange
    when(rabbitMqConfiguration.getUsername()).thenReturn("guest");
    when(rabbitMqConfiguration.getPassword()).thenReturn("guest");
    when(rabbitMqConfiguration.getPort()).thenReturn(5672);

    // Act
    server.start();

    // Assert
    verify(rabbitClient, times(1)).connect(eq("guest"), eq("guest"), eq("localhost"), eq(5672));
    verify(rabbitClient, times(1)).createQueue(eq(DIRECTOR_PROTOCOL));
    verify(rabbitClient, times(1))
        .bind(eq(PROTOCOL_EXCHANGE), eq(DIRECTOR_PROTOCOL), eq(DIRECTOR_PROTOCOL));
    verify(rabbitClient, times(1)).subscribe(eq(DIRECTOR_PROTOCOL), any(), anyInt());
  }

  @Test
  @DisplayName("start should throw TimeoutException if RabbitMQ client fails to connect")
  void start_throwsTimeoutException_onConnectionFailure() throws TimeoutException {
    // Arrange
    doThrow(TimeoutException.class)
        .when(rabbitClient)
        .connect(anyString(), anyString(), anyString(), anyInt());
    when(rabbitMqConfiguration.getUsername()).thenReturn("guest");
    when(rabbitMqConfiguration.getPassword()).thenReturn("guest");
    when(rabbitMqConfiguration.getPort()).thenReturn(5672);

    // Act & Assert
    assertThrows(TimeoutException.class, () -> server.start());

    verify(rabbitClient, never()).createQueue(anyString());
  }

  @Test
  @DisplayName("isConnected should delegate to RabbitMqClient")
  void isConnected_delegatesToRabbitMqClient() {
    // Arrange
    when(rabbitClient.isConnected()).thenReturn(true);

    // Act
    boolean connected = server.isConnected();

    // Assert
    assertThat(connected).isTrue();
    verify(rabbitClient, times(1)).isConnected();
  }

  @Test
  @DisplayName("registerPacketListener should add listeners to the map")
  void registerPacketListener_addsListeners() {
    // Arrange
    PacketListener<TestPlainPacket> listener1 = mock();
    PacketListener<TestPlainPacket> listener2 = mock();

    // Act
    server.registerPacketListener(TestPlainPacket.class, listener1);
    server.registerPacketListener(TestPlainPacket.class, listener2);

    // Assert
    assertThat(server.getPacketListeners()).containsOnlyKeys(TestPlainPacket.class);
    assertThat(server.getPacketListeners().get(TestPlainPacket.class))
        .containsExactly(listener1, listener2);
  }

  @Test
  @DisplayName(
      "registerPacketListener should add QueryHandler listener for ResponsePacket types only once")
  void registerPacketListener_addsQueryHandlerListenerForResponsePacketOnlyOnce() {
    // Arrange
    PacketListener<TestResponsePacket> listener = mock();

    // Act - Register first listener, should add QueryHandler's response listener
    server.registerPacketListener(TestResponsePacket.class, listener);

    // Assert - The internal listener for QueryHandler should have been added
    assertThat(server.getPacketListeners()).containsOnlyKeys(TestResponsePacket.class);
    // Expect 2 listeners: the provided 'listener' and the internal QueryHandler one
    assertThat(server.getPacketListeners().get(TestResponsePacket.class))
        .hasSize(2)
        .contains(listener);

    // Act - Register a second listener for the same type
    PacketListener<TestResponsePacket> secondListener = mock();
    server.registerPacketListener(TestResponsePacket.class, secondListener);

    // Assert that the QueryHandler's listener is still only effectively added once
    assertThat(server.getPacketListeners()).containsOnlyKeys(TestResponsePacket.class);
    assertThat(server.getPacketListeners().get(TestResponsePacket.class))
        .hasSize(3)
        .contains(listener, secondListener);
  }

  @Test
  @DisplayName("callPacketListeners should deserialize, extract and submit to listeners")
  void callPacketListeners_deserializesAndSubmits() throws TimeoutException {
    // Arrange
    server.start();

    TestPlainPacket plainPacket = new TestPlainPacket("hello");
    BotPacket wrapperPacket = new BotPacket("test-bot-id", plainPacket);
    String serializedPlainPacket = "{\"type\":\"plain\", \"message\":\"hello\"}";
    String serializedWrapperPacket =
        "{\"botId\":\"test-bot-id\", \"packet\":" + serializedPlainPacket + "}";

    when(packetConverter.deserialize(serializedWrapperPacket)).thenReturn(wrapperPacket);

    PacketListener<TestPlainPacket> listener = mock();
    server.registerPacketListener(TestPlainPacket.class, listener);

    // Act
    server.callPacketListeners(serializedWrapperPacket);

    // Assert
    verify(packetConverter, times(1)).deserialize(eq(serializedWrapperPacket));
    verify(executorService, times(1)).submit(any(Runnable.class));
    verify(listener, times(1)).onPacketReceived(eq("test-bot-id"), eq(plainPacket));
  }

  @Test
  @DisplayName("callPacketListeners should handle unknown packet types gracefully")
  void callPacketListeners_handlesUnknownPacketTypes() throws TimeoutException {
    // Arrange
    server.start();

    BotPacket wrapperPacket = new BotPacket("test-bot-id", mock(Packet.class));
    String serializedWrapperPacket =
        "{\"botId\":\"test-bot-id\", \"packet\":{\"type\":\"unknown\"}}";

    when(packetConverter.deserialize(serializedWrapperPacket)).thenReturn(wrapperPacket);

    // Act & Assert
    assertThatCode(() -> server.callPacketListeners(serializedWrapperPacket))
        .doesNotThrowAnyException();
    verify(executorService, never()).submit(any(Runnable.class));
  }

  @Test
  @DisplayName("callPacketListeners should submit to multiple listeners for same packet type")
  void callPacketListeners_submitsToMultipleListeners() throws TimeoutException {
    // Arrange
    server.start();

    TestPlainPacket plainPacket = new TestPlainPacket("foo");
    BotPacket wrapperPacket = new BotPacket("test-bot-id", plainPacket);
    String serializedWrapperPacket = "{\"botId\":\"test-bot-id\", \"packet\":{\"type\":\"plain\"}}";

    when(packetConverter.deserialize(serializedWrapperPacket)).thenReturn(wrapperPacket);

    PacketListener<TestPlainPacket> listener1 = mock();
    PacketListener<TestPlainPacket> listener2 = mock();
    server.registerPacketListener(TestPlainPacket.class, listener1);
    server.registerPacketListener(TestPlainPacket.class, listener2);

    // Act
    server.callPacketListeners(serializedWrapperPacket);

    // Assert
    verify(executorService, times(2)).submit(any(Runnable.class));
    verify(listener1, times(1)).onPacketReceived(eq("test-bot-id"), eq(plainPacket));
    verify(listener2, times(1)).onPacketReceived(eq("test-bot-id"), eq(plainPacket));
  }

  @Test
  @DisplayName("sendPacket (simple) should serialize and publish to RabbitMQ")
  void sendPacket_simple_serializesAndPublishes() {
    // Arrange
    TestPlainPacket packet = new TestPlainPacket("simple");
    String serializedPacket = "{\"type\":\"plain\",\"message\":\"simple\"}";
    String routingKey = String.format(BOT_PROTOCOL_IN_FORMAT, "target-bot");

    when(packetConverter.serialize(packet)).thenReturn(serializedPacket);

    // Act
    server.sendPacket(packet, "target-bot");

    // Assert
    verify(packetConverter, times(1)).serialize(eq(packet));
    verify(rabbitClient, times(1))
        .publish(eq(PROTOCOL_EXCHANGE), eq(routingKey), eq(serializedPacket));
  }

  @Test
  @DisplayName("sendPacket (query, default timeout) should delegate to full overload")
  void sendPacket_queryDefaultTimeout_delegatesToFullOverload() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("query");
    PacketListener<TestResponsePacket> callback = mock();
    ResponseTimeoutCallback timeoutCallback = mock(ResponseTimeoutCallback.class);

    // Act
    server.sendPacket(requestPacket, "target-bot", callback, timeoutCallback);

    // Assert
    // Verify that the full overload was called with the correct default timeout
    verify(queryHandler, times(1))
        .registerQuery(eq(requestPacket), any(), any(Runnable.class), eq(3L), eq(TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("sendPacket (query) should register query and send packet")
  void sendPacket_query_registersQueryAndSendsPacket() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("query", "req123");
    PacketListener<TestResponsePacket> callback = mock();
    ResponseTimeoutCallback timeoutCallback = mock(ResponseTimeoutCallback.class);
    String botId = "target-bot";
    long timeout = 10;
    TimeUnit timeoutUnit = TimeUnit.MINUTES;
    String serializedPacket = "{\"type\":\"testRequest\",\"requestData\":\"query\"}";
    String routingKey = String.format(BOT_PROTOCOL_IN_FORMAT, botId);

    when(packetConverter.serialize(requestPacket)).thenReturn(serializedPacket);

    // Act
    server.sendPacket(requestPacket, botId, callback, timeoutCallback, timeout, timeoutUnit);

    // Assert
    verify(queryHandler, times(1))
        .registerQuery(eq(requestPacket), any(), any(), eq(timeout), eq(timeoutUnit));

    // Verify packet serialization and publishing
    verify(packetConverter, times(1)).serialize(eq(requestPacket));
    verify(rabbitClient, times(1))
        .publish(eq(PROTOCOL_EXCHANGE), eq(routingKey), eq(serializedPacket));

    // Also verify that an internal listener for ResponsePacket was ensured
    assertThat(server.getPacketListeners()).containsKey(TestResponsePacket.class);
    assertThat(server.getPacketListeners().get(TestResponsePacket.class))
        .hasSize(1); // Only the internal QueryHandler one initially
  }

  @Test
  @DisplayName("sendPacket (query): registered callback is invoked on response")
  void sendPacket_query_callbackInvokedOnResponse() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("query", "req123");
    PacketListener<TestResponsePacket> callback = mock();
    ResponseTimeoutCallback timeoutCallback = mock(ResponseTimeoutCallback.class);
    String botId = "target-bot";
    long timeout = 10;
    TimeUnit timeoutUnit = TimeUnit.SECONDS;

    // Capture the arguments passed to queryHandler.registerQuery
    ArgumentCaptor<Consumer<TestResponsePacket>> queryResponseConsumerCaptor = captor();
    doNothing()
        .when(queryHandler)
        .registerQuery(
            eq(requestPacket),
            queryResponseConsumerCaptor.capture(),
            any(),
            eq(timeout),
            eq(timeoutUnit));

    // Act - Call sendPacket, which sets up the queryHandler
    server.sendPacket(requestPacket, botId, callback, timeoutCallback, timeout, timeoutUnit);

    // Simulate queryHandler calling its captured response consumer
    TestResponsePacket receivedResponse = new TestResponsePacket("actual response", "req123");
    queryResponseConsumerCaptor.getValue().accept(receivedResponse);

    // Assert
    verify(callback, times(1)).onPacketReceived(eq(botId), eq(receivedResponse));
    verify(timeoutCallback, never()).onResponseTimeout(anyString());
  }

  @Test
  @DisplayName("sendPacket (query): registered timeout callback is invoked on timeout")
  void sendPacket_query_timeoutCallbackInvokedOnTimeout() {
    // Arrange
    TestRequestPacket requestPacket = new TestRequestPacket("query", "req123");
    PacketListener<TestResponsePacket> callback = mock();
    ResponseTimeoutCallback timeoutCallback = mock(ResponseTimeoutCallback.class);
    String botId = "target-bot";
    long timeout = 10;
    TimeUnit timeoutUnit = TimeUnit.SECONDS;

    // Capture the arguments passed to queryHandler.registerQuery
    ArgumentCaptor<Runnable> queryTimeoutRunnableCaptor = captor();
    doNothing()
        .when(queryHandler)
        .registerQuery(
            eq(requestPacket),
            any(),
            queryTimeoutRunnableCaptor.capture(),
            eq(timeout),
            eq(timeoutUnit));

    // Act - Call sendPacket, which sets up the queryHandler
    server.sendPacket(requestPacket, botId, callback, timeoutCallback, timeout, timeoutUnit);

    // Simulate queryHandler calling its captured timeout runnable
    queryTimeoutRunnableCaptor.getValue().run();

    // Assert
    verify(callback, never()).onPacketReceived(anyString(), any());
    verify(timeoutCallback, times(1)).onResponseTimeout(eq(botId));
  }

  @Test
  @DisplayName("close should close RabbitMqClient connection")
  void close_closesRabbitMqClient() {
    // Act
    server.close();

    // Assert
    verify(rabbitClient, times(1)).closeConnection();
  }
}
