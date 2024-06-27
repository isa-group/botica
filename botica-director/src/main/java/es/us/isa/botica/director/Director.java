package es.us.isa.botica.director;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.deploy.DockerBotDeploymentHandler;
import es.us.isa.botica.director.docker.DockerClientFactory;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Director {
  private static final Logger log = LoggerFactory.getLogger(Director.class);

  private final File mainConfigurationFile;
  private BrokerDeploymentHandler brokerDeploymentHandler;
  private BotDeploymentHandler botDeploymentHandler;
  private MainConfiguration mainConfiguration;

  public Director(File mainConfigurationFile) {
    this.mainConfigurationFile = mainConfigurationFile;
  }

  public void init() {
    log.info("Starting the botica environment!");
    this.loadConfiguration();

    this.brokerDeploymentHandler = BrokerDeploymentHandler.fromConfig(mainConfiguration);
    this.botDeploymentHandler =
        new DockerBotDeploymentHandler(
            this.mainConfigurationFile,
            this.mainConfiguration,
            DockerClientFactory.createDockerClient(this.mainConfiguration.getDockerConfiguration()));
    this.start();
  }

  private void loadConfiguration() {
    try {
      ConfigurationFileLoader configurationFileLoader = new JacksonConfigurationFileLoader();
      this.mainConfiguration =
          configurationFileLoader.load(this.mainConfigurationFile, MainConfiguration.class);
    } catch (ConfigurationLoadingException e) {
      throw new DirectorException(e);
    }

    this.validateConfigurationFile();
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

  private void start() {
    botDeploymentHandler.removePreviousDeployment();

    log.info("Deploying the internal message broker...");
    this.brokerDeploymentHandler.deploy();
    log.info("Deploying bots...");
    this.botDeploymentHandler.deploy();
    log.info("Botica is running! Press Ctrl+C to shut down the environment.");

    while (true) {}
  }

  public void stop() {
    if (this.botDeploymentHandler != null) {
      log.info("Shutting down running bots...");
      this.botDeploymentHandler.shutdown();
    }
    if (this.brokerDeploymentHandler != null) {
      log.info("Shutting down the internal message broker...");
      this.brokerDeploymentHandler.shutdown();
    }
    log.info("Botica environment shut down successfully.");
  }
}
