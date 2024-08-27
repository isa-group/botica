package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class BotSubscribeConfiguration implements Configuration {
  public enum RoutingStrategy {
    @JsonProperty("distributed")
    DISTRIBUTED,
    @JsonProperty("broadcast")
    BROADCAST
  }

  private String key;
  private RoutingStrategy strategy = RoutingStrategy.DISTRIBUTED;

  @Override
  public void validate(ValidationReport report) {
    if (key == null || key.isBlank()) report.addError("key", "missing or empty key");
    if (strategy == null) report.addError("strategy", "strategy cannot be null");
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public RoutingStrategy getStrategy() {
    return strategy;
  }

  public void setStrategy(RoutingStrategy strategy) {
    this.strategy = strategy;
  }
}
