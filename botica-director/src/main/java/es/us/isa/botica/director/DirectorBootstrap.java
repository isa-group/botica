package es.us.isa.botica.director;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.director.cli.DirectorCli;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.initialize.ProjectInitializationException;
import es.us.isa.botica.director.initialize.ProjectInitializer;
import es.us.isa.botica.director.util.DotenvLoader;
import es.us.isa.botica.director.util.SystemDotenvLoader;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.jackson.JacksonConfigurationFileLoader;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectorBootstrap {
  private static final Logger log = LoggerFactory.getLogger(DirectorBootstrap.class);

  private static final String[] DEFAULT_CONFIG_BASE_NAMES = {
    "botica-environment", "botica-config", "botica", "environment", "config"
  };
  private static final String[] PREFERRED_EXTENSIONS = {".yml", ".yaml", ".json"};
  private static final String FALLBACK_RESOURCE_CONFIG_NAME = "environment.yml";

  private final ConfigurationFileLoader configurationFileLoader;
  private final Validator validator;
  private final UpdateManager updateManager;
  private final DotenvLoader dotenvLoader;
  private final ProjectInitializer projectInitializer;
  private final Runtime runtime;

  public static void main(String[] args) {
    new DirectorBootstrap().run(args);
  }

  private DirectorBootstrap() {
    this(
        new JacksonConfigurationFileLoader(),
        new Validator(),
        new UpdateManager(),
        new SystemDotenvLoader(),
        new ProjectInitializer(),
        Runtime.getRuntime());
  }

  @VisibleForTesting
  DirectorBootstrap(
      ConfigurationFileLoader configurationFileLoader,
      Validator validator,
      UpdateManager updateManager,
      DotenvLoader dotenvLoader,
      ProjectInitializer projectInitializer,
      Runtime runtime) {
    this.configurationFileLoader = configurationFileLoader;
    this.validator = validator;
    this.updateManager = updateManager;
    this.dotenvLoader = dotenvLoader;
    this.projectInitializer = projectInitializer;
    this.runtime = runtime;
  }

  @VisibleForTesting
  void run(String[] args) {
    if (args.length > 0 && args[0].equalsIgnoreCase("init")) {
      handleInitCommand(args);
      return;
    }

    dotenvLoader.loadIntoSystemProperties();
    updateManager.checkForUpdates();

    try {
      File configurationFile = this.resolveConfigurationFile(args).getAbsoluteFile();
      EnvironmentConfiguration configuration = this.loadConfiguration(configurationFile);
      this.validateConfiguration(configuration, configurationFile);

      Path workingPath = configurationFile.getParentFile().toPath();
      Director director = this.startDirectorInstance(configuration, workingPath);

      DirectorCli cli = new DirectorCli(director);
      new Thread(cli::start).start();

      if (!director.isRunning()) {
        director.shutdownInfrastructure();
        runtime.exit(0);
      }
    } catch (ConfigurationResolutionException | DirectorException e) {
      log.error(e.getMessage());
      if (e.getCause() != null) {
        log.debug(e.getMessage(), e.getCause());
      }
      runtime.exit(1);
    } catch (Exception e) {
      log.error("An unexpected error occurred during application startup: {}", e.getMessage(), e);
      runtime.exit(1);
    }
  }

  private void handleInitCommand(String[] args) {
    if (args.length != 3) {
      log.error("Invalid usage. Syntax: init <template-name> <directory-name>");
      log.info("Available templates: java, js, javascript, ts, typescript");
      runtime.exit(1);
      return;
    }
    String templateAlias = args[1];
    String directoryName = args[2];

    try {
      projectInitializer.initialize(templateAlias, directoryName);
      runtime.exit(0);
    } catch (ProjectInitializationException e) {
      log.error("Project initialization failed: {}", e.getMessage());
      runtime.exit(1);
    }
  }

  @VisibleForTesting
  File resolveConfigurationFile(String[] args) throws ConfigurationResolutionException {
    return this.resolveConfigurationFile(args, Paths.get("")); // Current directory
  }

  @VisibleForTesting
  File resolveConfigurationFile(String[] args, Path searchPath)
      throws ConfigurationResolutionException {
    if (args.length > 0) {
      File file = new File(args[0]);
      if (!file.exists()) {
        throw new ConfigurationResolutionException(
            String.format(
                "Environment file not found at specified path: %s", file.getAbsolutePath()));
      }
      log.info("Using environment file from arguments: {}", file.getAbsolutePath());
      return file;
    }

    for (String baseName : DEFAULT_CONFIG_BASE_NAMES) {
      for (String ext : PREFERRED_EXTENSIONS) {
        File file = searchPath.resolve(baseName + ext).toFile();
        if (file.exists()) {
          log.info("Using environment file: {}", file.getAbsolutePath());
          return file;
        }
      }
    }

    try {
      File file = this.copyFallbackConfig(searchPath);
      log.info(
          "No environment file found. A default environment file has been created at {}",
          file.getAbsolutePath());
      runtime.exit(0);
      return file;
    } catch (IOException e) {
      throw new ConfigurationResolutionException(
          String.format(
              "Failed to copy default configuration file '%s' from resources: %s",
              FALLBACK_RESOURCE_CONFIG_NAME, e.getMessage()),
          e);
    }
  }

  private EnvironmentConfiguration loadConfiguration(File file) {
    try {
      return configurationFileLoader.load(file, EnvironmentConfiguration.class);
    } catch (ConfigurationLoadingException e) {
      throw new DirectorException(e);
    }
  }

  private void validateConfiguration(EnvironmentConfiguration configuration, File file) {
    ValidationReport report = validator.validate(configuration);

    if (report.hasErrors()) {
      throw new DirectorException(
          String.format(
              "There are %d errors and %d warnings in your configuration file at %s:\n%s",
              report.countErrors(),
              report.countWarnings(),
              file.getAbsolutePath(),
              report.render()));
    }

    if (report.hasWarnings()) {
      log.warn(
          "There are {} warnings in your configuration file at {}:\n{}",
          report.countWarnings(),
          file.getAbsolutePath(),
          report.render());
    }
  }

  @VisibleForTesting
  Director startDirectorInstance(EnvironmentConfiguration configuration, Path workingPath)
      throws IOException {
    Director director = new Director(configuration, workingPath);
    Thread shutdownHook = new Thread(director::shutdownInfrastructure);
    runtime.addShutdownHook(shutdownHook);
    director.start();
    return director;
  }

  private File copyFallbackConfig(Path targetDirectory) throws IOException {
    Path targetPath = targetDirectory.resolve(FALLBACK_RESOURCE_CONFIG_NAME);
    try (InputStream in =
        DirectorBootstrap.class
            .getClassLoader()
            .getResourceAsStream(FALLBACK_RESOURCE_CONFIG_NAME)) {
      if (in == null) {
        throw new IOException(
            String.format(
                "Fallback configuration file '%s' not found in resources.",
                FALLBACK_RESOURCE_CONFIG_NAME));
      }
      Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
      return targetPath.toFile();
    }
  }

  static class ConfigurationResolutionException extends Exception {
    public ConfigurationResolutionException(String message) {
      super(message);
    }

    public ConfigurationResolutionException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
