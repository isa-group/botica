package es.us.isa.botica.director.deploy;

import static es.us.isa.botica.BoticaConstants.BOT_ID_ENV;
import static es.us.isa.botica.BoticaConstants.BOT_TYPE_ENV;
import static es.us.isa.botica.BoticaConstants.BROKER_NETWORK_NAME;
import static es.us.isa.botica.BoticaConstants.CONTAINER_PREFIX;
import static es.us.isa.botica.director.util.StringUtils.buildEnv;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.RestartPolicy;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotMountConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.docker.DockerClientFactory;
import es.us.isa.botica.director.exception.MountNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bot deployment handler using docker-java.
 *
 * @author Alberto Mimbrero
 */
public class DockerJavaBotDeploymentHandler implements BotDeploymentHandler {
  private static final Logger log = LoggerFactory.getLogger(DockerJavaBotDeploymentHandler.class);

  private static final String SHARED_VOLUME_NAME = "shared";
  private static final String SHARED_VOLUME_PATH = "/shared";
  private static final String CONFIGURATION_SECRET_PATH = "/run/secrets/botica-config";

  private final Director director;
  private final File mainConfigurationFile;
  private final MainConfiguration mainConfiguration;
  private final DockerClient dockerClient;

  public DockerJavaBotDeploymentHandler(
      Director director, File mainConfigurationFile, MainConfiguration mainConfiguration) {
    this(
        director,
        mainConfigurationFile,
        mainConfiguration,
        DockerClientFactory.createDockerClient(mainConfiguration.getDockerConfiguration()));
  }

  public DockerJavaBotDeploymentHandler(
      Director director,
      File mainConfigurationFile,
      MainConfiguration mainConfiguration,
      DockerClient dockerClient) {
    this.director = director;
    this.mainConfigurationFile = mainConfigurationFile;
    this.mainConfiguration = mainConfiguration;
    this.dockerClient = dockerClient;
  }

  @Override
  public void removePreviousDeployment() {
    this.removeBotContainers();
    this.removeSharedVolume();
  }

  private void removeBotContainers() {
    List<String> containerNames =
        this.mainConfiguration.getBotTypes().values().stream()
            .flatMap(type -> type.getInstances().values().stream())
            .map(BotInstanceConfiguration::getId)
            .map(this::buildContainerName)
            .collect(Collectors.toList());

    this.dockerClient
        .listContainersCmd()
        .withNameFilter(containerNames)
        .withShowAll(true)
        .exec()
        .stream()
        .peek(container -> log.debug("Removing container {}...", container.getId()))
        .forEach(
            container ->
                this.dockerClient.removeContainerCmd(container.getId()).withForce(true).exec());
  }

  private void removeSharedVolume() {
    this.dockerClient
        .listVolumesCmd()
        .withFilter("name", List.of(this.buildSharedVolumeName()))
        .exec()
        .getVolumes()
        .forEach(volume -> this.dockerClient.removeVolumeCmd(volume.getName()).exec());
  }

  @Override
  public void setupInfrastructure() {
    this.createSharedVolume();
  }

  private void createSharedVolume() {
    this.dockerClient.createVolumeCmd().withName(buildSharedVolumeName()).exec();
  }

  @Override
  public String createContainer(Bot bot) {
    if (!this.director.isRunning()) {
      return null;
    }
    return this.dockerClient
        .createContainerCmd(bot.getTypeConfiguration().getImage())
        .withName(this.buildContainerName(bot.getId()))
        .withEnv(this.buildEnvironmentVariables(bot.getTypeConfiguration(), bot.getConfiguration()))
        .withHostConfig(
            new HostConfig()
                .withNetworkMode(this.buildNetworkName())
                .withMounts(this.buildMounts(bot.getTypeConfiguration()))
                .withRestartPolicy(RestartPolicy.onFailureRestart(0)))
        .exec()
        .getId();
  }

  private List<String> buildEnvironmentVariables(
      BotTypeConfiguration typeConfiguration, BotInstanceConfiguration botConfiguration) {
    List<String> env = new ArrayList<>();
    env.add(buildEnv(BOT_TYPE_ENV, typeConfiguration.getId()));
    env.add(buildEnv(BOT_ID_ENV, botConfiguration.getId()));
    env.addAll(botConfiguration.getEnvironment());
    return env;
  }

  private List<Mount> buildMounts(BotTypeConfiguration typeConfiguration) {
    List<Mount> mounts = this.buildMountsFromConfiguration(typeConfiguration);
    mounts.add(this.buildSharedVolumeMount());
    mounts.add(this.buildConfigurationFileMount());
    return mounts;
  }

  private List<Mount> buildMountsFromConfiguration(BotTypeConfiguration typeConfiguration) {
    List<Mount> mounts = new ArrayList<>();
    for (BotMountConfiguration mount : typeConfiguration.getMounts()) {
      checkMount(typeConfiguration, mount);
      mounts.add(
          new Mount()
              .withType(MountType.BIND)
              .withSource(Path.of(mount.getSource()).toAbsolutePath().toString())
              .withTarget(mount.getTarget()));
    }
    return mounts;
  }

  private Mount buildSharedVolumeMount() {
    return new Mount()
        .withType(MountType.VOLUME)
        .withSource(this.buildSharedVolumeName())
        .withTarget(SHARED_VOLUME_PATH);
  }

  private Mount buildConfigurationFileMount() {
    return new Mount() // actually not a secret but a bind mount, not supporting swarm for now
        .withType(MountType.BIND)
        .withReadOnly(true)
        .withSource(this.mainConfigurationFile.getAbsolutePath())
        .withTarget(CONFIGURATION_SECRET_PATH);
  }

  @Override
  public void startContainer(String containerId) {
    if (!this.director.isRunning()) {
      return;
    }
    this.dockerClient.startContainerCmd(containerId).exec();
  }

  @Override
  public void stopContainer(String containerId) {
    try {
      this.dockerClient.stopContainerCmd(containerId).exec();
    } catch (RuntimeException e) {
      log.debug("Error while stopping a container", e);
    }
  }

  @Override
  public void shutdown() {
    this.removePreviousDeployment();
  }

  private String buildNetworkName() {
    return CONTAINER_PREFIX + BROKER_NETWORK_NAME;
  }

  private String buildSharedVolumeName() {
    return CONTAINER_PREFIX + SHARED_VOLUME_NAME;
  }

  private String buildContainerName(String id) {
    return CONTAINER_PREFIX + id;
  }

  private static void checkMount(BotTypeConfiguration botType, BotMountConfiguration mount) {
    File source = new File(mount.getSource());
    if (source.exists()) {
      return;
    }
    if (!mount.isCreateHostPath() || !source.mkdirs()) {
      try {
        throw new MountNotFoundException(
            String.format(
                "The file or directory at %s does not exist and is required by '%s' bots",
                source.getCanonicalFile().getAbsolutePath(), botType.getId()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
