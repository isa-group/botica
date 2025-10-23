package es.us.isa.botica.configuration.bot.lifecycle;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class ReactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  private String defaultAction;

  @Override
  public void validate(ValidationReport report) {}

  @Override
  public BotLifecycleType getType() {
    return BotLifecycleType.REACTIVE;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  @Override
  public String toString() {
    return "ReactiveBotLifecycleConfiguration{" + "defaultAction='" + defaultAction + '\'' + '}';
  }
}
