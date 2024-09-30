package es.us.isa.botica.director;

import es.us.isa.botica.director.cli.DirectorCli;
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

    Director director = startDirector(mainConfigurationFile);
    DirectorCli cli = new DirectorCli(director);
    new Thread(cli::start).start();
  }

  private static Director startDirector(File mainConfigurationFile) {
    Director director = new Director(mainConfigurationFile);
    Thread shutdownHook = new Thread(director::shutdownInfrastructure);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    try {
      director.start();
    } catch (DirectorException e) {
      log.error(e.getMessage());
      System.exit(0);
    } catch (Exception e) {
      log.error("An unexpected error occurred", e);
      System.exit(0);
    }
    if (director.isRunning()) {
      // user interrupt will be handled by DirectorCli from this point
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
    return director;
  }
}
