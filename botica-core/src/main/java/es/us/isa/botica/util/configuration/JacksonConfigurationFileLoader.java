package es.us.isa.botica.util.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;

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
      return mapper.readValue(file, configurationFileClass);
    } catch (Exception e) {
      // TODO: maybe implement syntax checking
      throw new ConfigurationLoadingException(
          "Unable to read the configuration file at "
              + file.getAbsolutePath()
              + ". Please check for any syntax errors.");
    }
  }
}
