package es.us.isa.botica.director;

import es.us.isa.botica.director.broker.BrokerDeploymentHandler;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.configuration.MainConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Director {
  private static final Logger log = LoggerFactory.getLogger(Director.class);

  private final BrokerDeploymentHandler brokerDeploymentHandler;
  private final BotDeploymentHandler botDeploymentHandler;
  private final MainConfiguration mainConfiguration;

  public Director(
      BrokerDeploymentHandler brokerDeploymentHandler,
      BotDeploymentHandler botDeploymentHandler,
      MainConfiguration mainConfiguration) {
    this.brokerDeploymentHandler = brokerDeploymentHandler;
    this.botDeploymentHandler = botDeploymentHandler;
    this.mainConfiguration = mainConfiguration;
  }

  public void init() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    log.info("Starting the botica environment!");
    botDeploymentHandler.removePreviousDeployment();

    log.info("Deploying the internal broker instance...");
    this.brokerDeploymentHandler.deploy();
    log.info("Broker deployed successfully.");
    log.info("Deploying all the bots...");
    this.botDeploymentHandler.deploy();
    log.info("Botica is running! Press Ctrl+C to shut down the environment.");

    while (true) {}
  }

  public void stop() {
    log.info("Shutting down running bots...");
    this.botDeploymentHandler.shutdown();
    log.info("All bots shut down correctly. Shutting down the broker...");
    this.brokerDeploymentHandler.shutdown();
    log.info("Botica environment shut down successfully.");
  }
}
