package es.us.isa.botica.configuration.bot;

import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class BotMountConfiguration implements Configuration {
  private String source;
  private String target;
  private boolean createHostPath = false;

  @Override
  public void validate(ValidationReport report) {
    if (source == null || source.isBlank()) report.addError("source", "missing or empty source");
    if (target == null || target.isBlank()) report.addError("target", "missing or empty target");
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public boolean isCreateHostPath() {
    return createHostPath;
  }

  public void setCreateHostPath(boolean createHostPath) {
    this.createHostPath = createHostPath;
  }

  @Override
  public String toString() {
    return "BotMountConfiguration{"
        + "source='"
        + source
        + '\''
        + ", target='"
        + target
        + '\''
        + ", createHostPath="
        + createHostPath
        + '}';
  }
}
