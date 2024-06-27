package es.us.isa.botica.configuration.bot.lifecycle;

import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Arrays;
import java.util.stream.Collectors;

/** Class representing an invalid or unsupported broker type. */
public class InvalidBotLifecycleConfiguration implements BotLifecycleConfiguration {
  @Override
  public BotLifecycleType getType() {
    return null;
  }

  @Override
  public void validate(ValidationReport report) {
    String supportedTypes =
        Arrays.stream(BotLifecycleType.values())
            .map(BotLifecycleType::getName)
            .collect(Collectors.joining(", "));

    report.addError(
        "type", "invalid or missing lifecycle type. Currently supported types: %s", supportedTypes);
  }
}
