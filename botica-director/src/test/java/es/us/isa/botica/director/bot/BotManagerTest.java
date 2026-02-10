package es.us.isa.botica.director.bot;

import static es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleType.REACTIVE;
import static es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleType.UNMANAGED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.DirectorState;
import es.us.isa.botica.director.bot.shutdown.ShutdownHandler;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.director.protocol.PacketListener;
import es.us.isa.botica.protocol.HeartbeatPacket;
import es.us.isa.botica.protocol.client.ReadyPacket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class BotManagerTest {
  @Mock private Director director;
  @Mock private BotDeploymentHandler deploymentHandler;
  @Mock private BoticaServer server;
  @Mock private ScheduledExecutorService mockExecutorService;
  @Mock private EnvironmentConfiguration configuration;
  @Mock private ShutdownHandler shutdownHandler;

  private BotManager botManager;

  @Captor private ArgumentCaptor<PacketListener<ReadyPacket>> readyPacketListenerCaptor;
  @Captor private ArgumentCaptor<PacketListener<HeartbeatPacket>> heartbeatPacketListenerCaptor;
  @Captor private ArgumentCaptor<Runnable> heartbeatRunnableCaptor;

  private BotTypeConfiguration mockBotTypeConfig;
  private BotInstanceConfiguration mockBotInstanceConfig;

  @BeforeEach
  void setUp() {
    botManager =
        new BotManager(director, deploymentHandler, server, mockExecutorService, shutdownHandler);

    // Capture listeners registered by BotManager in its constructor
    verify(server, times(1))
        .registerPacketListener(eq(ReadyPacket.class), readyPacketListenerCaptor.capture());
    verify(server, times(1))
        .registerPacketListener(eq(HeartbeatPacket.class), heartbeatPacketListenerCaptor.capture());

    // Common mock configurations for deploy tests
    mockBotTypeConfig = mock(BotTypeConfiguration.class);
    lenient().when(mockBotTypeConfig.getId()).thenReturn("type-id-1");
    lenient()
        .when(mockBotTypeConfig.getLifecycleConfiguration())
        .thenReturn(mock(BotLifecycleConfiguration.class));

    mockBotInstanceConfig = mock(BotInstanceConfiguration.class);
    lenient().when(mockBotInstanceConfig.getId()).thenReturn("bot-id-1");
    lenient().when(mockBotInstanceConfig.getTypeConfiguration()).thenReturn(mockBotTypeConfig);
  }

  @Test
  @DisplayName("Should deploy all bots from configuration and schedule heartbeat")
  void deploy_shouldDeployAllBotsAndScheduleHeartbeat() {
    // Arrange
    when(director.getState()).thenReturn(DirectorState.STARTING);
    when(director.getConfiguration()).thenReturn(configuration);

    // Setup bot type configuration for deployment
    when(configuration.getBotTypes()).thenReturn(Map.of("type-id-1", mockBotTypeConfig));
    when(mockBotTypeConfig.buildInstances()).thenReturn(List.of(mockBotInstanceConfig));

    when(deploymentHandler.createContainer(any(Bot.class))).thenReturn("container-id-1");
    when(mockBotTypeConfig.getLifecycleConfiguration().getType()).thenReturn(REACTIVE);

    // Act
    botManager.deploy();

    // Assert
    verify(deploymentHandler, times(1)).createContainer(any(Bot.class));
    verify(deploymentHandler, times(1)).startContainer(eq("container-id-1"));

    Bot deployedBot = botManager.getBot("bot-id-1");
    assertThat(deployedBot).isNotNull();
    assertThat(deployedBot.getContainerId()).isEqualTo("container-id-1");
    assertThat(deployedBot.getLastKnownStatus()).isEqualTo(BotStatus.STARTING);

    verify(mockExecutorService, times(1))
        .scheduleAtFixedRate(
            heartbeatRunnableCaptor.capture(), anyLong(), anyLong(), eq(TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("Should set UNMANAGED status for unmanaged bot types during deployment")
  void deploy_shouldSetUnmanagedStatusForUnmanagedBots() {
    // Arrange
    when(director.getState()).thenReturn(DirectorState.STARTING);
    when(director.getConfiguration()).thenReturn(configuration);

    when(configuration.getBotTypes()).thenReturn(Map.of("type-id-1", mockBotTypeConfig));
    when(mockBotTypeConfig.buildInstances()).thenReturn(List.of(mockBotInstanceConfig));

    when(deploymentHandler.createContainer(any(Bot.class))).thenReturn("container-id-1");
    when(mockBotTypeConfig.getLifecycleConfiguration().getType())
        .thenReturn(UNMANAGED); // Specific for this test

    // Act
    botManager.deploy();

    // Assert
    Bot deployedBot = botManager.getBot("bot-id-1");
    assertThat(deployedBot.getLastKnownStatus()).isEqualTo(BotStatus.UNMANAGED);
  }

  @Test
  @DisplayName("Should register a bot")
  void register_shouldAddBotToManager() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);

    // Act
    botManager.register(bot);

    // Assert
    assertThat(botManager.getBot(bot.getId())).isEqualTo(bot);
  }

  @Test
  @DisplayName("Should not deploy bot if director is not running")
  void deploy_singleBot_shouldNotDeployIfNotRunning() {
    // Arrange
    when(director.getState()).thenReturn(DirectorState.STOPPED);
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);

    // Act
    botManager.deploy(bot);

    // Assert
    verify(deploymentHandler, never()).createContainer(any(Bot.class));
    verify(deploymentHandler, never()).startContainer(any(String.class));
    assertThat(bot.getContainerId()).isNull();
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.STOPPED);
  }

  @Test
  @DisplayName("Heartbeat should mark unresponsive bots as UNKNOWN and send new heartbeat")
  void heartbeat_shouldMarkBotsAsUnknownAndSendHeartbeat() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot1.setContainerId("container-1");
    bot1.setLastKnownStatus(BotStatus.RUNNING);
    // Simulate an old heartbeat (e.g., 10 seconds ago)
    Instant oldHeartbeat = Instant.now().minus(10, ChronoUnit.SECONDS);
    bot1.setLastHeartbeat(oldHeartbeat);
    botManager.register(bot1);
    botManager.startHeartbeatScheduler();

    // Act: Directly invoke the captured heartbeat runnable
    // Capture the runnable and simulate its execution
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(mockExecutorService)
        .scheduleAtFixedRate(runnableCaptor.capture(), anyLong(), anyLong(), any(TimeUnit.class));

    // Execute the captured runnable
    runnableCaptor.getValue().run();

    // Assert
    assertThat(bot1.getLastKnownStatus()).isEqualTo(BotStatus.UNKNOWN);

    String botId = bot1.getId();
    verify(server, times(1)).sendPacket(any(HeartbeatPacket.class), eq(botId));
  }

  @Test
  @DisplayName("Heartbeat should not mark already UNKNOWN bots again")
  void heartbeat_shouldNotMarkAlreadyUnknownBots() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot1.setContainerId("container-1");
    bot1.setLastKnownStatus(BotStatus.UNKNOWN); // Already unknown
    Instant oldHeartbeat = Instant.now().minus(10, ChronoUnit.SECONDS);
    bot1.setLastHeartbeat(oldHeartbeat);
    botManager.register(bot1);
    botManager.startHeartbeatScheduler();

    // Act: Directly invoke the captured heartbeat runnable
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(mockExecutorService)
        .scheduleAtFixedRate(runnableCaptor.capture(), anyLong(), anyLong(), any(TimeUnit.class));
    runnableCaptor.getValue().run();

    // Assert
    assertThat(bot1.getLastKnownStatus()).isEqualTo(BotStatus.UNKNOWN); // Remains UNKNOWN

    String botId = bot1.getId();
    verify(server, times(1)).sendPacket(any(HeartbeatPacket.class), eq(botId));
  }

  @Test
  @DisplayName("Heartbeat should not affect healthy bots")
  void heartbeat_shouldNotAffectHealthyBots() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot1.setContainerId("container-1");
    bot1.setLastKnownStatus(BotStatus.RUNNING);
    Instant recentHeartbeat = Instant.now().minus(1, ChronoUnit.SECONDS);
    bot1.setLastHeartbeat(recentHeartbeat);
    botManager.register(bot1);
    botManager.startHeartbeatScheduler();

    // Act: Directly invoke the captured heartbeat runnable
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    verify(mockExecutorService)
        .scheduleAtFixedRate(runnableCaptor.capture(), anyLong(), anyLong(), any(TimeUnit.class));
    runnableCaptor.getValue().run();

    // Assert
    assertThat(bot1.getLastKnownStatus()).isEqualTo(BotStatus.RUNNING); // Stays RUNNING

    String botId = bot1.getId();
    verify(server, times(1)).sendPacket(any(HeartbeatPacket.class), eq(botId));
  }

  @Test
  @DisplayName("Should shutdown all running bots and execute callback")
  void shutdownSystem_shouldShutdownAllRunningBotsAndExecuteCallback() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot1.setContainerId("container-1");
    bot1.setLastKnownStatus(BotStatus.RUNNING);
    botManager.register(bot1);

    BotInstanceConfiguration mockBotInstanceConfig2 = mock(BotInstanceConfiguration.class);
    when(mockBotInstanceConfig2.getId()).thenReturn("bot-id-2");
    Bot bot2 = new Bot(mockBotTypeConfig, mockBotInstanceConfig2);
    bot2.setContainerId("container-2");
    bot2.setLastKnownStatus(BotStatus.STARTING);
    botManager.register(bot2);

    BotInstanceConfiguration mockBotInstanceConfig3 = mock(BotInstanceConfiguration.class);
    when(mockBotInstanceConfig3.getId()).thenReturn("bot-id-3");
    Bot bot3 = new Bot(mockBotTypeConfig, mockBotInstanceConfig3);
    bot3.setContainerId("container-3");
    bot3.setLastKnownStatus(BotStatus.STOPPED); // Should be filtered out
    botManager.register(bot3);

    Runnable callback = mock(Runnable.class);

    // Act
    botManager.shutdownSystem(ShutdownMode.REQUEST, callback);

    // Assert
    verify(shutdownHandler, times(1)).requestShutdown(eq(bot1), eq(ShutdownMode.REQUEST));
    verify(shutdownHandler, times(1)).requestShutdown(eq(bot2), eq(ShutdownMode.REQUEST));
    verify(shutdownHandler, never())
        .requestShutdown(eq(bot3), eq(ShutdownMode.REQUEST)); // Stopped bot
    verify(callback, never()).run(); // Not all bots stopped yet
  }

  @Test
  @DisplayName("Shutdown system callback should execute when all bots are stopped")
  void shutdownSystem_callbackExecutedWhenAllBotsStopped() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot1.setContainerId("container-1");
    bot1.setLastKnownStatus(BotStatus.RUNNING);
    botManager.register(bot1);

    Runnable callback = mock(Runnable.class);

    // Make `shutdownHandler.requestShutdown` immediately trigger `botManager.shutdown`
    // which then sets bot1 to STOPPED.
    doAnswer(
            invocation -> {
              Bot bot = invocation.getArgument(0);
              bot.setLastKnownStatus(BotStatus.STOPPED);
              return null;
            })
        .when(shutdownHandler)
        .requestShutdown(any(Bot.class), any(ShutdownMode.class));

    // Act
    botManager.shutdownSystem(ShutdownMode.REQUEST, callback);

    // Assert - verify that `shutdown` called on each applicable bot
    verify(shutdownHandler, times(1)).requestShutdown(eq(bot1), eq(ShutdownMode.REQUEST));

    // Now all bots are "stopped" from the perspective of the manager.
    // The callback should have been triggered by the last shutdown operation within the manager.
    verify(callback, times(1)).run();
    assertThat(bot1.getLastKnownStatus()).isEqualTo(BotStatus.STOPPED);
  }

  @Test
  @DisplayName("Should handle shutdown for REQUEST mode")
  void shutdown_requestMode_shouldDelegateToShutdownHandler() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.RUNNING);
    botManager.register(bot);

    // Act
    botManager.shutdown(bot, ShutdownMode.REQUEST);

    // Assert
    verify(shutdownHandler, times(1)).requestShutdown(eq(bot), eq(ShutdownMode.REQUEST));
    verify(deploymentHandler, never()).stopContainer(any(String.class));
    assertThat(bot.getLastKnownStatus())
        .isEqualTo(BotStatus.RUNNING); // Status is not changed immediately
  }

  @Test
  @DisplayName("Should handle shutdown for FORCE mode")
  void shutdown_forceMode_shouldDelegateToShutdownHandler() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.RUNNING);
    botManager.register(bot);

    // Act
    botManager.shutdown(bot, ShutdownMode.FORCE);

    // Assert
    verify(shutdownHandler, times(1)).requestShutdown(eq(bot), eq(ShutdownMode.FORCE));
    verify(deploymentHandler, never()).stopContainer(any(String.class));
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.RUNNING);
  }

  @Test
  @DisplayName("Should handle shutdown for STOP_CONTAINER mode")
  void shutdown_stopContainerMode_shouldStopContainerAndSetStatus() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.RUNNING);
    botManager.register(bot);

    // Act
    botManager.shutdown(bot, ShutdownMode.STOP_CONTAINER);

    // Assert
    verify(deploymentHandler, times(1)).stopContainer(eq("container-id-1"));
    verify(shutdownHandler, never()).requestShutdown(any(), any());
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.STOPPED);
  }

  @Test
  @DisplayName("On bot ready, should update status and heartbeat")
  void onBotReady_shouldUpdateBotStatusAndHeartbeat() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.STARTING);
    botManager.register(bot);

    Instant beforeReady = Instant.now();
    ReadyPacket readyPacket = mock(ReadyPacket.class);

    // Act
    PacketListener<ReadyPacket> listener = readyPacketListenerCaptor.getValue();
    listener.onPacketReceived(bot.getId(), readyPacket);

    // Assert
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.RUNNING);
    assertThat(bot.getLastHeartbeat()).isAfterOrEqualTo(beforeReady);
  }

  @Test
  @DisplayName("On bot heartbeat, should update heartbeat and set status to RUNNING if not already")
  void onBotHeartbeat_shouldUpdateHeartbeatAndSetStatusToRunning() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.UNKNOWN);
    botManager.register(bot);

    Instant beforeHeartbeat = Instant.now();
    HeartbeatPacket heartbeatPacket = mock(HeartbeatPacket.class);

    // Act
    PacketListener<HeartbeatPacket> listener = heartbeatPacketListenerCaptor.getValue();
    listener.onPacketReceived(bot.getId(), heartbeatPacket);

    // Assert
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.RUNNING);
    assertThat(bot.getLastHeartbeat()).isAfterOrEqualTo(beforeHeartbeat);
  }

  @Test
  @DisplayName("On bot heartbeat, should update heartbeat but not change status if already RUNNING")
  void onBotHeartbeat_shouldOnlyUpdateHeartbeatIfAlreadyRunning() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    bot.setContainerId("container-id-1");
    bot.setLastKnownStatus(BotStatus.RUNNING);
    Instant initialHeartbeat = Instant.now().minusSeconds(10);
    bot.setLastHeartbeat(initialHeartbeat);
    botManager.register(bot);

    HeartbeatPacket heartbeatPacket = mock(HeartbeatPacket.class);

    // Act
    PacketListener<HeartbeatPacket> listener = heartbeatPacketListenerCaptor.getValue();
    listener.onPacketReceived(bot.getId(), heartbeatPacket);

    // Assert
    assertThat(bot.getLastKnownStatus()).isEqualTo(BotStatus.RUNNING); // Stays RUNNING
    assertThat(bot.getLastHeartbeat()).isAfter(initialHeartbeat); // Heartbeat updated
  }

  @Test
  @DisplayName("Should return bot by ID")
  void getBot_shouldReturnCorrectBot() {
    // Arrange
    Bot bot = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    botManager.register(bot);

    // Act & Assert
    assertThat(botManager.getBot(bot.getId())).isEqualTo(bot);
    assertThat(botManager.getBot("non-existent")).isNull();
  }

  @Test
  @DisplayName("Should return all registered bots")
  void getBots_shouldReturnAllBots() {
    // Arrange
    Bot bot1 = new Bot(mockBotTypeConfig, mockBotInstanceConfig);
    botManager.register(bot1);

    BotInstanceConfiguration mockBotInstanceConfig2 = mock(BotInstanceConfiguration.class);
    when(mockBotInstanceConfig2.getId()).thenReturn("bot-id-2");
    Bot bot2 = new Bot(mockBotTypeConfig, mockBotInstanceConfig2);
    botManager.register(bot2);

    // Act
    Collection<Bot> bots = botManager.getBots();

    // Assert
    assertThat(bots).containsExactlyInAnyOrder(bot1, bot2);
  }
}
