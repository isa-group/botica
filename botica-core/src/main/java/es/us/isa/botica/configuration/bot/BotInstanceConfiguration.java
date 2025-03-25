package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotInstanceConfiguration implements Configuration {
  @JsonBackReference private BotTypeConfiguration typeConfiguration;

  private String id;

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  private List<String> environment = Collections.emptyList();

  @Override
  public void validate(ValidationReport report) {
    if (id == null || id.isBlank()) report.addError("id", "missing or empty id");
    if (lifecycleConfiguration != null) {
      report.registerChild("lifecycle", lifecycleConfiguration);
    }
  }

  public BotTypeConfiguration getTypeConfiguration() {
    return typeConfiguration;
  }

  public void setTypeConfiguration(BotTypeConfiguration typeConfiguration) {
    this.typeConfiguration = typeConfiguration;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public BotLifecycleConfiguration getLifecycleConfiguration() {
    return lifecycleConfiguration != null
        ? lifecycleConfiguration
        : typeConfiguration.getLifecycleConfiguration();
  }

  public BotLifecycleConfiguration getOwnLifecycleConfiguration() {
    return lifecycleConfiguration;
  }

  public void setOwnLifecycleConfiguration(BotLifecycleConfiguration lifecycleConfiguration) {
    this.lifecycleConfiguration = lifecycleConfiguration;
  }

  public List<String> getEnvironment() {
    ArrayList<String> env = new ArrayList<>(typeConfiguration.getEnvironment());
    env.addAll(environment);
    return env;
  }

  public List<String> getOwnEnvironment() {
    return environment;
  }

  public void setOwnEnvironment(List<String> environment) {
    this.environment = environment;
  }

  @Override
  public String toString() {
    return "BotInstanceConfiguration{"
        + "id='"
        + id
        + '\''
        + ", environment="
        + environment
        + ", lifecycleConfiguration="
        + lifecycleConfiguration
        + '}';
  }
}
