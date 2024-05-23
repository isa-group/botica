package es.us.isa.botica.configuration.bot.lifecycle;

public class ProactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  private long initialDelay;
  private long period;

  public long getInitialDelay() {
    return initialDelay;
  }

  public void setInitialDelay(long initialDelay) {
    this.initialDelay = initialDelay;
  }

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  @Override
  public String toString() {
    return "ProactiveBotLifecycleConfiguration{"
        + "initialDelay="
        + initialDelay
        + ", period="
        + period
        + '}';
  }
}
