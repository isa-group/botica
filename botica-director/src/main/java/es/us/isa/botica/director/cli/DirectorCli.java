package es.us.isa.botica.director.cli;

import ch.qos.logback.classic.LoggerContext;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import java.io.PrintStream;
import java.nio.file.Paths;
import org.fusesource.jansi.AnsiConsole;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.shell.jline3.PicocliCommands;

/** Command line interface for the botica director. */
public class DirectorCli {
  private static final Logger log = LoggerFactory.getLogger(DirectorCli.class);
  private static final String PROMPT = "> ";

  private final Director director;
  private final ShutdownCommand shutdownCommand;

  private int userInterrupts = 0;

  public DirectorCli(Director director) {
    this.director = director;
    this.shutdownCommand = new ShutdownCommand(director);
  }

  public void start() {
    AnsiConsole.systemInstall();
    try {
      CommandLine commands = new CommandLine(CommandSpec.create().name(""));
      commands.addSubcommand(this.shutdownCommand);

      PicocliCommands picocliCommands = new PicocliCommands(commands);
      Terminal terminal = TerminalBuilder.builder().system(true).build();
      Parser parser = new DefaultParser();

      SystemRegistry systemRegistry =
          new SystemRegistryImpl(
              parser, terminal, () -> Paths.get(System.getProperty("user.dir")), null);
      systemRegistry.setCommandRegistries(picocliCommands);
      systemRegistry.register("help", picocliCommands);

      LineReader lineReader =
          LineReaderBuilder.builder()
              .terminal(terminal)
              .completer(systemRegistry.completer())
              .parser(parser)
              .variable(LineReader.LIST_MAX, 50) // max tab completion candidates
              .build();

      configureStandardOutputStream(terminal);
      configureAppender(lineReader);
      startPrompting(lineReader, systemRegistry);
    } catch (Throwable t) {
      log.error("An unexpected error occurred while enabling the command line interface", t);
    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  private void configureStandardOutputStream(Terminal terminal) {
    PrintStream jlineOut = new PrintStream(terminal.output(), true);
    System.setOut(jlineOut);
    System.setErr(jlineOut);
  }

  private void configureAppender(LineReader lineReader) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    JLineAppender appender = new JLineAppender(lineReader);
    appender.setContext(loggerContext);
    appender.start();

    ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    rootLogger.detachAndStopAllAppenders();
    rootLogger.addAppender(appender);
  }

  private void startPrompting(LineReader lineReader, SystemRegistry systemRegistry) {
    while (true) {
      try {
        systemRegistry.cleanUp();
        String line = lineReader.readLine(this.userInterrupts < 1 ? PROMPT : null);
        systemRegistry.execute(line);
      } catch (UserInterruptException | EndOfFileException ignored) {
        this.handleUserInterrupt();
      } catch (Exception e) {
        systemRegistry.trace(e);
      }
    }
  }

  private void handleUserInterrupt() {
    if (this.userInterrupts == 0) {
      log.info(
          "Requesting bots to shut down... Press CTRL+C again to force the system to shut down.");
      new Thread(() -> this.director.shutdown(ShutdownMode.FORCE, () -> System.exit(0))).start();
    } else if (this.userInterrupts == 1) {
      log.info("Forcing system shutdown...");
      new Thread(this::shutdownSystem).start();
    }
    this.userInterrupts++;
  }

  private void shutdownSystem() {
    this.director.shutdownInfrastructure();
    System.exit(0);
  }
}
