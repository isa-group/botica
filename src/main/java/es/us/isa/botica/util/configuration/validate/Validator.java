package es.us.isa.botica.util.configuration.validate;

public class Validator {
  public ValidationReport validate(Validatable validatable) {
    ValidationReport report = new ValidationReport();
    validatable.validate(report);
    return report;
  }
}
