package es.us.isa.botica.configuration;

import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class ShutdownConfiguration implements Configuration {
  private long timeout = 10000;

  @Override
  public void validate(ValidationReport report) {}

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    return "ShutdownConfiguration{" + "timeout=" + timeout + '}';
  }
}
