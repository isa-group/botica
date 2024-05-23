package es.us.isa.botica.util.configuration;

import java.io.File;

/**
 * {@link ConfigurationFile} loader interface.
 *
 * @author Alberto Mimbrero
 * @see JacksonConfigurationFileLoader
 */
public interface ConfigurationFileLoader {
  <T extends ConfigurationFile> T load(File file, Class<T> configurationFileClass);
}
