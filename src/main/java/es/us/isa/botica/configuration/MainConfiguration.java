package es.us.isa.botica.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.util.configuration.ConfigurationFile;
import java.util.List;

public class MainConfiguration implements ConfigurationFile {
  @JsonProperty("broker")
  private BrokerConfiguration brokerConfiguration;

  private List<BotTypeConfiguration> botTypes;

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
