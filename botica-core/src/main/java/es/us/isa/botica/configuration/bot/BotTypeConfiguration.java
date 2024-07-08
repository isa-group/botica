package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BotTypeConfiguration implements Configuration {
  private String id;
  private String image;

  @JsonProperty("mount")
  private List<BotMountConfiguration> mounts = Collections.emptyList();

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration;

  @JsonProperty("publish")
  private BotPublishConfiguration publishConfiguration;

  private Map<String, BotInstanceConfiguration> instances = Collections.emptyMap();

  @Override
  public void validate(ValidationReport report) {
    if (id == null || id.isBlank()) report.addError("id", "missing or empty id");
    if (image == null || image.isBlank()) report.addError("image", "missing or empty image");
    if (instances.isEmpty()) {
      report.addWarning("instances", "missing or empty instances");
    } else {
      instances.forEach((id, instance) -> report.registerChild("instances." + id, instance));
    }
    report.registerChild("mounts", mounts);
    report.registerChild("lifecycle", lifecycleConfiguration);
    report.registerChild("publish", publishConfiguration);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Map<String, BotInstanceConfiguration> getInstances() {
    return instances;
  }

  public void setInstances(Map<String, BotInstanceConfiguration> instances) {
    this.instances = instances;
    instances.forEach((id, instance) -> instance.setId(id));
  }

  @Override
  public String toString() {
    return "BotConfiguration{"
        + "id='"
        + id
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
        + ", instanceConfigurations="
        + instances
        + '}';
  }
}
