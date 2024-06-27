package es.us.isa.botica.util.configuration;

/**
 * Exception thrown when there is an issue loading the configuration file.
 *
 * @author Alberto Mimbrero
 */
public class ConfigurationLoadingException extends RuntimeException {
  public ConfigurationLoadingException(String message) {
    super(message);
  }
}
