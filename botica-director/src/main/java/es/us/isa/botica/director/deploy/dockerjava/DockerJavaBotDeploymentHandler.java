package es.us.isa.botica.director.deploy.dockerjava;

import static es.us.isa.botica.BoticaConstants.BOT_ID_ENV;
import static es.us.isa.botica.BoticaConstants.BOT_TYPE_ENV;
import static es.us.isa.botica.BoticaConstants.BROKER_NETWORK_NAME;
import static es.us.isa.botica.BoticaConstants.CONTAINER_PREFIX;
import static es.us.isa.botica.util.StringUtils.buildEnv;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.RestartPolicy;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotMountConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.DirectorState;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.docker.DockerClientFactory;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.exception.MountNotFoundException;
import es.us.isa.botica.util.ExecutorUtils;
import es.us.isa.botica.util.FutureUtils;
import es.us.isa.botica.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
  private static final int BUILD_THREAD_POOL_SIZE =
      Math.min(4, Runtime.getRuntime().availableProcessors());
  private static final int BUILD_LOG_INTERVAL_SECONDS = 10;
  private static final int ERROR_LOG_TAIL_SIZE = 50;

  private final Director director;
  private final File configurationFile;
  private final File resolvedConfigurationFile;
  private final MainConfiguration mainConfiguration;
  private final DockerClient dockerClient;

  public DockerJavaBotDeploymentHandler(
      Director director,
      File configurationFile,
      File resolvedConfigurationFile,
      MainConfiguration mainConfiguration) {
    this(
        director,
        configurationFile,
        resolvedConfigurationFile,
        mainConfiguration,
        DockerClientFactory.createDockerClient(mainConfiguration.getDockerConfiguration()));
  }

  public DockerJavaBotDeploymentHandler(
      Director director,
      File configurationFile,
      File resolvedConfigurationFile,
      MainConfiguration mainConfiguration,
      DockerClient dockerClient) {
    this.director = director;
    this.configurationFile = configurationFile.getAbsoluteFile();
    this.resolvedConfigurationFile = resolvedConfigurationFile;
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
            .flatMap(type -> type.buildInstances().stream())
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

  public void buildBotImages() {
    List<BotTypeConfiguration> botsToBuild =
        this.mainConfiguration.getBotTypes().values().stream()
            .filter(botType -> botType.getBuild() != null && !botType.getBuild().isBlank())
            .collect(Collectors.toList());

    if (botsToBuild.isEmpty()) {
      log.debug("No bots to build from source.");
      return;
    }

    log.info("Building Docker images for {} bot type(s)...", botsToBuild.size());
    ExecutorService buildExecutor = ExecutorUtils.newDaemonFixedThreadPool(BUILD_THREAD_POOL_SIZE);
    try {
      List<CompletableFuture<Void>> buildFutures =
          botsToBuild.stream()
              .map(botType -> CompletableFuture.runAsync(() -> buildImage(botType), buildExecutor))
              .collect(Collectors.toList());

      FutureUtils.awaitCompletion(buildFutures);
    } finally {
      buildExecutor.shutdown();
    }
  }

  private void buildImage(BotTypeConfiguration botType) {
    String buildPath = botType.getBuild();
    Path buildContext = this.configurationFile.toPath().getParent().resolve(buildPath);

    if (!Files.isDirectory(buildContext)) {
      throw new DirectorException(
          String.format(
              "Build path '%s' for bot type '%s' does not exist or is not a directory.",
              buildContext.toAbsolutePath(), botType.getId()));
    }

    log.info("[{}] Preparing image...", botType.getId());

    try {
      String imageTag = generateImageTag(botType);
      BuildImageCmd buildImageCmd =
          dockerClient
              .buildImageCmd()
              .withDockerfile(buildContext.resolve("Dockerfile").toFile())
              .withPull(true)
              .withBaseDirectory(buildContext.toFile())
              .withTags(Set.of(imageTag))
              .withForcerm(true);
      BuildResult result = new BuildResult(botType.getId(), ERROR_LOG_TAIL_SIZE);
      buildImageCmd.exec(result);

      while (!result.awaitHeartbeat(BUILD_LOG_INTERVAL_SECONDS, TimeUnit.SECONDS)) {
        result.logHeartbeat();
      }
      result.awaitCompletion();

      if (result.hasError()) {
        result.logBuildFailure();
        throw new DirectorException(
            String.format(
                "An error occurred while building image for bot '%s'. Check the logs for details.",
                botType.getId()));
      }

      result.logSummary();
      botType.setImage(imageTag);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DirectorException(
          String.format("Build process for bot '%s' was interrupted.", botType.getId()), e);
    }
  }

  private String generateImageTag(BotTypeConfiguration botType) {
    try {
      Path projectRoot = this.configurationFile.getParentFile().getCanonicalFile().toPath();
      String projectName = projectRoot.getFileName().toString().toLowerCase();

      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      byte[] pathHashBytes = digest.digest(projectRoot.toString().getBytes(StandardCharsets.UTF_8));

      String projectHash = StringUtils.bytesToHex(pathHashBytes).substring(0, 8);
      String namespace = String.format("%s-%s", projectName, projectHash);
      String botName = botType.getId().toLowerCase();

      return String.format("%s/%s:latest", namespace, botName);
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new DirectorException("Failed to generate a unique image tag.", e);
    }
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
    if (this.director.getState() == DirectorState.STOPPED) {
      return null;
    }

    this.ensureImageExistsLocally(bot);
    List<PortBinding> portBindings = buildPorts(bot);
    return this.dockerClient
        .createContainerCmd(bot.getTypeConfiguration().getImage())
        .withName(this.buildContainerName(bot.getId()))
        .withEnv(this.buildEnvironmentVariables(bot.getConfiguration()))
        .withExposedPorts(
            portBindings.stream().map(PortBinding::getExposedPort).collect(Collectors.toList()))
        .withHostConfig(
            new HostConfig()
                .withNetworkMode(this.buildNetworkName())
                .withMounts(this.buildMounts(bot.getTypeConfiguration()))
                .withPortBindings(portBindings)
                .withRestartPolicy(RestartPolicy.onFailureRestart(0)))
        .exec()
        .getId();
  }

  private void ensureImageExistsLocally(Bot bot) {
    String imageName = bot.getTypeConfiguration().getImage();
    try {
      this.dockerClient.inspectImageCmd(imageName).exec();
      return;
    } catch (NotFoundException e) {
      log.info("Pulling image {}...", imageName);
    } catch (DockerClientException e) {
      log.warn(
          "Error fetching Docker image '{}' ({}). Attempting to pull anyway.",
          imageName,
          e.getMessage());
    }

    try {
      dockerClient.pullImageCmd(imageName).start().awaitCompletion();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DirectorException(
          String.format("Docker image pull for '%s' was interrupted.", imageName), e);
    } catch (NotFoundException e) {
      throw new DirectorException(
          String.format(
              "Docker image '%s' (used by '%s' bots) was not found.\n\n"
                  + "Please verify that the image name or tag is correct in the bot's configuration.\n"
                  + "Ensure the image has either been built locally (if it's a custom bot) or pushed "
                  + "to a public or accessible private registry (e.g., Docker Hub).",
              imageName, bot.getTypeConfiguration().getId()),
          e);
    } catch (Exception e) {
      throw new DirectorException(
          String.format(
              "An unexpected error occurred while pulling Docker image '%s': %s",
              imageName, e.getMessage()),
          e);
    }
  }

  private List<String> buildEnvironmentVariables(BotInstanceConfiguration botConfiguration) {
    List<String> env = new ArrayList<>();
    env.add(buildEnv(BOT_TYPE_ENV, botConfiguration.getTypeConfiguration().getId()));
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

  private static List<PortBinding> buildPorts(Bot bot) {
    return bot.getConfiguration().getPorts().stream()
        .map(PortBinding::parse)
        .collect(Collectors.toList());
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
        .withSource(this.resolvedConfigurationFile.getAbsolutePath())
        .withTarget(CONFIGURATION_SECRET_PATH);
  }

  @Override
  public void startContainer(String containerId) {
    if (this.director.getState() == DirectorState.STOPPED) {
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
