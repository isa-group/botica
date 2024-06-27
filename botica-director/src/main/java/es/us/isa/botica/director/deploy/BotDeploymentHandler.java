package es.us.isa.botica.director.deploy;

/**
 * Interface for handling the deployment of bots.
 *
 * @see DockerBotDeploymentHandler
 */
public interface BotDeploymentHandler {
  /**
   * Shuts down any previous deployment that may not have been properly shut down and could still be
   * running.
   */
  void removePreviousDeployment();

  /** Deploys all the containerized infrastructure. */
  void deploy();

  /** Stops all the containerized infrastructure. */
  void shutdown();
}
