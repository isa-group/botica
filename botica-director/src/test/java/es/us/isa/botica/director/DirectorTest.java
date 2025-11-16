package es.us.isa.botica.director;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.deploy.DockerJavaBotDeploymentHandler;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.director.protocol.RabbitMqBoticaServer;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class DirectorTest {
  @TempDir Path tempDir;

  @Mock private ConfigurationFileLoader configurationFileLoader;
  @Mock private MainConfiguration mainConfiguration;
  @Mock private ValidationReport validationReport;
  @Mock private BotManager botManager;

  @Captor private ArgumentCaptor<Runnable> shutdownCallbackCaptor;

  private File configFile;
  private Director director;

  @BeforeEach
  void setUp() throws IOException {
    configFile = tempDir.resolve("test-config.yml").toFile();
    Files.createFile(configFile.toPath());
    director = new Director(configFile, configurationFileLoader);

    lenient()
        .when(configurationFileLoader.load(configFile, MainConfiguration.class))
        .thenReturn(mainConfiguration);
    lenient().when(validationReport.hasErrors()).thenReturn(false);
    lenient().when(validationReport.hasWarnings()).thenReturn(false);
  }

  @Test
  @DisplayName("start should throw DirectorException if configuration loading fails")
  void start_throwsDirectorException_onConfigLoadFailure() {
    // Arrange
    ConfigurationLoadingException exception = new ConfigurationLoadingException("Failed to load");
    when(configurationFileLoader.load(configFile, MainConfiguration.class)).thenThrow(exception);

    // Act & Assert
    assertThatThrownBy(() -> director.start())
        .isInstanceOf(DirectorException.class)
        .hasMessage(exception.getMessage());

    assertThat(director.isRunning()).isFalse();
    assertThat(director.getState()).isEqualTo(DirectorState.STARTING);
  }

  @Test
  @DisplayName("start should throw DirectorException if configuration validation fails")
  void start_throwsDirectorException_onValidationFailure() {
    // Arrange
    when(validationReport.hasErrors()).thenReturn(true);
    when(validationReport.render()).thenReturn("Error details");
    try (MockedConstruction<Validator> mockedValidator =
        mockConstruction(
            Validator.class,
            (validator, context) -> when(validator.validate(any())).thenReturn(validationReport))) {
      // Act & Assert
      assertThatThrownBy(() -> director.start())
          .isInstanceOf(DirectorException.class)
          .hasMessageContaining("Error details");
    }
  }

  @Test
  @DisplayName("start should proceed if validation has warnings")
  void start_proceeds_onValidationWarning() throws IOException {
    // Arrange
    when(validationReport.hasWarnings()).thenReturn(true);
    // Mocks to allow the method to complete without throwing
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
        MockedStatic<BrokerDeploymentHandler> mockedBrokerHandler =
            mockStatic(BrokerDeploymentHandler.class);
        MockedConstruction<Validator> mockedValidator =
            mockConstruction(
                Validator.class,
                (validator, context) ->
                    when(validator.validate(any())).thenReturn(validationReport));
        MockedConstruction<RabbitMqBoticaServer> mockedServer =
            mockConstruction(RabbitMqBoticaServer.class);
        MockedConstruction<DockerJavaBotDeploymentHandler> mockedBotHandler =
            mockConstruction(DockerJavaBotDeploymentHandler.class);
        MockedConstruction<BotManager> mockedBotManager = mockConstruction(BotManager.class)) {
      mockedBrokerHandler
          .when(() -> BrokerDeploymentHandler.fromConfig(any()))
          .thenReturn(mock(BrokerDeploymentHandler.class));

      // Act
      director.start();

      // Assert
      verify(mockedBotManager.constructed().get(0), times(1)).deploy();
      assertThat(director.isRunning()).isTrue();
    }
  }

  @Test
  @DisplayName("start should orchestrate the full startup sequence correctly on success")
  void start_orchestratesFullStartupSequenceOnSuccess() throws Exception {
    // Arrange
    // This extensive setup mocks every dependency created inside `start()`
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
        MockedStatic<BrokerDeploymentHandler> mockedBrokerHandlerFactory =
            mockStatic(BrokerDeploymentHandler.class);
        MockedConstruction<Validator> mockedValidator =
            mockConstruction(
                Validator.class,
                (validator, context) ->
                    when(validator.validate(any())).thenReturn(validationReport));
        MockedConstruction<RabbitMqBoticaServer> mockedServer =
            mockConstruction(RabbitMqBoticaServer.class);
        MockedConstruction<DockerJavaBotDeploymentHandler> mockedBotHandler =
            mockConstruction(DockerJavaBotDeploymentHandler.class);
        MockedConstruction<BotManager> mockedBotManager = mockConstruction(BotManager.class)) {
      BrokerDeploymentHandler brokerHandlerInstance = mock(BrokerDeploymentHandler.class);
      mockedBrokerHandlerFactory
          .when(() -> BrokerDeploymentHandler.fromConfig(mainConfiguration))
          .thenReturn(brokerHandlerInstance);

      // Act
      director.start();

      // Assert
      // Configuration loaded, validated, and written
      verify(configurationFileLoader, times(1)).load(configFile, MainConfiguration.class);
      assertThat(mockedValidator.constructed()).hasSize(1);
      verify(mockedValidator.constructed().get(0), times(1)).validate(mainConfiguration);
      mockedFiles.verify(() -> Files.createDirectories(Director.DATA_DIRECTORY));
      verify(configurationFileLoader, times(1))
          .write(mainConfiguration, Director.RESOLVED_CONFIG_FILE);

      // Dependencies constructed
      assertThat(mockedServer.constructed()).hasSize(1);
      assertThat(mockedBotHandler.constructed()).hasSize(1);
      assertThat(mockedBotManager.constructed()).hasSize(1);

      // Orchestration methods called in order
      BotDeploymentHandler botHandlerInstance = mockedBotHandler.constructed().get(0);
      BoticaServer serverInstance = mockedServer.constructed().get(0);
      BotManager botManagerInstance = mockedBotManager.constructed().get(0);

      verify(botHandlerInstance, times(1)).removePreviousDeployment();
      verify(brokerHandlerInstance, times(1)).deploy();
      verify(serverInstance, times(1)).start();
      verify(botHandlerInstance, times(1)).setupInfrastructure();
      verify(botManagerInstance, times(1)).deploy();

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
    java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
