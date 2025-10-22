package es.us.isa.botica.director;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.director.DirectorBootstrap.ConfigurationResolutionException;
import es.us.isa.botica.director.cli.DirectorCli;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

@SuppressWarnings("ResultOfMethodCallIgnored")
@MockitoSettings
class DirectorBootstrapTest {
  @TempDir Path tempDir;

  @Mock private Runtime mockRuntime;
  @Mock private DotenvBuilder mockDotenvBuilder;
  @Mock private Dotenv mockDotenv;

  @Captor private ArgumentCaptor<Thread> shutdownHookCaptor;

  @Test
  @DisplayName("resolveConfigurationFile should use file from arguments if it exists")
  void resolveConfigurationFile_fromArgument_whenFileExists() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("my-config.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    // Act
    File resolvedFile = DirectorBootstrap.resolveConfigurationFile(args, tempDir);

    // Assert
    assertThat(resolvedFile).isEqualTo(configFile);
  }

  @Test
  @DisplayName("resolveConfigurationFile should throw exception if argument file does not exist")
  void resolveConfigurationFile_fromArgument_whenFileDoesNotExist() {
    // Arrange
    String nonExistentPath = tempDir.resolve("non-existent.yml").toString();
    String[] args = {nonExistentPath};

    // Act & Assert
    assertThatThrownBy(() -> DirectorBootstrap.resolveConfigurationFile(args, tempDir))
        .isInstanceOf(ConfigurationResolutionException.class);
  }

  @Test
  @DisplayName("resolveConfigurationFile should find default file if no arguments provided")
  void resolveConfigurationFile_fromDefaultFile_whenFileExists() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {};

    // Act
    File resolvedFile = DirectorBootstrap.resolveConfigurationFile(args, tempDir);

    // Assert
    assertThat(resolvedFile).isEqualTo(configFile);
  }

  @Test
  @DisplayName("resolveConfigurationFile should copy fallback resource if no other file is found")
  void resolveConfigurationFile_fromFallbackResource_whenNoOtherFileFound() throws Exception {
    // Arrange
    String[] args = {};
    Path fallbackFilePath = tempDir.resolve("environment.yml");
    assertThat(fallbackFilePath).doesNotExist();

    try (MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
      mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);

      // Act
      File resolvedFile = DirectorBootstrap.resolveConfigurationFile(args, tempDir);

      // Assert
      assertThat(resolvedFile).exists().hasName("environment.yml");
      assertThat(resolvedFile.getParentFile()).isEqualTo(tempDir.toFile());
      verify(mockRuntime, times(1)).exit(0);
    }
  }

  @Test
  @DisplayName(
      "startDirectorInstance should start Director, add and then remove shutdown hook on success")
  void startDirectorInstance_startsDirectorAndManagesShutdownHookOnSuccess() throws IOException {
    // Arrange
    File configFile = tempDir.resolve("config.yml").toFile();

    try (MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class)) {
      mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);

      try (MockedConstruction<Director> mockedDirector =
          mockConstruction(
              Director.class,
              (director, context) -> {
                when(director.isRunning()).thenReturn(true);
              })) {
        // Act
        Director directorInstance = DirectorBootstrap.startDirectorInstance(configFile);

        // Assert
        assertThat(mockedDirector.constructed()).hasSize(1);
        Director constructedDirector = mockedDirector.constructed().get(0);
        assertThat(directorInstance).isEqualTo(constructedDirector);

        verify(constructedDirector, times(1)).start();
        verify(mockRuntime, times(1)).addShutdownHook(shutdownHookCaptor.capture());
        verify(mockRuntime, times(1)).removeShutdownHook(shutdownHookCaptor.getValue());

        // Verify shutdown hook's behavior
        Thread shutdownHook = shutdownHookCaptor.getValue();
        //noinspection CallToThreadRun
        shutdownHook.run();
        verify(constructedDirector, times(1)).shutdownInfrastructure();
      }
    }
  }

  @Test
  @DisplayName("main method should initialize dependencies and start Director and CLI")
  void main_initializesAndStartsServices() {
    // Arrange
    String[] args = {};

    // Mock static dependencies and constructors for a full flow test
    try (MockedStatic<DirectorBootstrap> mockedBootstrap =
            mockStatic(DirectorBootstrap.class, Mockito.CALLS_REAL_METHODS);
        MockedStatic<Dotenv> mockedDotenv = mockStatic(Dotenv.class);
        MockedConstruction<Director> mockedDirector =
            mockConstruction(
                Director.class, (mock, context) -> when(mock.isRunning()).thenReturn(true));
        MockedConstruction<DirectorCli> mockedCli = mockConstruction(DirectorCli.class);
        // For every Thread that gets created, we stub its start() method to synchronously run
        // the Runnable it was constructed with
        MockedConstruction<Thread> mockedThread =
            mockConstruction(
                Thread.class,
                (thread, context) -> {
                  // Get the Runnable passed to the Thread's constructor
                  Runnable runnable = (Runnable) context.arguments().get(0);
                  // When #start() is called on this mock Thread, run the Runnable immediately
                  // in the current thread instead of starting a new one
                  doAnswer(
                          invocation -> {
                            runnable.run();
                            return null;
                          })
                      .when(thread)
                      .start();
                })) {
      mockedDotenv.when(Dotenv::configure).thenReturn(mockDotenvBuilder);
      when(mockDotenvBuilder.ignoreIfMissing()).thenReturn(mockDotenvBuilder);
      when(mockDotenvBuilder.systemProperties()).thenReturn(mockDotenvBuilder);
      when(mockDotenvBuilder.load()).thenReturn(mockDotenv);

      File mockConfigFile = mock(File.class);
      mockedBootstrap
          .when(() -> DirectorBootstrap.resolveConfigurationFile(any(String[].class)))
          .thenReturn(mockConfigFile);
      mockedBootstrap
          .when(() -> DirectorBootstrap.startDirectorInstance(any(File.class)))
          .thenReturn(mockedDirector.constructed().get(0));

      // Act
      DirectorBootstrap.main(args);

      // Assert
      verify(mockDotenvBuilder, times(1)).load();
      mockedBootstrap.verify(() -> DirectorBootstrap.startDirectorInstance(mockConfigFile));
      assertThat(mockedDirector.constructed()).hasSize(1);

      assertThat(mockedCli.constructed()).hasSize(1);
      DirectorCli cliInstance = mockedCli.constructed().get(0);

      verify(cliInstance, times(1)).start();
      assertThat(mockedThread.constructed()).hasSize(2); // 2 threads: Shutdown hook and CLI
    }
  }
}
