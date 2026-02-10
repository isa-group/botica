package es.us.isa.botica.director.bot.shutdown;

import es.us.isa.botica.configuration.ShutdownConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.BotStatus;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.protocol.client.ShutdownResponsePacket;
import es.us.isa.botica.protocol.server.ShutdownRequestPacket;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHandler {
  private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);
  @VisibleForTesting static final long FORCE_DELAY_MILLISECONDS = 3000;

  private final Director director;
  private final BotManager botManager;
  private final BoticaServer server;

  private final ShutdownConfiguration configuration;

  public ShutdownHandler(Director director, BotManager botManager, BoticaServer server) {
    this.director = director;
    this.botManager = botManager;
    this.server = server;
    this.configuration = director.getConfiguration().getShutdownConfiguration();
  }

  public void requestShutdown(Bot bot, ShutdownMode mode) {
    boolean force = mode.equals(ShutdownMode.FORCE);
    long timeout = force ? FORCE_DELAY_MILLISECONDS : this.configuration.getTimeout();

    log.debug(
        force ? "Forcing {} to shut down..." : "Sending shutdown request to {}...", bot.getId());
    this.server.sendPacket(
        new ShutdownRequestPacket(force),
        bot.getId(),
        (botId, response) -> this.handleResponse(bot, mode, response),
        botId -> this.handleTimeout(bot, mode),
        timeout,
        TimeUnit.MILLISECONDS);
  }

  private void handleResponse(Bot bot, ShutdownMode mode, ShutdownResponsePacket response) {
    if (!this.director.isRunning() || bot.getLastKnownStatus() == BotStatus.STOPPED) {
      return;
    }

    if (mode.equals(ShutdownMode.FORCE) || response.isReady()) {
      log.info("{} is ready to be shut down. Stopping...", bot.getId());
      this.botManager.shutdown(bot, ShutdownMode.STOP_CONTAINER);
    } else {
      log.info(
          "{} is busy, shutdown cancelled. Consider forcing its shutdown if this keeps happening.",
          bot.getId());
    }
  }

  private void handleTimeout(Bot bot, ShutdownMode mode) {
    if (!this.director.isRunning()) return;
    switch (mode) {
      case REQUEST:
        log.info(
            "{} timed out. Consider forcing its shutdown if this keeps happening.", bot.getId());
        break;
      case FORCE:
        log.info("{} timed out. Shutting down the container...", bot.getId());
        this.botManager.shutdown(bot, ShutdownMode.STOP_CONTAINER);
        break;
    }
  }
}
