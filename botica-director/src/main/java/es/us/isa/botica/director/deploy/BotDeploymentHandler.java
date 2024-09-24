package es.us.isa.botica.director.deploy;

import es.us.isa.botica.director.bot.Bot;

/**
 * Interface for handling the deployment of bots.
 *
 * @see DockerJavaBotDeploymentHandler
 */
public interface BotDeploymentHandler {
  /**
   * Shuts down any previous deployment that may not have been properly shut down and could still be
   * running.
   */
  void removePreviousDeployment();

  void setupInfrastructure();

  String createContainer(Bot bot);

  void startContainer(String containerId);

  void stopContainer(String containerId);

  /** Shuts down the container infrastructure. */
  void shutdown();
}
