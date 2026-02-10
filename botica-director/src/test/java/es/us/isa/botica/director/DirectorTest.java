package es.us.isa.botica.director;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class DirectorTest {
  @Mock private ConfigurationFileLoader configurationFileLoader;
  @Mock private EnvironmentConfiguration configuration;
  @Mock private BotManager botManager;
  @Mock private BoticaServer boticaServer;
  @Mock private BrokerDeploymentHandler brokerDeploymentHandler;
  @Mock private BotDeploymentHandler botDeploymentHandler;

  @Captor private ArgumentCaptor<Runnable> shutdownCallbackCaptor;

  private Director director;

  @BeforeEach
  void setUp() {
    director =
        new Director(
            configuration,
            configurationFileLoader,
            boticaServer,
            brokerDeploymentHandler,
            botDeploymentHandler,
            botManager);
  }

  @Test
  @DisplayName("start should write resolved configuration file")
  void start_writesResolvedConfigurationFile() throws Exception {
    // Arrange
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      // Act
      director.start();

      // Assert
      mockedFiles.verify(() -> Files.createDirectories(Director.DATA_DIRECTORY));
      verify(configurationFileLoader, times(1)).write(eq(configuration), any(File.class));
      assertThat(director.isRunning()).isTrue();
    }
  }

  @Test
  @DisplayName("start should orchestrate the full startup sequence correctly on success")
  void start_orchestratesFullStartupSequenceOnSuccess() throws Exception {
    // Arrange
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      // Act
      director.start();

      // Assert
      // Configuration written
      mockedFiles.verify(() -> Files.createDirectories(Director.DATA_DIRECTORY));
      verify(configurationFileLoader, times(1)).write(eq(configuration), any(File.class));

      // Orchestration methods called in order
      verify(botDeploymentHandler, times(1)).removePreviousDeployment();
      verify(brokerDeploymentHandler, times(1)).deploy();
      verify(boticaServer, times(1)).start();
      verify(botDeploymentHandler, times(1)).setupInfrastructure();
      verify(botManager, times(1)).deploy();

      assertThat(director.isRunning()).isTrue();
    }
  }

  @Test
  @DisplayName("shutdown with mode should delegate to BotManager")
  void shutdown_withMode_delegatesToBotManager() throws Exception {
    // Arrange
    setField(director, "botManager", botManager);

    // Act
    director.shutdown(ShutdownMode.FORCE);

    // Assert
    verify(botManager, times(1)).shutdownSystem(eq(ShutdownMode.FORCE), any(Runnable.class));
  }

  @Test
  @DisplayName("shutdown with callback should wrap shutdownInfrastructure in BotManager's callback")
  void shutdown_withCallback_wrapsShutdownInfrastructure() throws Exception {
    // Arrange
    BotDeploymentHandler mockBotDeploymentHandler = mock(BotDeploymentHandler.class);
    BrokerDeploymentHandler mockBrokerDeploymentHandler = mock(BrokerDeploymentHandler.class);
    BoticaServer mockServer = mock(BoticaServer.class);
    when(mockServer.isConnected()).thenReturn(true);

    setField(director, "botManager", botManager);
    setField(director, "state", DirectorState.RUNNING);
    setField(director, "botDeploymentHandler", mockBotDeploymentHandler);
    setField(director, "brokerDeploymentHandler", mockBrokerDeploymentHandler);
    setField(director, "server", mockServer);

    Runnable externalCallback = mock(Runnable.class);

    // Act
    director.shutdown(ShutdownMode.REQUEST, externalCallback);
    verify(botManager, times(1))
        .shutdownSystem(eq(ShutdownMode.REQUEST), shutdownCallbackCaptor.capture());
    shutdownCallbackCaptor.getValue().run();

    // Assert
    verify(mockBotDeploymentHandler, times(1)).shutdown();
    verify(mockBrokerDeploymentHandler, times(1)).shutdown();
    verify(mockServer, times(1)).close();
    verify(externalCallback, times(1)).run();
    assertThat(director.getState()).isEqualTo(DirectorState.STOPPED);
  }

  @Test
  @DisplayName("shutdownInfrastructure should shut down all components in order")
  void shutdownInfrastructure_shutsDownAllComponents() throws Exception {
    // Arrange
    BotDeploymentHandler mockBotDeploymentHandler = mock(BotDeploymentHandler.class);
    BrokerDeploymentHandler mockBrokerDeploymentHandler = mock(BrokerDeploymentHandler.class);
    BoticaServer mockServer = mock(BoticaServer.class);
    when(mockServer.isConnected()).thenReturn(true);

    setField(director, "state", DirectorState.RUNNING);
    setField(director, "botDeploymentHandler", mockBotDeploymentHandler);
    setField(director, "brokerDeploymentHandler", mockBrokerDeploymentHandler);
    setField(director, "server", mockServer);

    // Act
    director.shutdownInfrastructure();

    // Assert
    verify(mockBotDeploymentHandler, times(1)).shutdown();
    verify(mockServer, times(1)).close();
    verify(mockBrokerDeploymentHandler, times(1)).shutdown();
    assertThat(director.getState()).isEqualTo(DirectorState.STOPPED);
  }

  @Test
  @DisplayName("shutdownInfrastructure should do nothing if not running")
  void shutdownInfrastructure_doesNothingIfNotRunning() {
    // Arrange
    // Director is not running by default

    // Act
    director.shutdownInfrastructure();

    // Assert
    assertThat(director.getState()).isEqualTo(DirectorState.STOPPED);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
