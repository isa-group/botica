package es.us.isa.botica.director;

import es.us.isa.botica.configuration.EnvironmentConfiguration;
import es.us.isa.botica.director.cli.DirectorCli;
import es.us.isa.botica.director.exception.DirectorException;
import es.us.isa.botica.director.initialize.ProjectInitializationException;
import es.us.isa.botica.director.initialize.ProjectInitializer;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.Validator;
import es.us.isa.botica.util.configuration.jackson.JacksonConfigurationFileLoader;

public class DirectorBootstrap {
  private static final Logger log = LoggerFactory.getLogger(DirectorBootstrap.class);

  private static final String[] DEFAULT_CONFIG_BASE_NAMES = {
    "botica-environment", "botica-config", "botica", "environment", "config"
  };
  private static final String[] PREFERRED_EXTENSIONS = {".yml", ".yaml", ".json"};
  private static final String FALLBACK_RESOURCE_CONFIG_NAME = "environment.yml";

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equalsIgnoreCase("init")) {
      handleInitCommand(args);
      return;
    }

    Dotenv.configure().ignoreIfMissing().systemProperties().load();
    new UpdateManager().checkForUpdates();

    try {
      File configurationFile = resolveConfigurationFile(args);
      EnvironmentConfiguration configuration = loadConfiguration(configurationFile);
      validateConfiguration(configuration, configurationFile);

      Path workingPath = configurationFile.getParentFile().toPath();
      Director director = startDirectorInstance(configuration, workingPath);

      DirectorCli cli = new DirectorCli(director);
      new Thread(cli::start).start();
    } catch (ConfigurationResolutionException e) {
      log.error(e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      log.error("An unexpected error occurred during application startup: {}", e.getMessage(), e);
      System.exit(1);
    }
  }

  private static void handleInitCommand(String[] args) {
    if (args.length != 3) {
      log.error("Invalid usage. Syntax: init <template-name> <directory-name>");
      log.info("Available templates: java, js, javascript, ts, typescript");
      System.exit(1);
    }
    String templateAlias = args[1];
    String directoryName = args[2];

    try {
      new ProjectInitializer().initialize(templateAlias, directoryName);
      System.exit(0);
    } catch (ProjectInitializationException e) {
      log.error("Project initialization failed: {}", e.getMessage());
      System.exit(1);
    }
  }

  @VisibleForTesting
  static File resolveConfigurationFile(String[] args) throws ConfigurationResolutionException {
    return resolveConfigurationFile(args, Paths.get("")); // Current directory
  }

  @VisibleForTesting
  static File resolveConfigurationFile(String[] args, Path searchPath)
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
      File file = copyFallbackConfig(searchPath);
      log.info(
          "No environment file found. A default environment file has been created at {}",
          file.getAbsolutePath());
      System.exit(0);
      return file;
    } catch (IOException e) {
      throw new ConfigurationResolutionException(
          String.format(
              "Failed to copy default configuration file '%s' from resources: %s",
              FALLBACK_RESOURCE_CONFIG_NAME, e.getMessage()),
          e);
    }
  }

  private static EnvironmentConfiguration loadConfiguration(File file) {
    ConfigurationFileLoader loader = new JacksonConfigurationFileLoader();
    try {
      return loader.load(file, EnvironmentConfiguration.class);
    } catch (ConfigurationLoadingException e) {
      throw new DirectorException(e);
    }
  }

  private static void validateConfiguration(EnvironmentConfiguration configuration, File file) {
    ValidationReport report = new Validator().validate(configuration);

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
  static Director startDirectorInstance(EnvironmentConfiguration configuration, Path workingPath) {
    Director director = new Director(configuration, workingPath);
    Thread shutdownHook = new Thread(director::shutdownInfrastructure);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    try {
      director.start();
    } catch (DirectorException e) {
      log.error(e.getMessage());
      if (e.getCause() != null) log.debug(e.getMessage(), e.getCause());
    } catch (Exception e) {
      log.error("An unexpected error occurred during startup: {}", e.getMessage(), e);
    }
    if (!director.isRunning()) {
      director.shutdownInfrastructure();
      System.exit(0);
    }
    return director;
  }

  private static File copyFallbackConfig(Path targetDirectory) throws IOException {
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
