package es.us.isa.botica.director.bot;

import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import java.time.Instant;
import java.util.Objects;

public class Bot {
  private final BotTypeConfiguration typeConfiguration;
  private final BotInstanceConfiguration configuration;
  private String containerId;
  private BotStatus lastKnownStatus = BotStatus.STOPPED;
  private Instant lastHeartbeat;

  public Bot(BotTypeConfiguration typeConfiguration, BotInstanceConfiguration configuration) {
    this.typeConfiguration = typeConfiguration;
    this.configuration = configuration;
  }

  public String getId() {
    return configuration.getId();
  }

  public BotTypeConfiguration getTypeConfiguration() {
    return typeConfiguration;
  }

  public BotInstanceConfiguration getConfiguration() {
    return configuration;
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  public BotStatus getLastKnownStatus() {
    return lastKnownStatus;
  }

  public void setLastKnownStatus(BotStatus lastKnownStatus) {
    this.lastKnownStatus = lastKnownStatus;
  }

  public Instant getLastHeartbeat() {
    return lastHeartbeat;
  }

  public void updateLastHeartbeat() {
    this.lastHeartbeat = Instant.now();
  }

  @Override
  public String toString() {
    return "Bot{"
        + "id="
        + this.getId()
        + ", typeConfiguration="
        + typeConfiguration
        + ", configuration="
        + configuration
        + ", containerId='"
        + containerId
        + '\''
        + ", lastKnownStatus="
        + lastKnownStatus
        + ", lastHeartbeat="
        + lastHeartbeat
        + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof Bot)) return false;
    Bot bot = (Bot) object;
    return Objects.equals(this.getId(), bot.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getId());
  }
}
