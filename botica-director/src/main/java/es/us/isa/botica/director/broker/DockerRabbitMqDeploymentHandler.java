package es.us.isa.botica.director.broker;

import static es.us.isa.botica.BoticaConstants.BROKER_NETWORK_NAME;
import static es.us.isa.botica.BoticaConstants.CONTAINER_PREFIX;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.CONTAINER_NAME;
import static es.us.isa.botica.director.util.StringUtils.buildEnv;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Containerized RabbitMQ deployment handler using docker-java.
 *
 * @author Alberto Mimbrero
 */
public class DockerRabbitMqDeploymentHandler implements BrokerDeploymentHandler {
  private static final Logger log = LoggerFactory.getLogger(DockerRabbitMqDeploymentHandler.class);

  private static final String RABBITMQ_IMAGE = "rabbitmq:3.13-management";
  private static final String DEFINITIONS_SECRET_PATH = "/run/secrets/definitions";
  private static final ExposedPort DEFAULT_PORT = new ExposedPort(5672);

  private final DockerClient dockerClient;
  private final RabbitMqConfigurationGenerator configurationGenerator;
  private final RabbitMqConfiguration rabbitMqConfiguration;

  public DockerRabbitMqDeploymentHandler(
      DockerClient dockerClient, MainConfiguration mainConfiguration) {
    this.dockerClient = dockerClient;
    this.configurationGenerator = new RabbitMqConfigurationGenerator(mainConfiguration);
    this.rabbitMqConfiguration = (RabbitMqConfiguration) mainConfiguration.getBrokerConfiguration();
  }

  @Override
  public void deploy() {
    try {
      log.debug("Generating RabbitMQ definitions file...");
      this.configurationGenerator.generateDefinitionsFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.removePreviousDeployment();
    this.pullImage();
    this.createNetwork();
    String containerId = this.createContainer();
    this.dockerClient.startContainerCmd(containerId).exec();
  }

  private void removePreviousDeployment() {
    this.dockerClient
        .listContainersCmd()
        .withShowAll(true)
        .withNameFilter(List.of(this.buildContainerName()))
        .exec()
        .stream()
        .peek(container -> log.debug("Removing container {}...", container.getId()))
        .forEach(
            container ->
                this.dockerClient.removeContainerCmd(container.getId()).withForce(true).exec());

    this.dockerClient.listNetworksCmd().withNameFilter(this.buildNetworkName()).exec().stream()
        .peek(network -> log.debug("Removing network {}...", network.getId()))
        .forEach(network -> this.dockerClient.removeNetworkCmd(network.getName()).exec());
  }

  private void pullImage() {
    try {
      log.info("Downloading {}...", RABBITMQ_IMAGE);
      this.dockerClient.pullImageCmd(RABBITMQ_IMAGE).start().awaitCompletion();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void createNetwork() {
    this.dockerClient
        .createNetworkCmd()
        .withName(this.buildNetworkName())
        .withAttachable(true)
        .exec();
  }

  private String createContainer() {
    return this.dockerClient
        .createContainerCmd(RABBITMQ_IMAGE)
        .withName(this.buildContainerName())
        .withEnv(this.buildEnvironmentVariables())
        .withHostConfig(
            HostConfig.newHostConfig()
                .withPortBindings(this.buildPortBindings())
                .withNetworkMode(this.buildNetworkName())
                .withMounts(this.buildMounts()))
        .exec()
        .getId();
  }

  private List<String> buildEnvironmentVariables() {
    return List.of(
        buildEnv(
            "RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
            "-rabbitmq_management load_definitions \"" + DEFINITIONS_SECRET_PATH + "\""));
  }

  private List<PortBinding> buildPortBindings() {
    return List.of(
        new PortBinding(Binding.bindPort(rabbitMqConfiguration.getPort()), DEFAULT_PORT));
  }

  private List<Mount> buildMounts() {
    return List.of(
        new Mount() // actually not a secret but a bind mount, not supporting swarm for now
            .withType(MountType.BIND)
            .withSource(
                RabbitMqConfigurationGenerator.DEFINITIONS_TARGET_PATH.toAbsolutePath().toString())
            .withTarget(DEFINITIONS_SECRET_PATH));
  }

  @Override
  public boolean isRunning() {
    return this.dockerClient
        .listContainersCmd()
        .withNameFilter(List.of(this.buildContainerName()))
        .exec()
        .stream()
        .findFirst() // #anyMatch returns true if the stream is empty
        .filter(container -> container.getState().equalsIgnoreCase("running"))
        .isPresent();
  }

  @Override
  public void shutdown() {
    this.removePreviousDeployment();
  }

  private String buildNetworkName() {
    return CONTAINER_PREFIX + BROKER_NETWORK_NAME;
  }

  private String buildContainerName() {
    return CONTAINER_PREFIX + CONTAINER_NAME;
  }
}
