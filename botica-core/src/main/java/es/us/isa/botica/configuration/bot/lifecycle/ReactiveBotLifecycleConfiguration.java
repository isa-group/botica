package es.us.isa.botica.configuration.bot.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.List;

public class ReactiveBotLifecycleConfiguration implements BotLifecycleConfiguration {
  @JsonProperty("keys")
  private List<String> subscribeKeys;

  private String order;

  @Override
  public void validate(ValidationReport report) {
    if (subscribeKeys == null || subscribeKeys.isEmpty()) {
      report.addError("keys", "missing or empty key list");
    }
    if (order == null || order.isBlank()) report.addError("order", "missing or empty order");
  }

  @Override
  public BotLifecycleType getType() {
    return BotLifecycleType.REACTIVE;
  }

  public List<String> getSubscribeKeys() {
    return subscribeKeys;
  }

  public void setSubscribeKeys(List<String> subscribeKeys) {
    this.subscribeKeys = subscribeKeys;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "ReactiveBotLifecycleConfiguration{"
        + "subscribeKeys="
        + subscribeKeys
        + ", order='"
        + order
        + '\''
        + '}';
  }
}
