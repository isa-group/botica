package es.us.isa.botica.util.configuration;

import java.io.File;

/**
 * {@link Configuration} loader interface.
 *
 * @author Alberto Mimbrero
 */
public interface ConfigurationFileLoader {
  <T extends Configuration> T load(File file, Class<T> configurationFileClass);

  void write(Configuration configuration, File file);
}
