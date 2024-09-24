package es.us.isa.botica.director.bot.shutdown;

import es.us.isa.botica.configuration.ShutdownConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.Bot;
import es.us.isa.botica.director.bot.BotManager;
import es.us.isa.botica.director.bot.BotStatus;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.protocol.client.ShutdownResponsePacket;
import es.us.isa.botica.protocol.server.ShutdownRequestPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHandler {
  private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);
  private static final int FORCE_DELAY_MILLISECONDS = 3000;

  private final Director director;
  private final BotManager botManager;
  private final BoticaServer server;
  private final ScheduledExecutorService executorService;

  private final ShutdownConfiguration configuration;
  private final Map<Bot, ScheduledFuture<?>> awaitingResponse = new ConcurrentHashMap<>();
  private final Map<Bot, ShutdownMode> requestedShutdowns = new ConcurrentHashMap<>();

  public ShutdownHandler(
      Director director,
      BotManager botManager,
      BoticaServer server,
      ScheduledExecutorService executorService) {
    this.director = director;
    this.botManager = botManager;
    this.server = server;
    this.executorService = executorService;
    this.configuration = director.getMainConfiguration().getShutdownConfiguration();
    server.registerPacketListener(ShutdownResponsePacket.class, this::onShutdownResponse);
  }

  public void requestShutdown(Bot bot, ShutdownMode mode) {
    boolean force = mode.equals(ShutdownMode.FORCE);
    this.cancelLastRequest(bot);
    log.debug(
        force ? "Forcing {} to shut down..." : "Sending shutdown request to {}...", bot.getId());

    ScheduledFuture<?> future =
        this.executorService.schedule(
            () -> this.onTimeout(bot, mode),
            force ? FORCE_DELAY_MILLISECONDS : configuration.getTimeout(),
            TimeUnit.MILLISECONDS);
    this.awaitingResponse.put(bot, future);
    this.requestedShutdowns.put(bot, mode);

    this.server.sendPacket(new ShutdownRequestPacket(force), bot.getId());
  }

  private void onShutdownResponse(String botId, ShutdownResponsePacket response) {
    if (!this.director.isRunning()) return;

    Bot bot = this.botManager.getBot(botId);
    if (bot.getLastKnownStatus() == BotStatus.STOPPED) return;
    this.cancelLastRequest(bot);
    this.awaitingResponse.remove(bot);

    ShutdownMode mode = this.requestedShutdowns.getOrDefault(bot, ShutdownMode.REQUEST);
    if (mode.equals(ShutdownMode.FORCE) || response.isReady()) {
      log.info("{} is ready to be shut down. Stopping...", botId);
      this.botManager.shutdown(bot, ShutdownMode.STOP_CONTAINER);
    } else {
      log.info(
          "{} is busy, shutdown cancelled. Consider forcing its shutdown if this keeps happening.",
          botId);
    }
    this.requestedShutdowns.remove(bot);
  }

  private void cancelLastRequest(Bot bot) {
    ScheduledFuture<?> timeoutFuture = this.awaitingResponse.get(bot);
    if (timeoutFuture != null) {
      timeoutFuture.cancel(false);
    }
  }

  private void onTimeout(Bot bot, ShutdownMode mode) {
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
    this.awaitingResponse.remove(bot);
    this.requestedShutdowns.remove(bot);
  }
}
