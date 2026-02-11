package es.us.isa.botica.director;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.director.DirectorBootstrap.ConfigurationResolutionException;
import es.us.isa.botica.director.cli.DirectorCli;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.initialize.ProjectInitializationException;
import es.us.isa.botica.director.initialize.ProjectInitializer;
import es.us.isa.botica.director.util.DotenvLoader;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class DirectorBootstrapTest {
  @TempDir Path tempDir;

  @Mock private ConfigurationFileLoader configurationFileLoader;
  @Mock private Validator validator;
  @Mock private UpdateManager updateManager;
  @Mock private DotenvLoader dotenvLoader;
  @Mock private ProjectInitializer projectInitializer;
  @Mock private Runtime runtime;
  @Mock private ValidationReport validationReport;

  @Captor private ArgumentCaptor<Thread> shutdownHookCaptor;

  private DirectorBootstrap bootstrap;

  @BeforeEach
  void setUp() {
    bootstrap =
        new DirectorBootstrap(
            configurationFileLoader,
            validator,
            updateManager,
            dotenvLoader,
            projectInitializer,
            runtime);
  }

  @Test
  @DisplayName("resolveConfigurationFile should use file from arguments if it exists")
  void resolveConfigurationFile_fileFromArgumentExists_usesFile() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("my-config.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    // Act
    File resolvedFile = bootstrap.resolveConfigurationFile(args, tempDir);

    // Assert
    assertThat(resolvedFile).isEqualTo(configFile);
  }

  @Test
  @DisplayName("resolveConfigurationFile should throw exception if argument file does not exist")
  void resolveConfigurationFile_fileFromArgumentDoesNotExist_throwsException() {
    // Arrange
    String nonExistentPath = tempDir.resolve("non-existent.yml").toString();
    String[] args = {nonExistentPath};

    // Act & Assert
    assertThatThrownBy(() -> bootstrap.resolveConfigurationFile(args, tempDir))
        .isInstanceOf(ConfigurationResolutionException.class);
  }

  @Test
  @DisplayName("resolveConfigurationFile should find default file if no arguments provided")
  void resolveConfigurationFile_noArguments_usesDefaultFile() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {};

    // Act
    File resolvedFile = bootstrap.resolveConfigurationFile(args, tempDir);

    // Assert
    assertThat(resolvedFile).isEqualTo(configFile);
  }

  @Test
  @DisplayName("resolveConfigurationFile should copy fallback resource if no other file is found")
  void resolveConfigurationFile_noDefaultFile_copiesFallbackResource() throws Exception {
    // Arrange
    String[] args = {};
    Path fallbackFilePath = tempDir.resolve("environment.yml");
    assertThat(fallbackFilePath).doesNotExist();

    // Act
    File resolvedFile = bootstrap.resolveConfigurationFile(args, tempDir);

    // Assert
    assertThat(resolvedFile).exists().hasName("environment.yml");
    assertThat(resolvedFile.getParentFile()).isEqualTo(tempDir.toFile());
    verify(runtime, times(1)).exit(0);
  }

  @Test
  @DisplayName("startDirectorInstance should start Director and add shutdown hook")
  void startDirectorInstance_success_registersShutdownHook() throws IOException {
    // Arrange
    try (MockedConstruction<Director> mockedDirector =
        mockConstruction(
            Director.class,
            (director, context) -> {
              when(director.isRunning()).thenReturn(true);
            })) {
      // Act
      Director directorInstance =
          bootstrap.startDirectorInstance(new EnvironmentConfiguration(), tempDir);

      // Assert
      assertThat(mockedDirector.constructed()).hasSize(1);
      Director constructedDirector = mockedDirector.constructed().getFirst();
      assertThat(directorInstance).isEqualTo(constructedDirector);

      verify(constructedDirector, times(1)).start();
      verify(runtime, times(1)).addShutdownHook(shutdownHookCaptor.capture());

      // Verify shutdown hook's behavior
      Thread shutdownHook = shutdownHookCaptor.getValue();
      //noinspection CallToThreadRun
      shutdownHook.run();
      verify(constructedDirector, times(1)).shutdownInfrastructure();
    }
  }

  @Test
  @DisplayName("startDirectorInstance should throw exception if Director fails to start")
  void startDirectorInstance_startupFailure_throwsException() {
    // Arrange
    try (MockedConstruction<Director> ignored =
        mockConstruction(
            Director.class,
            (director, context) -> {
              doThrow(new DirectorException("Startup failed")).when(director).start();
              when(director.isRunning()).thenReturn(false);
            })) {
      // Act & Assert
      assertThatThrownBy(
              () -> bootstrap.startDirectorInstance(new EnvironmentConfiguration(), tempDir))
          .isInstanceOf(DirectorException.class)
          .hasMessage("Startup failed");

      // Verify shutdown hook was still registered (cleanup happens via hook)
      verify(runtime, times(1)).addShutdownHook(any(Thread.class));
    }
  }

  @Test
  @DisplayName("run should exit with error when configuration loading fails")
  void run_configLoadFails_exitsWithError() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    ConfigurationLoadingException loadException =
        new ConfigurationLoadingException("Failed to load");
    when(configurationFileLoader.load(eq(configFile), eq(EnvironmentConfiguration.class)))
        .thenThrow(loadException);

    // Act
    bootstrap.run(args);

    // Assert
    verify(dotenvLoader, times(1)).loadIntoSystemProperties();
    verify(updateManager, times(1)).checkForUpdates();
    verify(runtime, times(1)).exit(1);
    verify(validator, never()).validate(any());
  }

  @Test
  @DisplayName("run should exit with error when validation has errors")
  void run_validationHasErrors_exitsWithError() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    EnvironmentConfiguration config = new EnvironmentConfiguration();
    when(configurationFileLoader.load(eq(configFile), eq(EnvironmentConfiguration.class)))
        .thenReturn(config);

    when(validationReport.hasErrors()).thenReturn(true);
    when(validationReport.countErrors()).thenReturn(2L);
    when(validationReport.countWarnings()).thenReturn(1L);
    when(validationReport.render()).thenReturn("Validation error details");
    when(validator.validate(config)).thenReturn(validationReport);

    try (MockedConstruction<Director> mockedDirector = mockConstruction(Director.class)) {
      // Act
      bootstrap.run(args);

      // Assert
      verify(dotenvLoader, times(1)).loadIntoSystemProperties();
      verify(updateManager, times(1)).checkForUpdates();
      verify(validator, times(1)).validate(config);
      verify(runtime, times(1)).exit(1);

      // Director should NOT be created
      assertThat(mockedDirector.constructed()).isEmpty();
    }
  }

  @Test
  @DisplayName("run should continue startup when validation has warnings only")
  void run_validationHasWarnings_continuesStartup() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    EnvironmentConfiguration config = new EnvironmentConfiguration();
    when(configurationFileLoader.load(eq(configFile), eq(EnvironmentConfiguration.class)))
        .thenReturn(config);

    when(validationReport.hasErrors()).thenReturn(false);
    when(validationReport.hasWarnings()).thenReturn(true);
    when(validationReport.countWarnings()).thenReturn(1L);
    when(validationReport.render()).thenReturn("Validation warning details");
    when(validator.validate(config)).thenReturn(validationReport);

    try (MockedConstruction<Director> mockedDirector =
            mockConstruction(
                Director.class,
                (director, context) -> when(director.isRunning()).thenReturn(true));
        MockedConstruction<DirectorCli> mockedCli = mockConstruction(DirectorCli.class);
        MockedConstruction<Thread> ignored =
            mockConstruction(
                Thread.class,
                (thread, context) -> {
                  Runnable runnable = (Runnable) context.arguments().getFirst();
                  doAnswer(
                          invocation -> {
                            runnable.run();
                            return null;
                          })
                      .when(thread)
                      .start();
                })) {
      // Act
      bootstrap.run(args);

      // Assert
      verify(dotenvLoader, times(1)).loadIntoSystemProperties();
      verify(updateManager, times(1)).checkForUpdates();
      verify(validator, times(1)).validate(config);
      verify(runtime, never()).exit(1);

      // Director and CLI should be created
      assertThat(mockedDirector.constructed()).hasSize(1);
      assertThat(mockedCli.constructed()).hasSize(1);

      Director director = mockedDirector.constructed().getFirst();
      verify(director, times(1)).start();
    }
  }

  @Test
  @DisplayName("run should complete full startup sequence when configuration is valid")
  void run_validConfiguration_completesStartup() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    EnvironmentConfiguration config = new EnvironmentConfiguration();
    when(configurationFileLoader.load(eq(configFile), eq(EnvironmentConfiguration.class)))
        .thenReturn(config);

    when(validationReport.hasErrors()).thenReturn(false);
    when(validationReport.hasWarnings()).thenReturn(false);
    when(validator.validate(config)).thenReturn(validationReport);

    try (MockedConstruction<Director> mockedDirector =
            mockConstruction(
                Director.class,
                (director, context) -> when(director.isRunning()).thenReturn(true));
        MockedConstruction<DirectorCli> mockedCli = mockConstruction(DirectorCli.class);
        MockedConstruction<Thread> mockedThread =
            mockConstruction(
                Thread.class,
                (thread, context) -> {
                  Runnable runnable = (Runnable) context.arguments().getFirst();
                  doAnswer(
                          invocation -> {
                            runnable.run();
                            return null;
                          })
                      .when(thread)
                      .start();
                })) {
      // Act
      bootstrap.run(args);

      // Assert
      verify(dotenvLoader, times(1)).loadIntoSystemProperties();
      verify(updateManager, times(1)).checkForUpdates();
      verify(configurationFileLoader, times(1))
          .load(eq(configFile), eq(EnvironmentConfiguration.class));
      verify(validator, times(1)).validate(config);
      verify(runtime, never()).exit(1);

      assertThat(mockedDirector.constructed()).hasSize(1);
      assertThat(mockedCli.constructed()).hasSize(1);

      Director director = mockedDirector.constructed().getFirst();
      DirectorCli cli = mockedCli.constructed().getFirst();

      verify(director, times(1)).start();
      verify(runtime, times(1)).addShutdownHook(any(Thread.class));
      verify(cli, times(1)).start();

      // 2 threads: shutdown hook and CLI thread
      assertThat(mockedThread.constructed()).hasSize(2);
    }
  }

  @Test
  @DisplayName("run should handle init command with valid arguments")
  void run_initCommandValid_delegatesToInitializer() throws Exception {
    // Arrange
    String[] args = {"init", "java", "my-project"};

    // Act
    bootstrap.run(args);

    // Assert
    verify(projectInitializer, times(1)).initialize("java", "my-project");
    verify(runtime, times(1)).exit(0);
    verify(dotenvLoader, never()).loadIntoSystemProperties();
    verify(updateManager, never()).checkForUpdates();
  }

  @Test
  @DisplayName("run should exit with error when init command has invalid arguments")
  void run_initCommandInvalid_exitsWithError() throws ProjectInitializationException {
    // Arrange
    String[] args = {"init", "java"}; // Missing directory name

    // Act
    bootstrap.run(args);

    // Assert
    verify(runtime, times(1)).exit(1);
    verify(projectInitializer, never()).initialize(any(), any());
  }

  @Test
  @DisplayName("run should exit with error when init command fails")
  void run_initCommandFails_exitsWithError() throws ProjectInitializationException {
    // Arrange
    String[] args = {"init", "java", "my-project"};

    ProjectInitializationException initException =
        new ProjectInitializationException("Template not found");
    doThrow(initException).when(projectInitializer).initialize("java", "my-project");

    // Act
    bootstrap.run(args);

    // Assert
    verify(projectInitializer, times(1)).initialize("java", "my-project");
    verify(runtime, times(1)).exit(1);
  }

  @Test
  @DisplayName("run should exit with error when unexpected exception occurs")
  void run_unexpectedException_exitsWithError() throws Exception {
    // Arrange
    File configFile = Files.createFile(tempDir.resolve("botica.yml")).toFile();
    String[] args = {configFile.getAbsolutePath()};

    when(configurationFileLoader.load(any(), any()))
        .thenThrow(new RuntimeException("Unexpected error"));

    // Act
    bootstrap.run(args);

    // Assert
    verify(runtime, times(1)).exit(1);
  }
}
