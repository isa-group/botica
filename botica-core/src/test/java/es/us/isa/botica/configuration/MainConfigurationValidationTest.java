package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.ValidationResult;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class MainConfigurationValidationTest {
  @Test
  void testDuplicateBotIds() {
    MainConfiguration configuration = new MainConfiguration();

    Map<String, BotTypeConfiguration> botTypes = new LinkedHashMap<>();
    botTypes.put("foo", createBotType("id-1", "id-2"));
    botTypes.put("bar", createBotType("id-3"));
    botTypes.put("baz", createBotType("id-1", "id-4"));
    botTypes.put("qux", createBotType("id-1"));
    botTypes.put("quux", createBotType("id-4"));
    configuration.setBotTypes(botTypes);

    ValidationReport report = new ValidationReport();
    configuration.validate(report);

    assertThat(report.getResults("bots"))
        .hasSize(1)
        .first()
        .extracting(ValidationResult::getMessage)
        .isEqualTo("duplicate bot IDs: id-1, id-4");
  }

  private static BotTypeConfiguration createBotType(String... botIds) {
    BotTypeConfiguration botTypeConfiguration = new BotTypeConfiguration();
    botTypeConfiguration.setInstances(new LinkedHashMap<>());
    for (String botId : botIds) {
      botTypeConfiguration.getInstances().put(botId, new BotInstanceConfiguration());
    }
    return botTypeConfiguration;
  }
}
