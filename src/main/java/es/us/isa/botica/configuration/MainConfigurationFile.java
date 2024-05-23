package es.us.isa.botica.configuration;

import es.us.isa.botica.configuration.bot.BotConfiguration;
import es.us.isa.botica.util.configuration.ConfigurationFile;
import java.util.List;

public class MainConfigurationFile implements ConfigurationFile {
  private List<BotConfiguration> bots;

  public List<BotConfiguration> getBots() {
    return bots;
  }

  public void setBots(List<BotConfiguration> bots) {
    this.bots = bots;
  }

  @Override
  public String toString() {
    return "MainConfiguration{" +
           "bots=" + bots +
           '}';
  }
}
