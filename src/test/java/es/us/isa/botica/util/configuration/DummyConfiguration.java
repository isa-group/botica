package es.us.isa.botica.util.configuration;

import es.us.isa.botica.util.configuration.validate.ValidationReport;

public class DummyConfiguration implements Configuration {
  public String string = "";
  public InnerObject object;

  @Override
  public void validate(ValidationReport report) {
    if (string == null || string.isEmpty()) {
      report.addWarning("string", "missing or empty string");
    }

    if (object == null) {
      report.addError("object", "object must be declared");
    } else {
      report.registerChild("object", object);
    }
  }

  public static class InnerObject implements Configuration {
    public Integer integer;

    @Override
    public void validate(ValidationReport report) {
      if (integer != null && integer < 0) report.addWarning("integer", "negative number");
    }
  }
}
