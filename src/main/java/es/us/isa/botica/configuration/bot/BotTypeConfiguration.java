package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;

import java.util.List;

public class BotTypeConfiguration {
  private String name;
  private String image;

  @JsonProperty("mount")
  private List<BotMountConfiguration> mounts;

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  @JsonProperty("publish")
  private BotPublishConfiguration publishConfiguration;

  @JsonProperty("subscribe")
  private List<String> subscribeKeys;

  private List<BotInstanceConfiguration> instances;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public List<BotMountConfiguration> getMounts() {
    return mounts;
  }

  public void setMounts(List<BotMountConfiguration> mounts) {
    this.mounts = mounts;
  }

  public BotLifecycleConfiguration getLifecycleConfiguration() {
    return lifecycleConfiguration;
  }

  public void setLifecycleConfiguration(BotLifecycleConfiguration lifecycleConfiguration) {
    this.lifecycleConfiguration = lifecycleConfiguration;
  }

  public BotPublishConfiguration getPublishConfiguration() {
    return publishConfiguration;
  }

  public void setPublishConfiguration(BotPublishConfiguration publishConfiguration) {
    this.publishConfiguration = publishConfiguration;
  }

  public List<String> getSubscribeKeys() {
    return subscribeKeys;
  }

  public void setSubscribeKeys(List<String> subscribeKeys) {
    this.subscribeKeys = subscribeKeys;
  }

  public List<BotInstanceConfiguration> getInstances() {
    return instances;
  }

  public void setInstances(List<BotInstanceConfiguration> instances) {
    this.instances = instances;
  }

  @Override
  public String toString() {
    return "BotConfiguration{"
           + "name='"
           + name
           + '\''
           + ", image='"
           + image
           + '\''
           + ", mountConfigurations="
           + mounts
           + ", lifecycleConfiguration="
           + lifecycleConfiguration
           + ", publishConfiguration="
           + publishConfiguration
           + ", subscribeKeys="
           + subscribeKeys
           + ", instanceConfigurations="
           + instances
           + '}';
  }
}
