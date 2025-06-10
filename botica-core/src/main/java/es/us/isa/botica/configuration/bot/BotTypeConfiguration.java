package es.us.isa.botica.configuration.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BotTypeConfiguration implements Configuration {
  private String id;
  private String image;

  @JsonProperty("mount")
  private List<BotMountConfiguration> mounts = Collections.emptyList();

  @JsonProperty("publish")
  private BotPublishConfiguration publishConfiguration = new BotPublishConfiguration();

  @JsonProperty("subscribe")
  private List<BotSubscribeConfiguration> subscribeConfigurations = Collections.emptyList();

  @JsonProperty("lifecycle")
  private BotLifecycleConfiguration lifecycleConfiguration =
      new ReactiveBotLifecycleConfiguration();

  private int replicas = 1;

  private List<String> environment = Collections.emptyList();

  @JsonManagedReference
  private Map<String, BotInstanceConfiguration> instances = Collections.emptyMap();

  @Override
  public void validate(ValidationReport report) {
    if (id == null || id.isBlank()) report.addError("id", "missing or empty id");
    if (image == null || image.isBlank()) report.addError("image", "missing or empty image");
    if (replicas < 0) {
      report.addError("replicas", "negative number of replicas");
    }
    instances.forEach((id, instance) -> report.registerChild("instances." + id, instance));
    report.registerChild("mounts", mounts);
    report.registerChild("publish", publishConfiguration);
    report.registerChild("subscribe", subscribeConfigurations);
    report.registerChild("lifecycle", lifecycleConfiguration);
  }

  @JsonIgnore
  public List<BotInstanceConfiguration> buildInstances() {
    List<BotInstanceConfiguration> typeInstances =
        new ArrayList<>(this.getDeclaredInstances().values());

    for (int i = 1; i <= this.replicas; i++) {
      BotInstanceConfiguration botConfiguration = new BotInstanceConfiguration();
      botConfiguration.setTypeConfiguration(this);
      botConfiguration.setId(String.format("%s-%d", this.id, i));
      typeInstances.add(botConfiguration);
    }
    return typeInstances;
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

  public List<BotSubscribeConfiguration> getSubscribeConfigurations() {
    return subscribeConfigurations;
  }

  public void setSubscribeConfigurations(List<BotSubscribeConfiguration> subscribeConfigurations) {
    this.subscribeConfigurations = subscribeConfigurations;
  }

  public int getReplicas() {
    return replicas;
  }

  public void setReplicas(int replicas) {
    this.replicas = replicas;
  }

  public List<String> getEnvironment() {
    return environment;
  }

  public void setEnvironment(List<String> environment) {
    this.environment = environment;
  }

  @JsonProperty("instances")
  public Map<String, BotInstanceConfiguration> getDeclaredInstances() {
    return instances;
  }

  @JsonProperty("instances")
  public void setDeclaredInstances(Map<String, BotInstanceConfiguration> instances) {
    this.instances = instances;
    instances.forEach((id, instance) -> instance.setId(id));
  }

  @Override
  public String toString() {
    return "BotTypeConfiguration{"
        + "id='"
        + id
        + '\''
        + ", image='"
        + image
        + '\''
        + ", mounts="
        + mounts
        + ", publishConfiguration="
        + publishConfiguration
        + ", subscribeConfigurations="
        + subscribeConfigurations
        + ", lifecycleConfiguration="
        + lifecycleConfiguration
        + ", replicas="
        + replicas
        + ", environment="
        + environment
        + ", instances="
        + instances
        + '}';
  }
}
