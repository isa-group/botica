package es.us.isa.botica.director;

import es.us.isa.botica.director.exception.DirectorException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectorBootstrap {
  private static final Logger log = LoggerFactory.getLogger(DirectorBootstrap.class);
  private static final File DEFAULT_MAIN_CONFIGURATION_FILE = new File("config.yml");

  public static void main(String[] args) {
    File mainConfigurationFile = DEFAULT_MAIN_CONFIGURATION_FILE;
    if (args.length > 0) {
      mainConfigurationFile = new File(args[0]);
    }

    Director director = new Director(mainConfigurationFile);
    Runtime.getRuntime().addShutdownHook(new Thread(director::stop));
    try {
      director.init();
    } catch (DirectorException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      log.error("An unexpected error occurred", e);
    }
  }
}
