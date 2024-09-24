package es.us.isa.botica.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.configuration.docker.DockerConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainConfiguration implements Configuration {
  @JsonProperty("docker")
  private DockerConfiguration dockerConfiguration = new DockerConfiguration();

  @JsonProperty("broker")
  private BrokerConfiguration brokerConfiguration;

  @JsonProperty("bots")
  private Map<String, BotTypeConfiguration> botTypes = Collections.emptyMap();

  @JsonProperty("shutdown")
  private ShutdownConfiguration shutdownConfiguration = new ShutdownConfiguration();

  @Override
  public void validate(ValidationReport report) {
    if (brokerConfiguration == null) {
      report.addError("broker", "missing broker configuration");
    } else {
      report.registerChild("broker", brokerConfiguration);
    }

    if (botTypes.isEmpty()) {
      report.addWarning("bots", "missing or empty bots declaration");
    } else {
      Set<String> duplicateBotIds = this.getDuplicateBotIds();
      if (!duplicateBotIds.isEmpty()) {
        report.addError("bots", "duplicate bot IDs: %s", String.join(", ", duplicateBotIds));
      }
      botTypes.forEach((name, botType) -> report.registerChild("bots." + name, botType));
    }

    report.registerChild("shutdown", shutdownConfiguration);
  }

  private Set<String> getDuplicateBotIds() {
    Set<String> distinctBotIds = new HashSet<>();
    return this.botTypes.values().stream()
        .flatMap(type -> type.getInstances().keySet().stream())
        .filter(id -> !distinctBotIds.add(id))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public DockerConfiguration getDockerConfiguration() {
    return dockerConfiguration;
  }

  public void setDockerConfiguration(DockerConfiguration dockerConfiguration) {
    this.dockerConfiguration = dockerConfiguration;
  }

  public BrokerConfiguration getBrokerConfiguration() {
    return brokerConfiguration;
  }

  public void setBrokerConfiguration(BrokerConfiguration brokerConfiguration) {
    this.brokerConfiguration = brokerConfiguration;
  }

  public Map<String, BotTypeConfiguration> getBotTypes() {
    return botTypes;
  }

  public void setBotTypes(Map<String, BotTypeConfiguration> botTypes) {
    this.botTypes = botTypes;
    botTypes.forEach((name, botType) -> botType.setId(name));
  }

  public ShutdownConfiguration getShutdownConfiguration() {
    return shutdownConfiguration;
  }

  public void setShutdownConfiguration(ShutdownConfiguration shutdownConfiguration) {
    this.shutdownConfiguration = shutdownConfiguration;
  }

  @Override
  public String toString() {
    return "MainConfiguration{"
        + "dockerConfiguration="
        + dockerConfiguration
        + ", brokerConfiguration="
        + brokerConfiguration
        + ", botTypes="
        + botTypes
        + ", shutdownConfiguration="
        + shutdownConfiguration
        + '}';
  }
}
