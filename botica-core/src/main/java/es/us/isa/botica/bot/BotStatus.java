package es.us.isa.botica.bot;

public enum BotStatus {
  /** The bot is not managed by botica. */
  UNMANAGED,

  /** The bot is confirmed to be down. */
  STOPPED,

  /**
   * The bot's status is unknown. It may still be running, but the last heartbeat was not recent.
   */
  UNKNOWN,

  /** The bot has not yet sent a ready packet. */
  STARTING,

  /** The bot is confirmed to be running based on a recent heartbeat. */
  RUNNING
}
