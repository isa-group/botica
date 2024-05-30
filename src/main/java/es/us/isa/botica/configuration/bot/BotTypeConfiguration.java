package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.List;

public class BotTypeConfiguration implements Configuration {
  private String name;
  private String image;

  @JsonProperty("mount")
  private List<BotMountConfiguration> mounts = Collections.emptyList();

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  @JsonProperty("publish")
  private BotPublishConfiguration publishConfiguration;

  @JsonProperty("subscribe")
  private List<String> subscribeKeys = Collections.emptyList();

  private List<BotInstanceConfiguration> instances = Collections.emptyList();

  @Override
  public void validate(ValidationReport report) {
    if (name == null || name.isBlank()) report.addError("name", "missing or empty name");
    if (image == null || image.isBlank()) report.addError("image", "missing or empty image");
    if (instances.isEmpty()) {
      report.addWarning("instances", "missing or empty instances");
    } else {
      report.registerChild("instances", instances);
    }
    report.registerChild("mounts", mounts);
    report.registerChild("lifecycle", lifecycleConfiguration);
    report.registerChild("publish", publishConfiguration);
  }

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
