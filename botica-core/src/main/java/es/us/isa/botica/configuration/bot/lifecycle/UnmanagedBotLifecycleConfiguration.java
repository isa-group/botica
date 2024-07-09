package es.us.isa.botica.configuration.bot.lifecycle;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class UnmanagedBotLifecycleConfiguration implements BotLifecycleConfiguration {
  @Override
  public void validate(ValidationReport report) {}

  @Override
  public BotLifecycleType getType() {
    return BotLifecycleType.UNMANAGED;
  }

  @Override
  public String toString() {
    return "UnmanagedBotLifecycleConfiguration{}";
  }
}
