package es.us.isa.botica.director;

import static java.util.concurrent.CompletableFuture.runAsync;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.deploy.dockerjava.DockerJavaBotDeploymentHandler;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.director.protocol.RabbitMqBoticaServer;
import es.us.isa.botica.protocol.JacksonPacketConverter;
import es.us.isa.botica.util.ExecutorUtils;
import es.us.isa.botica.util.FutureUtils;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.jackson.JacksonConfigurationFileLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Director {
  private static final Logger log = LoggerFactory.getLogger(Director.class);
  public static final Path DATA_DIRECTORY = Path.of(".botica");

  private final EnvironmentConfiguration configuration;
  private final ConfigurationFileLoader configurationFileLoader;
  private final File resolvedConfigurationFile;

  private final BoticaServer server;
  private final BrokerDeploymentHandler brokerDeploymentHandler;
  private final BotDeploymentHandler botDeploymentHandler;
  private final BotManager botManager;

  private DirectorState state = DirectorState.STOPPED;

  public Director(EnvironmentConfiguration configuration, Path workingPath) {
    this.configuration = configuration;
    this.configurationFileLoader = new JacksonConfigurationFileLoader();
    this.resolvedConfigurationFile = DATA_DIRECTORY.resolve("environment.yml").toFile();
    this.server = new RabbitMqBoticaServer(configuration, new JacksonPacketConverter());
    this.brokerDeploymentHandler = BrokerDeploymentHandler.fromConfig(configuration);
    this.botDeploymentHandler =
        new DockerJavaBotDeploymentHandler(
            this, configuration, this.resolvedConfigurationFile, workingPath);
    this.botManager = new BotManager(this, this.botDeploymentHandler, this.server);
  }

  public Director(
      EnvironmentConfiguration configuration,
      ConfigurationFileLoader configurationFileLoader,
      BoticaServer server,
      BrokerDeploymentHandler brokerDeploymentHandler,
      BotDeploymentHandler botDeploymentHandler,
      BotManager botManager) {
    this.configuration = configuration;
    this.configurationFileLoader = configurationFileLoader;
    this.resolvedConfigurationFile = DATA_DIRECTORY.resolve("environment.yml").toFile();
    this.server = server;
    this.brokerDeploymentHandler = brokerDeploymentHandler;
    this.botDeploymentHandler = botDeploymentHandler;
    this.botManager = botManager;
  }

  /** Starts this director instance. */
  public void start() throws IOException {
    log.info("Starting the botica environment!");
    this.state = DirectorState.STARTING;

    Files.createDirectories(DATA_DIRECTORY);
    this.configurationFileLoader.write(this.configuration, this.resolvedConfigurationFile);

    this.botDeploymentHandler.removePreviousDeployment();

    ExecutorService startupExecutor = ExecutorUtils.newDaemonFixedThreadPool(2);
    try {
      FutureUtils.awaitCompletion(
          runAsync(this::startServer, startupExecutor),
          runAsync(botDeploymentHandler::buildBotImages, startupExecutor));
    } finally {
      startupExecutor.shutdown();
    }

    log.info("Deploying bots...");
    this.botDeploymentHandler.setupInfrastructure();
    this.botManager.deploy();

    this.state = DirectorState.RUNNING;
    log.info("Botica is running! Use the 'stop' command to shut down the environment.");
  }

  private void startServer() {
    try {
      log.info("Deploying the internal message broker...");
      this.brokerDeploymentHandler.deploy();
      log.info("Starting the server...");
      this.server.start();
      log.info("Server started.");
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gracefully shuts down the infrastructure, sending a shutdown signal to running bots.
   *
   * <p>This is equivalent to calling {@code shutdown(shutdownMode, null)}.
   *
   * @see #shutdownInfrastructure()
   */
  public void shutdown(ShutdownMode shutdownMode) {
    this.shutdown(shutdownMode, null);
  }

  /**
   * Gracefully shuts down the infrastructure, sending a shutdown signal to running bots.
   *
   * @param callback the callback to be executed after the infrastructure shutdown, or {@code null}
   *     if none.
   * @see #shutdownInfrastructure()
   */
  public void shutdown(ShutdownMode shutdownMode, Runnable callback) {
    this.botManager.shutdownSystem(
        shutdownMode,
        () -> {
          shutdownInfrastructure();
          if (callback != null) callback.run();
        });
  }

  /**
   * Shuts down the infrastructure, without sending a shutdown signal to running bots.
   *
   * @see #shutdown(ShutdownMode)
   */
  public void shutdownInfrastructure() {
    if (this.state == DirectorState.STOPPED) return;
    this.state = DirectorState.STOPPED;

    if (this.botDeploymentHandler != null) {
      log.info("Shutting down the container infrastructure...");
      this.botDeploymentHandler.shutdown();
    }
    if (this.server != null && this.server.isConnected()) {
      log.info("Stopping the server...");
      this.server.close();
    }
    if (this.brokerDeploymentHandler != null) {
      log.info("Shutting down the internal message broker...");
      this.brokerDeploymentHandler.shutdown();
    }
    log.info("Botica environment shut down successfully!");
  }

  public EnvironmentConfiguration getConfiguration() {
    return configuration;
  }

  public boolean isRunning() {
    return this.state == DirectorState.RUNNING;
  }

  public DirectorState getState() {
    return state;
  }
}
