package es.us.isa.botica.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.BotConfiguration;
import es.us.isa.botica.configuration.broker.BrokerConfiguration;
import es.us.isa.botica.util.configuration.ConfigurationFile;
import java.util.List;

public class MainConfigurationFile implements ConfigurationFile {
  @JsonProperty("broker")
  private BrokerConfiguration brokerConfiguration;

  private List<BotConfiguration> bots;

  public BrokerConfiguration getBrokerConfiguration() {
    return brokerConfiguration;
  }

  public void setBrokerConfiguration(BrokerConfiguration brokerConfiguration) {
    this.brokerConfiguration = brokerConfiguration;
  }

  public List<BotConfiguration> getBots() {
    return bots;
  }

  public void setBots(List<BotConfiguration> bots) {
    this.bots = bots;
  }

  @Override
  public String toString() {
    return "MainConfigurationFile{"
        + "brokerConfiguration="
        + brokerConfiguration
        + ", bots="
        + bots
        + '}';
  }
}
