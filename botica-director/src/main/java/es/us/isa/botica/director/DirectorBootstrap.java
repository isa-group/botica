package es.us.isa.botica.director;

import es.us.isa.botica.director.cli.DirectorCli;
import es.us.isa.botica.director.exception.DirectorException;
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

public class DirectorBootstrap {
  private static final Logger log = LoggerFactory.getLogger(DirectorBootstrap.class);

  private static final String[] DEFAULT_CONFIG_BASE_NAMES = {
    "botica-environment", "botica-config", "botica", "environment", "config"
  };
  private static final String[] PREFERRED_EXTENSIONS = {".yml", ".yaml", ".json"};
  private static final String FALLBACK_RESOURCE_CONFIG_NAME = "environment.yml";

  public static void main(String[] args) {
    try {
      File mainConfigurationFile = resolveConfigurationFile(args);

      Dotenv.configure().ignoreIfMissing().systemProperties().load();

      Director director = startDirectorInstance(mainConfigurationFile);
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

  @VisibleForTesting
  static Director startDirectorInstance(File mainConfigurationFile) {
    Director director = new Director(mainConfigurationFile);
    Thread shutdownHook = new Thread(director::shutdownInfrastructure);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    try {
      director.start();
    } catch (DirectorException e) {
      log.error(e.getMessage(), e.getCause());
      System.exit(0);
    } catch (Exception e) {
      log.error("An unexpected error occurred during startup: {}", e.getMessage(), e);
      System.exit(0);
    }
    if (director.isRunning()) {
      // User interrupt will be handled by DirectorCli from this point
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
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
