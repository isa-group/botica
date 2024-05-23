package es.us.isa.botica.configuration.bot.lifecycle;

public class ReactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  private String order;

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "ReactiveBotLifecycleConfiguration{" +
           "order='" + order + '\'' +
           '}';
  }
}
