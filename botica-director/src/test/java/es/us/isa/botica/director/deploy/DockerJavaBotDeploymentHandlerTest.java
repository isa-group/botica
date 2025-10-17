package es.us.isa.botica.director.deploy;

import static es.us.isa.botica.BoticaConstants.BOT_ID_ENV;
import static es.us.isa.botica.BoticaConstants.BOT_TYPE_ENV;
import static es.us.isa.botica.BoticaConstants.BROKER_NETWORK_NAME;
import static es.us.isa.botica.BoticaConstants.CONTAINER_PREFIX;
import static es.us.isa.botica.util.StringUtils.buildEnv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateVolumeCmd;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListVolumesCmd;
import com.github.dockerjava.api.command.ListVolumesResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveVolumeCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.RestartPolicy;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotMountConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.docker.DockerConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.exception.MountNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class DockerJavaBotDeploymentHandlerTest {
  @Mock private Director director;
  @Mock private File mainConfigurationFile;
  @Mock private MainConfiguration mainConfiguration;
  @Mock private DockerClient dockerClient;

  // DockerClient Command Mocks
  @Mock private ListContainersCmd listContainersCmd;
  @Mock private RemoveContainerCmd removeContainerCmd;
  @Mock private ListVolumesCmd listVolumesCmd;
  @Mock private ListVolumesResponse listVolumesResponse;
  @Mock private RemoveVolumeCmd removeVolumeCmd;
  @Mock private CreateVolumeCmd createVolumeCmd;
  @Mock private CreateContainerCmd createContainerCmd;
  @Mock private CreateContainerResponse createContainerResponse;
  @Mock private StartContainerCmd startContainerCmd;
  @Mock private StopContainerCmd Cmd;

  private DockerJavaBotDeploymentHandler deploymentHandler;

  @TempDir Path tempDir; // For temporary files/directories for mount tests

  private String sharedVolumeName;
  private String brokerNetworkName;
  private String containerPrefix;

  @Captor private ArgumentCaptor<List<String>> nameFilterCaptor;
  @Captor private ArgumentCaptor<HostConfig> hostConfigCaptor;
  @Captor private ArgumentCaptor<List<ExposedPort>> portBindingsCaptor;

  @BeforeEach
  void setUp() {
    when(mainConfigurationFile.getAbsolutePath()).thenReturn("/path/to/config.yml");

    when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
    when(listContainersCmd.withNameFilter(anyList())).thenReturn(listContainersCmd);
    when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
    when(listContainersCmd.exec()).thenReturn(Collections.emptyList());

    when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
    when(removeContainerCmd.withForce(any(Boolean.class))).thenReturn(removeContainerCmd);

    when(dockerClient.listVolumesCmd()).thenReturn(listVolumesCmd);
    when(listVolumesCmd.withFilter(anyString(), anyList())).thenReturn(listVolumesCmd);
    when(listVolumesCmd.exec()).thenReturn(listVolumesResponse);
    when(listVolumesResponse.getVolumes()).thenReturn(Collections.emptyList());

    when(dockerClient.removeVolumeCmd(anyString())).thenReturn(removeVolumeCmd);

    when(dockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
    when(createVolumeCmd.withName(anyString())).thenReturn(createVolumeCmd);
    when(createVolumeCmd.exec()).thenReturn(mock(CreateVolumeResponse.class));

    when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
    when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
    when(createContainerCmd.withEnv(anyList())).thenReturn(createContainerCmd);
    when(createContainerCmd.withExposedPorts(anyList())).thenReturn(createContainerCmd);
    when(createContainerCmd.withHostConfig(any(HostConfig.class))).thenReturn(createContainerCmd);
    when(createContainerCmd.exec()).thenReturn(createContainerResponse);
    when(createContainerResponse.getId()).thenReturn("new-container-id");

    when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);

    when(dockerClient.stopContainerCmd(anyString())).thenReturn(Cmd);

    when(mainConfiguration.getBotTypes()).thenReturn(Collections.emptyMap());
    when(mainConfiguration.getDockerConfiguration()).thenReturn(mock(DockerConfiguration.class));

    deploymentHandler =
        new DockerJavaBotDeploymentHandler(
            director, mainConfigurationFile, mainConfiguration, dockerClient);

    sharedVolumeName = CONTAINER_PREFIX + "shared";
    brokerNetworkName = CONTAINER_PREFIX + BROKER_NETWORK_NAME;
    containerPrefix = CONTAINER_PREFIX;
  }

  @Test
  @DisplayName("removePreviousDeployment should remove all bot containers and the shared volume")
  void removePreviousDeployment_removesContainersAndSharedVolume() {
    // Arrange
    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setId("type1");
    typeConfig.setDeclaredInstances(
        Map.of("bot-1", buildBotInstanceConfig("bot-1"), "bot-2", buildBotInstanceConfig("bot-2")));
    when(mainConfiguration.getBotTypes()).thenReturn(Map.of("type1", typeConfig));

    Container mockContainer1 = mock(Container.class);
    when(mockContainer1.getId()).thenReturn("container-id-1");
    Container mockContainer2 = mock(Container.class);
    when(mockContainer2.getId()).thenReturn("container-id-2");
    when(listContainersCmd.exec()).thenReturn(List.of(mockContainer1, mockContainer2));

    InspectVolumeResponse volume = mock(InspectVolumeResponse.class);
    when(volume.getName()).thenReturn(sharedVolumeName);
    when(listVolumesResponse.getVolumes()).thenReturn(List.of(volume));

    // Act
    deploymentHandler.removePreviousDeployment();

    // Assert
    verify(listContainersCmd, times(1)).withNameFilter(nameFilterCaptor.capture());
    assertThat(nameFilterCaptor.getValue())
        .containsExactlyInAnyOrder(containerPrefix + "bot-1", containerPrefix + "bot-2");
    verify(dockerClient, times(1)).removeContainerCmd("container-id-1");
    verify(dockerClient, times(1)).removeContainerCmd("container-id-2");

    verify(listVolumesCmd, times(1)).withFilter(eq("name"), eq(List.of(sharedVolumeName)));
    verify(dockerClient, times(1)).removeVolumeCmd(eq(sharedVolumeName));
  }

  @Test
  @DisplayName("setupInfrastructure should create the shared volume")
  void setupInfrastructure_createsSharedVolume() {
    // Act
    deploymentHandler.setupInfrastructure();

    // Assert
    verify(dockerClient, times(1)).createVolumeCmd();
    verify(createVolumeCmd, times(1)).withName(eq(sharedVolumeName));
    verify(createVolumeCmd, times(1)).exec();
  }

  @Test
  @DisplayName("createContainer should return null if director is not running")
  void createContainer_directorNotRunning_returnsNull() {
    // Arrange
    when(director.isRunning()).thenReturn(false);
    Bot bot = mock(Bot.class);

    // Act
    String containerId = deploymentHandler.createContainer(bot);

    // Assert
    assertThat(containerId).isNull();
    verify(dockerClient, never()).createContainerCmd(anyString());
  }

  @Test
  @DisplayName("createContainer should correctly build and create a container")
  void createContainer_buildsAndCreatesCorrectly() {
    // Arrange
    when(director.isRunning()).thenReturn(true);

    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setId("type-id-1");
    typeConfig.setImage("my-bot-image:latest");
    typeConfig.setMounts(Collections.emptyList());

    BotInstanceConfiguration instanceConfig = new BotInstanceConfiguration();
    instanceConfig.setId("bot-id-1");
    instanceConfig.setTypeConfiguration(typeConfig);
    instanceConfig.setOwnEnvironment(List.of("CUSTOM_VAR=custom_value"));
    instanceConfig.setOwnPorts(List.of("8080:80", "9000:9000/udp"));

    Bot bot = new Bot(typeConfig, instanceConfig);

    // Act
    String containerId = deploymentHandler.createContainer(bot);

    // Assert
    assertThat(containerId).isEqualTo("new-container-id");
    verify(dockerClient, times(1)).createContainerCmd(eq("my-bot-image:latest"));
    verify(createContainerCmd, times(1)).withName(eq(containerPrefix + "bot-id-1"));
    verify(createContainerCmd, times(1))
        .withEnv(
            List.of(
                buildEnv(BOT_TYPE_ENV, "type-id-1"),
                buildEnv(BOT_ID_ENV, "bot-id-1"),
                "CUSTOM_VAR=custom_value"));
    verify(createContainerCmd, times(1)).withExposedPorts(portBindingsCaptor.capture());
    assertThat(portBindingsCaptor.getValue()).hasSize(2).doesNotContainNull();

    verify(createContainerCmd, times(1)).withHostConfig(hostConfigCaptor.capture());

    HostConfig capturedHostConfig = hostConfigCaptor.getValue();
    assertThat(capturedHostConfig.getNetworkMode()).isEqualTo(brokerNetworkName);
    assertThat(capturedHostConfig.getRestartPolicy().getName())
        .isEqualTo(RestartPolicy.onFailureRestart(0).getName());

    List<Mount> mounts = capturedHostConfig.getMounts();
    assertThat(mounts).hasSize(2); // Shared volume + config file
    assertThat(mounts)
        .anyMatch(
            m ->
                m.getType() == MountType.VOLUME
                    && Objects.equals(m.getSource(), sharedVolumeName)
                    && Objects.equals(m.getTarget(), "/shared"));
    assertThat(mounts)
        .anyMatch(
            m ->
                m.getType() == MountType.BIND
                    && Objects.equals(m.getSource(), "/path/to/config.yml")
                    && Objects.equals(m.getTarget(), "/run/secrets/botica-config")
                    && Boolean.TRUE.equals(m.getReadOnly()));
  }

  @Test
  @DisplayName("createContainer should handle bot specific mounts")
  void createContainer_withBotSpecificMounts() throws IOException {
    // Arrange
    when(director.isRunning()).thenReturn(true);

    Path hostMountPath = tempDir.resolve("host_data");
    Files.createDirectory(hostMountPath);

    BotMountConfiguration mountConfig = new BotMountConfiguration();
    mountConfig.setSource(hostMountPath.toString());
    mountConfig.setTarget("/container/data");
    mountConfig.setCreateHostPath(false);

    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setId("type-id-1");
    typeConfig.setImage("my-bot-image:latest");
    typeConfig.setMounts(List.of(mountConfig));

    BotInstanceConfiguration instanceConfig = new BotInstanceConfiguration();
    instanceConfig.setId("bot-id-1");
    instanceConfig.setTypeConfiguration(typeConfig);
    instanceConfig.setOwnEnvironment(Collections.emptyList());
    instanceConfig.setOwnPorts(Collections.emptyList());

    Bot bot = new Bot(typeConfig, instanceConfig);

    // Act
    deploymentHandler.createContainer(bot);

    // Assert
    verify(createContainerCmd, times(1)).withHostConfig(hostConfigCaptor.capture());
    HostConfig capturedHostConfig = hostConfigCaptor.getValue();
    List<Mount> mounts = capturedHostConfig.getMounts();
    assertThat(mounts).hasSize(3); // Shared volume + config file + bot mount

    assertThat(mounts)
        .anyMatch(
            m ->
                m.getType() == MountType.BIND
                    && Objects.equals(m.getSource(), hostMountPath.toAbsolutePath().toString())
                    && Objects.equals(m.getTarget(), "/container/data"));
  }

  @Test
  @DisplayName("createContainer should create host path for mount if configured")
  void createContainer_createsHostPathForMount() {
    // Arrange
    when(director.isRunning()).thenReturn(true);

    Path nonExistentHostMountPath = tempDir.resolve("non_existent_host_data");
    assertThat(Files.exists(nonExistentHostMountPath)).isFalse();

    BotMountConfiguration mountConfig = new BotMountConfiguration();
    mountConfig.setSource(nonExistentHostMountPath.toString());
    mountConfig.setTarget("/container/data");
    mountConfig.setCreateHostPath(true);

    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setId("type-id-1");
    typeConfig.setImage("my-bot-image:latest");
    typeConfig.setMounts(List.of(mountConfig));

    BotInstanceConfiguration instanceConfig = new BotInstanceConfiguration();
    instanceConfig.setId("bot-id-1");
    instanceConfig.setTypeConfiguration(typeConfig);
    instanceConfig.setOwnEnvironment(Collections.emptyList());
    instanceConfig.setOwnPorts(Collections.emptyList());

    Bot bot = new Bot(typeConfig, instanceConfig);

    // Act
    assertDoesNotThrow(() -> deploymentHandler.createContainer(bot));

    // Assert
    assertThat(Files.exists(nonExistentHostMountPath)).isTrue();
  }

  @Test
  @DisplayName(
      "createContainer should throw MountNotFoundException if mount source does not exist and createHostPath is false")
  void createContainer_mountSourceNotFoundAndNoCreateHostPath_throwsException() throws IOException {
    // Arrange
    when(director.isRunning()).thenReturn(true);

    Path nonExistentHostMountPath = tempDir.resolve("definitely_not_there");
    assertThat(Files.exists(nonExistentHostMountPath)).isFalse();

    BotMountConfiguration mountConfig = new BotMountConfiguration();
    mountConfig.setSource(nonExistentHostMountPath.toString());
    mountConfig.setCreateHostPath(false);

    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setId("type-id-1");
    typeConfig.setImage("my-bot-image:latest");
    typeConfig.setMounts(List.of(mountConfig));

    BotInstanceConfiguration instanceConfig = new BotInstanceConfiguration();
    instanceConfig.setId("bot-id-1");
    instanceConfig.setTypeConfiguration(typeConfig);
    instanceConfig.setOwnEnvironment(Collections.emptyList());
    instanceConfig.setOwnPorts(Collections.emptyList());

    Bot bot = new Bot(typeConfig, instanceConfig);

    // Act & Assert
    assertThatThrownBy(() -> deploymentHandler.createContainer(bot))
        .isInstanceOf(MountNotFoundException.class)
        .hasMessageContaining(
            String.format(
                "The file or directory at %s does not exist and is required by 'type-id-1' bots",
                nonExistentHostMountPath.toFile().getCanonicalFile().getAbsolutePath()));
  }

  @Test
  @DisplayName("startContainer should not start container if director is not running")
  void startContainer_directorNotRunning_doesNotStart() {
    // Arrange
    when(director.isRunning()).thenReturn(false);

    // Act
    deploymentHandler.startContainer("container-id-1");

    // Assert
    verify(dockerClient, never()).startContainerCmd(anyString());
  }

  @Test
  @DisplayName("startContainer should start the specified container")
  void startContainer_startsContainer() {
    // Arrange
    when(director.isRunning()).thenReturn(true);

    // Act
    deploymentHandler.startContainer("container-id-1");

    // Assert
    verify(dockerClient, times(1)).startContainerCmd(eq("container-id-1"));
    verify(startContainerCmd, times(1)).exec();
  }

  @Test
  @DisplayName("stopContainer should stop the specified container")
  void stopContainer_stopsContainer() {
    // Act
    deploymentHandler.stopContainer("container-id-1");

    // Assert
    verify(dockerClient, times(1)).stopContainerCmd(eq("container-id-1"));
    verify(Cmd, times(1)).exec();
  }

  @Test
  @DisplayName("stopContainer should handle RuntimeException gracefully")
  void stopContainer_handlesRuntimeException() {
    // Arrange
    when(Cmd.exec()).thenThrow(new RuntimeException("Docker error"));
    when(dockerClient.stopContainerCmd(anyString())).thenReturn(Cmd);

    // Act & Assert
    assertThatCode(() -> deploymentHandler.stopContainer("container-id-1"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("shutdown should call removePreviousDeployment")
  void shutdown_callsRemovePreviousDeployment() {
    // Arrange
    BotTypeConfiguration typeConfig = new BotTypeConfiguration();
    typeConfig.setDeclaredInstances(Collections.emptyMap());
    when(mainConfiguration.getBotTypes()).thenReturn(Map.of("type1", typeConfig));

    // Act
    deploymentHandler.shutdown();

    // Assert
    verify(listContainersCmd, times(1)).exec();
    verify(listVolumesCmd, times(1)).exec();
  }

  private BotInstanceConfiguration buildBotInstanceConfig(String id) {
    BotInstanceConfiguration config = new BotInstanceConfiguration();
    config.setId(id);
    return config;
  }
}
