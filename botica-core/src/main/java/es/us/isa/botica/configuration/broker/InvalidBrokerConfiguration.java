package es.us.isa.botica.configuration.broker;

import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Class representing an invalid or unsupported broker type. */
public class InvalidBrokerConfiguration implements BrokerConfiguration {
  @Override
  public void validate(ValidationReport report) {
    String supportedTypes =
        Arrays.stream(BrokerType.values())
            .map(BrokerType::getName)
            .collect(Collectors.joining(", "));

    report.addError(
        "type", "invalid or missing broker type. Currently supported types: %s", supportedTypes);
  }
}
