package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;

import java.util.Map;

public class BotInstanceConfiguration {
  private String id;
  private boolean persistent;
  private Map<String, String> environment;

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isPersistent() {
    return persistent;
  }

  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }

  public Map<String, String> getEnvironment() {
    return environment;
  }

  public void setEnvironment(Map<String, String> environment) {
    this.environment = environment;
  }

  public BotLifecycleConfiguration getLifecycleConfiguration() {
    return lifecycleConfiguration;
  }

  public void setLifecycleConfiguration(BotLifecycleConfiguration lifecycleConfiguration) {
    this.lifecycleConfiguration = lifecycleConfiguration;
  }

  @Override
  public String toString() {
    return "BotInstanceConfiguration{"
        + "id='"
        + id
        + '\''
        + ", persistent="
        + persistent
        + ", environment="
        + environment
        + ", lifecycleConfiguration="
        + lifecycleConfiguration
        + '}';
  }
}
