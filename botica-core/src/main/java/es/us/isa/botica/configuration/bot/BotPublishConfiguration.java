package es.us.isa.botica.configuration.bot;

import es.us.isa.botica.util.configuration.Configuration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class BotPublishConfiguration implements Configuration {
  private String defaultKey;
  private String defaultAction;

  @Override
  public void validate(ValidationReport report) {
    boolean missingKey = defaultKey == null || defaultKey.isBlank();
    boolean missingAction = defaultAction == null || defaultAction.isBlank();
    if (missingKey ^ missingAction) {
      if (missingKey) report.addError("defaultKey", "missing or empty default key");
      if (missingAction) report.addError("defaultAction", "missing or empty default action");
    }
  }

  public String getDefaultKey() {
    return defaultKey;
  }

  public void setDefaultKey(String defaultKey) {
    this.defaultKey = defaultKey;
  }

  public String getDefaultAction() {
    return defaultAction;
  }

  public void setDefaultAction(String defaultAction) {
    this.defaultAction = defaultAction;
  }

  @Override
  public String toString() {
    return "BotPublishConfiguration{"
        + "defaultKey='"
        + defaultKey
        + '\''
        + ", defaultAction='"
        + defaultAction
        + '\''
        + '}';
  }
}
