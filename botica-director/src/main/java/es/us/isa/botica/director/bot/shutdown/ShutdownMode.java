package es.us.isa.botica.director.bot.shutdown;

public enum ShutdownMode {
  /**
   * Requests a bot to shut down. If the bot is busy, it can cancel the shutdown, and another
   * shutdown request will need to be sent later.
   */
  REQUEST,

  /**
   * Forces a bot to shut down. The bot will have a grace period to save important files before
   * termination.
   */
  FORCE,

  /**
   * Immediately stops the bot's container without prior notice.
   */
  STOP_CONTAINER
}
