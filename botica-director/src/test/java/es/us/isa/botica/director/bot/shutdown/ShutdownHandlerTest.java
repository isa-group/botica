package es.us.isa.botica.director.bot.shutdown;

import static es.us.isa.botica.director.bot.shutdown.ShutdownHandler.FORCE_DELAY_MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.configuration.ShutdownConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.BotStatus;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.director.protocol.PacketListener;
import es.us.isa.botica.director.protocol.ResponseTimeoutCallback;
import es.us.isa.botica.protocol.client.ShutdownResponsePacket;
import es.us.isa.botica.protocol.server.ShutdownRequestPacket;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class ShutdownHandlerTest {
  @Mock private Director director;
  @Mock private BotManager botManager;
  @Mock private BoticaServer server;
  @Mock private EnvironmentConfiguration configuration;
  @Mock private ShutdownConfiguration shutdownConfiguration;
  @Mock private Bot bot;

  private ShutdownHandler shutdownHandler;

  @Captor private ArgumentCaptor<PacketListener<ShutdownResponsePacket>> responseListenerCaptor;
  @Captor private ArgumentCaptor<ResponseTimeoutCallback> timeoutCallbackCaptor;

  @BeforeEach
  void setUp() {
    when(director.getConfiguration()).thenReturn(configuration);
    when(configuration.getShutdownConfiguration()).thenReturn(shutdownConfiguration);
    lenient().when(shutdownConfiguration.getTimeout()).thenReturn(5000L);

    when(bot.getId()).thenReturn("test-bot-id");
    lenient().when(bot.getLastKnownStatus()).thenReturn(BotStatus.RUNNING);

    shutdownHandler = new ShutdownHandler(director, botManager, server);
  }

  @Test
  @DisplayName("Should send a shutdown request packet for REQUEST mode")
  void requestShutdown_requestMode_sendsRequestPacket() {
    // Act
    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Assert
    long configurationTimeout = shutdownConfiguration.getTimeout();
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            any(),
            any(),
            eq(configurationTimeout),
            eq(TimeUnit.MILLISECONDS));

    // Verify the packet content
    ArgumentCaptor<ShutdownRequestPacket> packetCaptor =
        ArgumentCaptor.forClass(ShutdownRequestPacket.class);
    verify(server).sendPacket(packetCaptor.capture(), any(), any(), any(), anyLong(), any());
    assertThat(packetCaptor.getValue().isForced()).isFalse();
  }

  @Test
  @DisplayName("Should send a forceful shutdown request packet for FORCE mode")
  void requestShutdown_forceMode_sendsForcefulRequestPacket() {
    // Act
    shutdownHandler.requestShutdown(bot, ShutdownMode.FORCE);

    // Assert
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            any(),
            any(),
            eq(FORCE_DELAY_MILLISECONDS),
            eq(TimeUnit.MILLISECONDS));

    // Verify the packet content
    ArgumentCaptor<ShutdownRequestPacket> packetCaptor =
        ArgumentCaptor.forClass(ShutdownRequestPacket.class);
    verify(server).sendPacket(packetCaptor.capture(), any(), any(), any(), anyLong(), any());
    assertThat(packetCaptor.getValue().isForced()).isTrue();
  }

  @Test
  @DisplayName("handleResponse: REQUEST mode, bot ready, should stop container")
  void handleResponse_requestModeBotReady_stopsContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    ShutdownResponsePacket response = mock(ShutdownResponsePacket.class);
    when(response.isReady()).thenReturn(true);

    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            responseListenerCaptor.capture(),
            any(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    responseListenerCaptor.getValue().onPacketReceived("test-bot-id", response);

    // Assert
    verify(botManager, times(1)).shutdown(eq(bot), eq(ShutdownMode.STOP_CONTAINER));
  }

  @Test
  @DisplayName("handleResponse: REQUEST mode, bot busy, should not stop container")
  void handleResponse_requestModeBotBusy_doesNotStopContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    ShutdownResponsePacket response = mock(ShutdownResponsePacket.class);
    when(response.isReady()).thenReturn(false);

    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            responseListenerCaptor.capture(),
            any(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    responseListenerCaptor.getValue().onPacketReceived("test-bot-id", response);

    // Assert
    verify(botManager, never()).shutdown(any(Bot.class), any(ShutdownMode.class));
  }

  @Test
  @DisplayName("handleResponse: FORCE mode, bot busy, should still stop container")
  void handleResponse_forceModeBotBusy_stopsContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    ShutdownResponsePacket response = mock(ShutdownResponsePacket.class);

    shutdownHandler.requestShutdown(bot, ShutdownMode.FORCE);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            responseListenerCaptor.capture(),
            any(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    responseListenerCaptor.getValue().onPacketReceived("test-bot-id", response);

    // Assert
    verify(botManager, times(1)).shutdown(eq(bot), eq(ShutdownMode.STOP_CONTAINER));
  }

  @Test
  @DisplayName("handleResponse: director not running, should do nothing")
  void handleResponse_directorNotRunning_doesNothing() {
    // Arrange
    when(director.isRunning()).thenReturn(false);
    ShutdownResponsePacket response = mock(ShutdownResponsePacket.class);

    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            responseListenerCaptor.capture(),
            any(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    responseListenerCaptor.getValue().onPacketReceived("test-bot-id", response);

    // Assert
    verify(botManager, never()).shutdown(any(Bot.class), any(ShutdownMode.class));
  }

  @Test
  @DisplayName("handleResponse: bot already stopped, should do nothing")
  void handleResponse_botAlreadyStopped_doesNothing() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    when(bot.getLastKnownStatus()).thenReturn(BotStatus.STOPPED); // Bot is already stopped
    ShutdownResponsePacket response = mock(ShutdownResponsePacket.class);

    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            responseListenerCaptor.capture(),
            any(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    responseListenerCaptor.getValue().onPacketReceived("test-bot-id", response);

    // Assert
    verify(botManager, never()).shutdown(any(Bot.class), any(ShutdownMode.class));
  }

  @Test
  @DisplayName("handleTimeout: REQUEST mode, should not stop container")
  void handleTimeout_requestMode_doesNotStopContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            any(),
            timeoutCallbackCaptor.capture(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    timeoutCallbackCaptor.getValue().onResponseTimeout("test-bot-id");

    // Assert
    verify(botManager, never()).shutdown(any(Bot.class), any(ShutdownMode.class));
  }

  @Test
  @DisplayName("handleTimeout: FORCE mode, should stop container")
  void handleTimeout_forceMode_stopsContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);
    shutdownHandler.requestShutdown(bot, ShutdownMode.FORCE);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            any(),
            timeoutCallbackCaptor.capture(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    timeoutCallbackCaptor.getValue().onResponseTimeout("test-bot-id");

    // Assert
    verify(botManager, times(1)).shutdown(eq(bot), eq(ShutdownMode.STOP_CONTAINER));
  }

  @Test
  @DisplayName("handleTimeout: director not running, should do nothing")
  void handleTimeout_directorNotRunning_doesNothing() {
    // Arrange
    when(director.isRunning()).thenReturn(false);
    shutdownHandler.requestShutdown(bot, ShutdownMode.REQUEST);

    // Explicitly verify the sendPacket call and capture the handlers for this specific test case
    verify(server, times(1))
        .sendPacket(
            any(ShutdownRequestPacket.class),
            eq("test-bot-id"),
            any(),
            timeoutCallbackCaptor.capture(),
            anyLong(),
            any(TimeUnit.class));

    // Act
    timeoutCallbackCaptor.getValue().onResponseTimeout("test-bot-id");

    // Assert
    verify(botManager, never()).shutdown(any(Bot.class), any(ShutdownMode.class));
  }
}
