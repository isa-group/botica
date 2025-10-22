package es.us.isa.botica.director;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.deploy.DockerJavaBotDeploymentHandler;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.director.protocol.RabbitMqBoticaServer;
import es.us.isa.botica.protocol.JacksonPacketConverter;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.jackson.JacksonConfigurationFileLoader;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Director {
  public static final Path DATA_DIRECTORY = Path.of(".botica");
  public static final File RESOLVED_CONFIG_FILE =
      DATA_DIRECTORY.resolve("environment.yml").toFile();
  private static final Logger log = LoggerFactory.getLogger(Director.class);

  private final File mainConfigurationFile;
  private final ConfigurationFileLoader configurationFileLoader;
  private MainConfiguration mainConfiguration;

  private BoticaServer server;
  private BrokerDeploymentHandler brokerDeploymentHandler;
  private BotDeploymentHandler botDeploymentHandler;
  private BotManager botManager;

  private boolean running = false;

  public Director(File mainConfigurationFile) {
    this(mainConfigurationFile, new JacksonConfigurationFileLoader());
  }

  public Director(File mainConfigurationFile, ConfigurationFileLoader configurationFileLoader) {
    this.mainConfigurationFile = mainConfigurationFile;
    this.configurationFileLoader = configurationFileLoader;
  }

  /** Starts this director instance. */
  public void start() throws IOException {
    this.running = true;
    log.info("Starting the botica environment!");

    try {
      this.loadConfiguration();
      Files.createDirectories(DATA_DIRECTORY);
      this.configurationFileLoader.write(this.mainConfiguration, RESOLVED_CONFIG_FILE);

      this.server = new RabbitMqBoticaServer(this.mainConfiguration, new JacksonPacketConverter());
      this.brokerDeploymentHandler = BrokerDeploymentHandler.fromConfig(this.mainConfiguration);
      this.botDeploymentHandler =
          new DockerJavaBotDeploymentHandler(this, RESOLVED_CONFIG_FILE, this.mainConfiguration);
      this.botManager = new BotManager(this, this.botDeploymentHandler, this.server);

      this.botDeploymentHandler.removePreviousDeployment();

      log.info("Deploying the internal message broker...");
      this.brokerDeploymentHandler.deploy();
      log.info("Starting the server...");
      this.startServer();
      log.info("Deploying bots...");
      this.botDeploymentHandler.setupInfrastructure();
      this.botManager.deploy();
      log.info("Botica is running! Use the 'stop' command to shut down the environment.");
    } catch (Exception e) {
      this.running = false;
      throw e;
    }
  }

  private void loadConfiguration() {
    try {
      this.mainConfiguration =
          this.configurationFileLoader.load(this.mainConfigurationFile, MainConfiguration.class);
      this.validateConfigurationFile();
    } catch (ConfigurationLoadingException e) {
      throw new DirectorException(e);
    }
  }

  private void validateConfigurationFile() {
    ValidationReport validationReport = new Validator().validate(mainConfiguration);
    if (validationReport.hasErrors()) {
      throw new DirectorException(
          String.format(
              "There are %d errors and %d warnings in your configuration file at %s:\n%s",
              validationReport.countErrors(),
              validationReport.countWarnings(),
              this.mainConfigurationFile.getAbsolutePath(),
              validationReport.render()));
    }
    if (validationReport.hasWarnings()) {
      log.warn(
          "There are {} warnings in your configuration file at {}:\n{}",
          validationReport.countWarnings(),
          this.mainConfigurationFile.getAbsolutePath(),
          validationReport.render());
    }
  }

  private void startServer() {
    try {
      this.server.start();
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
    if (!this.running) return;
    this.running = false;

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

  public MainConfiguration getMainConfiguration() {
    return mainConfiguration;
  }

  public boolean isRunning() {
    return running;
  }
}
