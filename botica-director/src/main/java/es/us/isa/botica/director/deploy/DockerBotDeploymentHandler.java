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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bot deployment handler using docker-java.
 *
 * @author Alberto Mimbrero
 */
public class DockerBotDeploymentHandler implements BotDeploymentHandler {
  private static final Logger log = LoggerFactory.getLogger(DockerBotDeploymentHandler.class);

  private static final String SHARED_VOLUME_NAME = "shared";
  private static final String SHARED_VOLUME_PATH = "/app/shared";
  private static final String CONFIGURATION_SECRET_PATH = "/run/secrets/botica-config";

  private final File mainConfigurationFile;
  private final MainConfiguration mainConfiguration;
  private final DockerClient dockerClient;

  private final Map<String, String> botContainerIds = new HashMap<>();

  public DockerBotDeploymentHandler(
      File mainConfigurationFile, MainConfiguration mainConfiguration, DockerClient dockerClient) {
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
    List<String> containerNames = this.mainConfiguration.getBotTypes().values().stream()
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
  public void deploy() {
    this.createSharedVolume();
    this.createBotContainers();
    this.startBotContainers();
  }

  private void createSharedVolume() {
    this.dockerClient.createVolumeCmd().withName(buildSharedVolumeName()).exec();
  }

  private void createBotContainers() {
    for (BotTypeConfiguration typeConfiguration : this.mainConfiguration.getBotTypes().values()) {
      for (BotInstanceConfiguration botConfiguration : typeConfiguration.getInstances().values()) {
        String containerId = this.createContainer(typeConfiguration, botConfiguration);
        this.botContainerIds.put(botConfiguration.getId(), containerId);
      }
    }
  }

  private String createContainer(BotTypeConfiguration type, BotInstanceConfiguration bot) {
    log.info("Creating {}...", bot.getId());
    return this.dockerClient
        .createContainerCmd(type.getImage())
        .withName(this.buildContainerName(bot.getId()))
        .withEnv(this.buildEnvironmentVariables(type, bot))
        .withHostConfig(
            new HostConfig()
                .withNetworkMode(this.buildNetworkName())
                .withMounts(this.buildMounts(type))
                .withRestartPolicy(RestartPolicy.alwaysRestart()))
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
      // TODO check if not exists
      String source = new File(mount.getSource()).getAbsolutePath();
      mounts.add(
          new Mount().withType(MountType.BIND).withSource(source).withTarget(mount.getTarget()));
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

  private void startBotContainers() {
    for (BotTypeConfiguration typeConfiguration : this.mainConfiguration.getBotTypes().values()) {
      for (BotInstanceConfiguration botConfiguration : typeConfiguration.getInstances().values()) {
        log.info("Starting {}...", botConfiguration.getId());
        this.dockerClient
            .startContainerCmd(this.botContainerIds.get(botConfiguration.getId()))
            .exec();
      }
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
}
