package es.us.isa.botica.configuration.bot.lifecycle;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class ReactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  private String order;

  @Override
  public void validate(ValidationReport report) {}

  @Override
  public BotLifecycleType getType() {
    return BotLifecycleType.REACTIVE;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "ReactiveBotLifecycleConfiguration{" + "order='" + order + '\'' + '}';
  }
}
