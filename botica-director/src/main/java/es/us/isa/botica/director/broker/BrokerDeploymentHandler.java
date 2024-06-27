package es.us.isa.botica.director.broker;

import com.github.dockerjava.api.DockerClient;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.director.docker.DockerClientFactory;

/**
 * Deployment handler for the internal message broker.
 *
 * @author Alberto Mimbrero
 */
public interface BrokerDeploymentHandler {
  /** Deploys the message broker. */
  void deploy();

  /** Returns whether the message broker is running. */
  boolean isRunning();

  /** Stops the running message broker. */
  void shutdown();

  /**
   * Creates a {@link BrokerDeploymentHandler} instance for the broker type specified in the
   * configuration.
   *
   * @param mainConfiguration the Botica main configuration
   */
  static BrokerDeploymentHandler fromConfig(MainConfiguration mainConfiguration) {
    if (mainConfiguration.getBrokerConfiguration() instanceof RabbitMqConfiguration) {
      DockerClient dockerClient =
          DockerClientFactory.createDockerClient(mainConfiguration.getDockerConfiguration());
      return new DockerRabbitMqDeploymentHandler(dockerClient, mainConfiguration);
    } else {
      throw new UnsupportedOperationException("unsupported broker type");
    }
  }
}
