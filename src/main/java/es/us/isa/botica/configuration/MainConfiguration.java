package es.us.isa.botica.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.List;

public class MainConfiguration implements Configuration {
  @JsonProperty("broker")
  private BrokerConfiguration brokerConfiguration;

  @JsonProperty("bots")
  private List<BotTypeConfiguration> botTypes = Collections.emptyList();

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
      report.registerChild("bots", botTypes);
    }
  }

  public BrokerConfiguration getBrokerConfiguration() {
    return brokerConfiguration;
  }

  public void setBrokerConfiguration(BrokerConfiguration brokerConfiguration) {
    this.brokerConfiguration = brokerConfiguration;
  }

  public List<BotTypeConfiguration> getBotTypes() {
    return botTypes;
  }

  public void setBotTypes(List<BotTypeConfiguration> botTypes) {
    this.botTypes = botTypes;
  }

  @Override
  public String toString() {
    return "MainConfiguration{"
        + "brokerConfiguration="
        + brokerConfiguration
        + ", botTypes="
        + botTypes
        + '}';
  }
}
