package es.us.isa.botica.director.cli;

import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "shutdown",
    aliases = "stop",
    mixinStandardHelpOptions = true,
    description = "Shuts down the whole infrastructure.")
public class ShutdownCommand implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(ShutdownCommand.class);
  private final Director director;

  @Option(names = {"--force", "-f"})
  private boolean forced;

  public ShutdownCommand(Director director) {
    this.director = director;
  }

  @Override
  public void run() {
    log.info(this.forced ? "Forcing shutdown..." : "Requesting bots to shut down...");
    this.director.shutdown(this.getMode(), () -> System.exit(0));
  }

  private ShutdownMode getMode() {
    return this.forced ? ShutdownMode.FORCE : ShutdownMode.REQUEST;
  }
}
