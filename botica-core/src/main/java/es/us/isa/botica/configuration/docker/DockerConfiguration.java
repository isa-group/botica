package es.us.isa.botica.configuration.docker;

import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class DockerConfiguration implements Configuration {
  private String host;

  @Override
  public void validate(ValidationReport report) {}

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public String toString() {
    return "DockerConfiguration{" + "host='" + host + '\'' + '}';
  }
}
