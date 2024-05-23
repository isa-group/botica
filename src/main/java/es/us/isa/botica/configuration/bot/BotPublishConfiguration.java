package es.us.isa.botica.configuration.bot;

public class BotPublishConfiguration {
  private String key;
  private String order;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "BotPublishConfiguration{" + "key='" + key + '\'' + ", order='" + order + '\'' + '}';
  }
}
