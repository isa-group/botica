package es.us.isa.botica.director.bot;

import static es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleType.UNMANAGED;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.director.Director;
import es.us.isa.botica.director.bot.shutdown.ShutdownHandler;
import es.us.isa.botica.director.bot.shutdown.ShutdownMode;
import es.us.isa.botica.director.deploy.BotDeploymentHandler;
import es.us.isa.botica.director.protocol.BoticaServer;
import es.us.isa.botica.protocol.HeartbeatPacket;
import es.us.isa.botica.protocol.client.ReadyPacket;
import es.us.isa.botica.util.ExecutorUtils;
import es.us.isa.botica.util.annotation.VisibleForTesting;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotManager {
  private static final Logger log = LoggerFactory.getLogger(BotManager.class);
  private static final long BOT_TIMEOUT_SECONDS = 5;
  private static final long HEARTBEAT_RATE_SECONDS = 2;

  private final Director director;
  private final BotDeploymentHandler deploymentHandler;
  private final BoticaServer server;
  private final ScheduledExecutorService executorService;
  private final ShutdownHandler shutdownHandler;

  private final Map<String, Bot> bots = new HashMap<>();

  private Runnable systemShutdownCallback = null;

  public BotManager(
      Director director, BotDeploymentHandler deploymentHandler, BoticaServer server) {
    this.director = director;
    this.deploymentHandler = deploymentHandler;
    this.server = server;
    this.executorService = ExecutorUtils.newDaemonSingleThreadScheduledExecutor();
    this.shutdownHandler = new ShutdownHandler(director, this, server);

    server.registerPacketListener(ReadyPacket.class, this::onBotReady);
    server.registerPacketListener(HeartbeatPacket.class, this::onBotHeartbeat);
  }

  @VisibleForTesting
  BotManager(
      Director director,
      BotDeploymentHandler deploymentHandler,
      BoticaServer server,
      ScheduledExecutorService executorService,
      ShutdownHandler shutdownHandler) {
    this.director = director;
    this.deploymentHandler = deploymentHandler;
    this.server = server;
    this.executorService = executorService;
    this.shutdownHandler = shutdownHandler;

    server.registerPacketListener(ReadyPacket.class, this::onBotReady);
    server.registerPacketListener(HeartbeatPacket.class, this::onBotHeartbeat);
  }

  public void deploy() {
    MainConfiguration mainConfiguration = this.director.getMainConfiguration();
    for (BotTypeConfiguration typeConfiguration : mainConfiguration.getBotTypes().values()) {
      for (BotInstanceConfiguration botConfiguration : typeConfiguration.buildInstances()) {
        Bot bot = new Bot(typeConfiguration, botConfiguration);
        this.register(bot);
        this.deploy(bot);
      }
    }

    this.startHeartbeatScheduler();
  }

  @VisibleForTesting
  void startHeartbeatScheduler() {
    this.executorService.scheduleAtFixedRate(
        this::heartbeat, HEARTBEAT_RATE_SECONDS, HEARTBEAT_RATE_SECONDS, TimeUnit.SECONDS);
  }

  public void register(Bot bot) {
    this.bots.put(bot.getId(), bot);
  }

  public void deploy(Bot bot) {
    if (!this.director.isRunning()) {
      return;
    }
    log.info("Creating {} container...", bot.getId());
    String containerId = deploymentHandler.createContainer(bot);
    bot.setContainerId(containerId);

    log.info("Starting {}...", bot.getId());
    deploymentHandler.startContainer(containerId);

    if (bot.getTypeConfiguration().getLifecycleConfiguration().getType().equals(UNMANAGED)) {
      bot.setLastKnownStatus(BotStatus.UNMANAGED);
    } else {
      bot.setLastKnownStatus(BotStatus.STARTING);
    }
  }

  private void heartbeat() {
    this.getBots().stream()
        .filter(BotManager::isBotReady)
        .forEach(
            bot -> {
              if (bot.getLastHeartbeat()
                      .isBefore(Instant.now().minus(BOT_TIMEOUT_SECONDS, ChronoUnit.SECONDS))
                  && !bot.getLastKnownStatus().equals(BotStatus.UNKNOWN)) {
                bot.setLastKnownStatus(BotStatus.UNKNOWN);
                log.info("{} is not responding", bot.getId());
              }

              this.server.sendPacket(new HeartbeatPacket(), bot.getId());
            });
  }

  private static boolean isBotReady(Bot bot) {
    return bot.getLastKnownStatus() == BotStatus.UNKNOWN
        || bot.getLastKnownStatus() == BotStatus.RUNNING;
  }

  public void shutdownSystem(ShutdownMode mode, Runnable callback) {
    this.systemShutdownCallback = callback;
    this.getBots().stream()
        .filter(bot -> !bot.getLastKnownStatus().equals(BotStatus.STOPPED))
        .forEach(bot -> this.shutdown(bot, mode));
  }

  public void shutdown(Bot bot, ShutdownMode mode) {
    switch (mode) {
      case REQUEST:
      case FORCE:
        this.shutdownHandler.requestShutdown(bot, mode);
        break;
      case STOP_CONTAINER:
        this.deploymentHandler.stopContainer(bot.getContainerId());
        bot.setLastKnownStatus(BotStatus.STOPPED);
        break;
    }

    if (this.systemShutdownCallback != null
        && this.getBots().stream()
            .allMatch(b -> b.getLastKnownStatus().equals(BotStatus.STOPPED))) {
      this.systemShutdownCallback.run();
    }
  }

  private void onBotReady(String botId, ReadyPacket readyPacket) {
    Bot bot = this.getBot(botId);
    log.debug("{} is up and ready. Connection established.", botId); // TODO
    bot.updateLastHeartbeat();
    bot.setLastKnownStatus(BotStatus.RUNNING);
  }

  private void onBotHeartbeat(String botId, HeartbeatPacket heartbeatPacket) {
    Bot bot = this.getBot(botId);
    log.trace("{} heartbeat received", botId);
    bot.updateLastHeartbeat();
    if (!bot.getLastKnownStatus().equals(BotStatus.RUNNING)) {
      bot.setLastKnownStatus(BotStatus.RUNNING);
      log.info("{} is back responding", botId);
    }
  }

  public Bot getBot(String id) {
    return this.bots.get(id);
  }

  public Collection<Bot> getBots() {
    return this.bots.values();
  }
}
