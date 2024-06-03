package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.List;

public class BotInstanceConfiguration implements Configuration {
  private String id;
  private Boolean persistent;
  private List<String> environment = Collections.emptyList();

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  @Override
  public void validate(ValidationReport report) {
    if (id == null || id.isBlank()) report.addError("id", "missing or empty id");
    if (persistent == null) report.addError("persistent", "missing property");
    if (lifecycleConfiguration != null) {
        report.registerChild("lifecycle", lifecycleConfiguration);
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isPersistent() {
    return persistent;
  }

  public void setPersistent(Boolean persistent) {
    this.persistent = persistent;
  }

  public List<String> getEnvironment() {
    return environment;
  }

  public void setEnvironment(List<String> environment) {
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
