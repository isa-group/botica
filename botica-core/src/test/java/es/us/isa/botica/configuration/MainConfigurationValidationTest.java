package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.ValidationResult.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MainConfiguration Validation Tests")
class MainConfigurationValidationTest {
  @Test
  @DisplayName("validate should report an error for duplicate bot IDs across different bot types")
  void validate_duplicateBotIds_reportsSingleErrorWithAllDuplicates() {
    // Arrange
    MainConfiguration configuration = new MainConfiguration();
    Map<String, BotTypeConfiguration> botTypes = new LinkedHashMap<>();
    botTypes.put("type-A", createBotTypeWithInstances("bot-1", "bot-2"));
    botTypes.put("type-B", createBotTypeWithInstances("bot-3"));
    botTypes.put("type-C", createBotTypeWithInstances("bot-1", "bot-4")); // "bot-1" is a duplicate
    botTypes.put("type-D", createBotTypeWithInstances("bot-1")); // "bot-1" is a duplicate
    botTypes.put("type-E", createBotTypeWithInstances("bot-4")); // "bot-4" is a duplicate
    configuration.setBotTypes(botTypes);

    ValidationReport report = new ValidationReport();

    // Act
    configuration.validate(report);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("bots"))
        .hasSize(1)
        .first()
        .satisfies(
            result -> {
              assertThat(result.getType()).isEqualTo(Type.ERROR);
              assertThat(result.getMessage()).isEqualTo("duplicate bot IDs: bot-1, bot-4");
            });
  }

  @Test
  @DisplayName("validate should not report errors when all bot IDs are unique")
  void validate_uniqueBotIds_reportsNoErrors() {
    // Arrange
    MainConfiguration configuration = new MainConfiguration();
    Map<String, BotTypeConfiguration> botTypes = new LinkedHashMap<>();
    botTypes.put("type-A", createBotTypeWithInstances("bot-1", "bot-2"));
    botTypes.put("type-B", createBotTypeWithInstances("bot-3", "bot-4"));
    configuration.setBotTypes(botTypes);

    ValidationReport report = new ValidationReport();

    // Act
    configuration.validate(report);

    // Assert
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.getResults("bots")).isNullOrEmpty();
  }

  @Test
  @DisplayName("validate should report a warning if no bots are declared")
  void validate_noBotsDeclared_reportsWarning() {
    // Arrange
    MainConfiguration configuration = new MainConfiguration();
    configuration.setBotTypes(Collections.emptyMap());
    ValidationReport report = new ValidationReport();

    // Act
    configuration.validate(report);

    // Assert
    assertThat(report.hasWarnings()).isTrue();
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.getResults("bots"))
        .hasSize(1)
        .first()
        .satisfies(
            result -> {
              assertThat(result.getType()).isEqualTo(Type.WARNING);
              assertThat(result.getMessage()).isEqualTo("missing or empty bots declaration");
            });
  }

  @Test
  @DisplayName("validate should register child reports for each bot type")
  void validate_registersChildReportsForEachBotType() {
    // Arrange
    MainConfiguration configuration = new MainConfiguration();
    BotTypeConfiguration invalidBotType = new BotTypeConfiguration(); // Missing 'image' and 'build'
    invalidBotType.setId("invalid-bot");

    Map<String, BotTypeConfiguration> botTypes = new LinkedHashMap<>();
    botTypes.put("invalid-bot", invalidBotType);
    configuration.setBotTypes(botTypes);

    ValidationReport report = new ValidationReport();

    // Act
    configuration.validate(report);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getChildren()).containsKey("bots.invalid-bot");
    ValidationReport childReport = report.getChild("bots.invalid-bot");
    assertThat(childReport.hasErrors()).isTrue();
    assertThat(childReport.getResults("image/build")).hasSize(1);
  }

  // Helper method to create a BotTypeConfiguration with specified bot instance IDs
  private static BotTypeConfiguration createBotTypeWithInstances(String... botIds) {
    BotTypeConfiguration botTypeConfig = new BotTypeConfiguration();
    botTypeConfig.setImage("some-image"); // Make it valid by default
    Map<String, BotInstanceConfiguration> instances = new LinkedHashMap<>();
    for (String botId : botIds) {
      BotInstanceConfiguration botInstance = new BotInstanceConfiguration();
      instances.put(botId, botInstance);
    }
    botTypeConfig.setDeclaredInstances(instances);
    return botTypeConfig;
  }
}
