package es.us.isa.botica.configuration.bot.lifecycle;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class ProactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  private long initialDelay;
  private long period;

  @Override
  public void validate(ValidationReport report) {
    if (initialDelay < 0) report.addError("initialDelay", "initialDelay must be positive");
    if (period == 0) report.addError("period", "period cannot be 0");
  }

  @Override
  public BotLifecycleType getType() {
    return BotLifecycleType.PROACTIVE;
  }

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
