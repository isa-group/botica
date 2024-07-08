package es.us.isa.botica.configuration.bot;

import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class BotPublishConfiguration implements Configuration {
  private String key;
  private String order;

  @Override
  public void validate(ValidationReport report) {
    boolean missingKey = key == null || key.isBlank();
    boolean missingOrder = order == null || order.isBlank();
    if (missingKey ^ missingOrder) {
      if (missingKey) report.addError("key", "missing or empty key");
      if (missingOrder) report.addError("order", "missing or empty order");
    }
  }

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
