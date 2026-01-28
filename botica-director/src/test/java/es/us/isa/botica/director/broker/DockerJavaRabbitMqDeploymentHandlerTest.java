package es.us.isa.botica.director.broker;

import static es.us.isa.botica.BoticaConstants.BROKER_NETWORK_NAME;
import static es.us.isa.botica.BoticaConstants.CONTAINER_PREFIX;
import static es.us.isa.botica.rabbitmq.RabbitMqConstants.CONTAINER_NAME;
import static es.us.isa.botica.util.StringUtils.buildEnv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListNetworksCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveNetworkCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.PullResponseItem;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class DockerJavaRabbitMqDeploymentHandlerTest {
  private final String DEFINITIONS_PATH = "/run/secrets/definitions";
  private final String IMAGE = "rabbitmq:3.13-management";
  private final ExposedPort DEFAULT_PORT = new ExposedPort(5672);

  @Mock private DockerClient dockerClient;
  @Mock private RabbitMqConfigurationGenerator configurationGenerator;
  @Mock private RabbitMqConfiguration rabbitMqConfiguration;

  // DockerClient Command Mocks
  @Mock private ListContainersCmd listContainersCmd;
  @Mock private RemoveContainerCmd removeContainerCmd;
  @Mock private PullImageCmd pullImageCmd;

  @Mock
  private ResultCallback.Adapter<PullResponseItem> pullImageProcess; // To mock awaitCompletion()

  @Mock private CreateNetworkCmd createNetworkCmd;
  @Mock private ListNetworksCmd listNetworksCmd;
  @Mock private RemoveNetworkCmd removeNetworkCmd;
  @Mock private CreateContainerCmd createContainerCmd;
  @Mock private CreateContainerResponse createContainerResponse;
  @Mock private StartContainerCmd startContainerCmd;

  private DockerJavaRabbitMqDeploymentHandler deploymentHandler;

  private String rabbitMqContainerName;
  private String brokerNetworkName;

  @Captor private ArgumentCaptor<List<String>> nameFilterCaptor;
  @Captor private ArgumentCaptor<HostConfig> hostConfigCaptor;
  @Captor private ArgumentCaptor<List<String>> envCaptor;
  @Captor private ArgumentCaptor<String> networkNameCaptor;

  @BeforeEach
  void setUp() throws InterruptedException {
    // Standard names based on BoticaConstants
    rabbitMqContainerName = CONTAINER_PREFIX + CONTAINER_NAME;
    brokerNetworkName = CONTAINER_PREFIX + BROKER_NETWORK_NAME;

    // DockerClient command chaining stubs
    when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
    when(listContainersCmd.withShowAll(anyBoolean())).thenReturn(listContainersCmd);
    when(listContainersCmd.withNameFilter(anyList())).thenReturn(listContainersCmd);
    when(listContainersCmd.exec()).thenReturn(Collections.emptyList());

    when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
    when(removeContainerCmd.withForce(anyBoolean())).thenReturn(removeContainerCmd);

    when(dockerClient.pullImageCmd(anyString())).thenReturn(pullImageCmd);
    when(pullImageCmd.start()).thenReturn(pullImageProcess);
    when(pullImageProcess.awaitCompletion()).thenReturn(mock());

    when(dockerClient.createNetworkCmd()).thenReturn(createNetworkCmd);
    when(createNetworkCmd.withName(anyString())).thenReturn(createNetworkCmd);
    when(createNetworkCmd.withAttachable(anyBoolean())).thenReturn(createNetworkCmd);
    when(createNetworkCmd.exec()).thenReturn(mock());

    when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
    when(listNetworksCmd.withNameFilter(anyString())).thenReturn(listNetworksCmd);
    when(listNetworksCmd.exec()).thenReturn(Collections.emptyList());

    when(dockerClient.removeNetworkCmd(anyString())).thenReturn(removeNetworkCmd);

    when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
    when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
    when(createContainerCmd.withEnv(anyList())).thenReturn(createContainerCmd);
    when(createContainerCmd.withHostConfig(any(HostConfig.class))).thenReturn(createContainerCmd);
    when(createContainerCmd.exec()).thenReturn(createContainerResponse);
    when(createContainerResponse.getId()).thenReturn("rabbitmq-container-id");

    when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);

    // RabbitMQ Configuration mocks
    when(rabbitMqConfiguration.getPort()).thenReturn(5672);

    deploymentHandler =
        new DockerJavaRabbitMqDeploymentHandler(
            dockerClient, configurationGenerator, rabbitMqConfiguration);
  }

  @Test
  @DisplayName("deploy should throw RuntimeException if definitions file generation fails")
  void deploy_throwsRuntimeException_onDefinitionsGenerationFailure() throws IOException {
    // Arrange
    doThrow(IOException.class).when(configurationGenerator).generateDefinitionsFile();

    // Act & Assert
    assertThatThrownBy(() -> deploymentHandler.deploy())
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(IOException.class);

    verify(dockerClient, never()).listContainersCmd(); // Should fail before this
  }

  @Test
  @DisplayName("deploy should throw RuntimeException if image pull is interrupted")
  void deploy_throwsRuntimeException_onImagePullInterruption()
      throws InterruptedException, IOException {
    // Arrange
    doThrow(InterruptedException.class).when(pullImageProcess).awaitCompletion();

    // Act & Assert
    assertThatThrownBy(() -> deploymentHandler.deploy())
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(InterruptedException.class);

    // Verify it reached pullImage, but no further
    verify(configurationGenerator, times(1)).generateDefinitionsFile();
    verify(dockerClient, times(1)).pullImageCmd(anyString());
    verify(dockerClient, never()).createNetworkCmd();
  }

  @Test
  @DisplayName("removePreviousDeployment should remove existing RabbitMQ container and network")
  void removePreviousDeployment_removesExistingContainerAndNetwork() {
    // Arrange
    Container mockContainer = mock(Container.class);
    when(mockContainer.getId()).thenReturn("existing-rabbit-id");
    when(listContainersCmd.exec()).thenReturn(List.of(mockContainer));

    Network mockNetwork = mock(Network.class);
    when(mockNetwork.getName()).thenReturn(brokerNetworkName);
    when(listNetworksCmd.exec()).thenReturn(List.of(mockNetwork));

    // Act
    deploymentHandler.removePreviousDeployment();

    // Assert
    verify(listContainersCmd, times(1)).withNameFilter(nameFilterCaptor.capture());
    assertThat(nameFilterCaptor.getValue()).containsExactly(rabbitMqContainerName);
    verify(dockerClient, times(1)).removeContainerCmd(eq("existing-rabbit-id"));
    verify(removeContainerCmd, times(1)).withForce(true);

    verify(listNetworksCmd, times(1)).withNameFilter(eq(brokerNetworkName));
    verify(dockerClient, times(1)).removeNetworkCmd(eq(brokerNetworkName));
  }

  @Test
  @DisplayName("removePreviousDeployment should do nothing if no previous deployment exists")
  void removePreviousDeployment_doesNothingIfNoPreviousDeployment() {
    // Arrange - default mocks return empty lists

    // Act
    deploymentHandler.removePreviousDeployment();

    // Assert
    verify(listContainersCmd, times(1)).exec();
    verify(dockerClient, never()).removeContainerCmd(anyString());

    verify(listNetworksCmd, times(1)).exec();
    verify(dockerClient, never()).removeNetworkCmd(anyString());
  }

  @Test
  @DisplayName(
      "deploy should generate definitions, remove old, pull image, create network, create and start container")
  void deploy_orchestratesFullDeploymentFlow() throws IOException, InterruptedException {
    // Act
    deploymentHandler.deploy();

    // Assert
    // 1. Definitions file generated
    verify(configurationGenerator, times(1)).generateDefinitionsFile();

    // 2. Previous deployment removed (containers and network)
    verify(listContainersCmd, times(1))
        .withNameFilter(nameFilterCaptor.capture()); // For RabbitMQ container
    assertThat(nameFilterCaptor.getValue()).containsExactly(rabbitMqContainerName);
    verify(dockerClient, never())
        .removeContainerCmd(anyString()); // Actually called if containers found

    verify(listNetworksCmd, times(1)).withNameFilter(networkNameCaptor.capture());
    assertThat(networkNameCaptor.getValue()).isEqualTo(brokerNetworkName);
    verify(dockerClient, never())
        .removeNetworkCmd(anyString()); // Actually called if networks found

    // 3. Image pulled
    verify(dockerClient, times(1)).pullImageCmd(eq(IMAGE));
    verify(pullImageCmd, times(1)).start();
    verify(pullImageProcess, times(1)).awaitCompletion();

    // 4. Network created
    verify(dockerClient, times(1)).createNetworkCmd();
    verify(createNetworkCmd, times(1)).withName(eq(brokerNetworkName));
    verify(createNetworkCmd, times(1)).withAttachable(true);
    verify(createNetworkCmd, times(1)).exec();

    // 5. Container created
    verify(dockerClient, times(1)).createContainerCmd(eq(IMAGE));
    verify(createContainerCmd, times(1)).withName(eq(rabbitMqContainerName));
    verify(createContainerCmd, times(1)).withEnv(envCaptor.capture());
    assertThat(envCaptor.getValue())
        .containsExactly(
            buildEnv(
                "RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS",
                "-rabbitmq_management load_definitions \"" + DEFINITIONS_PATH + "\""));
    verify(createContainerCmd, times(1)).withHostConfig(hostConfigCaptor.capture());
    HostConfig capturedHostConfig = hostConfigCaptor.getValue();
    assertThat(capturedHostConfig.getNetworkMode()).isEqualTo(brokerNetworkName);
    assertThat(capturedHostConfig.getPortBindings().getBindings())
        .hasSize(1)
        .containsOnlyKeys(DEFAULT_PORT)
        .extractingByKey(DEFAULT_PORT)
        .asInstanceOf(InstanceOfAssertFactories.ARRAY)
        .containsExactly(Binding.bindPort(5672));

    List<Mount> mounts = capturedHostConfig.getMounts();
    assertThat(mounts).hasSize(1);
    assertThat(mounts.get(0).getType()).isEqualTo(MountType.BIND);
    assertThat(mounts.get(0).getSource())
        .isEqualTo(
            RabbitMqConfigurationGenerator.DEFINITIONS_TARGET_PATH.toAbsolutePath().toString());
    assertThat(mounts.get(0).getTarget()).isEqualTo(DEFINITIONS_PATH);

    verify(createContainerCmd, times(1)).exec();

    // 6. Container started
    verify(dockerClient, times(1)).startContainerCmd(eq("rabbitmq-container-id"));
    verify(startContainerCmd, times(1)).exec();
  }

  @Test
  @DisplayName("isRunning should return true if RabbitMQ container is running")
  void isRunning_returnsTrue_ifContainerIsRunning() {
    // Arrange
    Container runningContainer = mock(Container.class);
    when(runningContainer.getState()).thenReturn("running");
    when(listContainersCmd.exec()).thenReturn(List.of(runningContainer));

    // Act
    boolean running = deploymentHandler.isRunning();

    // Assert
    assertThat(running).isTrue();
    verify(listContainersCmd, times(1)).withNameFilter(nameFilterCaptor.capture());
    assertThat(nameFilterCaptor.getValue()).containsExactly(rabbitMqContainerName);
  }

  @Test
  @DisplayName("isRunning should return false if RabbitMQ container is not running (e.g., exited)")
  void isRunning_returnsFalse_ifContainerNotRunning() {
    // Arrange
    Container exitedContainer = mock(Container.class);
    when(exitedContainer.getState()).thenReturn("exited");
    when(listContainersCmd.exec()).thenReturn(List.of(exitedContainer));

    // Act
    boolean running = deploymentHandler.isRunning();

    // Assert
    assertThat(running).isFalse();
  }

  @Test
  @DisplayName("isRunning should return false if RabbitMQ container does not exist")
  void isRunning_returnsFalse_ifContainerDoesNotExist() {
    // Arrange - default listContainersCmd.exec() returns empty list

    // Act
    boolean running = deploymentHandler.isRunning();

    // Assert
    assertThat(running).isFalse();
  }

  @Test
  @DisplayName("shutdown should call removePreviousDeployment")
  void shutdown_callsRemovePreviousDeployment() {
    // Act
    deploymentHandler.shutdown();

    // Assert
    verify(listContainersCmd, times(1)).exec();
    verify(listNetworksCmd, times(1)).exec();
  }
}
