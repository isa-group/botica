package es.us.isa.botica.util.configuration.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import java.io.File;
import java.nio.file.Files;

/**
 * Configuration loader for YAML and JSON formats using Jackson.
 *
 * @author Alberto Mimbrero
 */
public class JacksonConfigurationFileLoader implements ConfigurationFileLoader {
  private final ObjectMapper mapper =
      new ObjectMapper(new YAMLFactory())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public <T extends Configuration> T load(File file, Class<T> configurationFileClass) {
    if (!file.isFile()) {
      throw new ConfigurationLoadingException(
          "Unable to load the configuration file: "
              + file.getAbsolutePath()
              + " is not a file or does not exist");
    }

    try {
      return PropertyPlaceholderResolver.resolve(
          mapper, Files.readString(file.toPath()), configurationFileClass);
    } catch (Exception e) {
      throw new ConfigurationLoadingException(
          String.format(
              "Unable to read the configuration file at %s: %s",
              file.getAbsolutePath(), e.getMessage()));
    }
  }
}
