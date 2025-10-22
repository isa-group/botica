package es.us.isa.botica.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.bot.BotMountConfiguration;
import es.us.isa.botica.configuration.bot.BotPublishConfiguration;
import es.us.isa.botica.configuration.bot.BotSubscribeConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.InvalidBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.InvalidBrokerConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Simple configuration validation tests")
class SimpleConfigurationValidationTest {
  @Nested
  @DisplayName("for BotMountConfiguration")
  class BotMountConfigurationTest {
    @Test
    @DisplayName("validate should report error for missing source")
    void validate_missingSource_reportsError() {
      // Arrange
      BotMountConfiguration config = new BotMountConfiguration();
      config.setTarget("/valid/target");
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("source")).hasSize(1);
    }

    @Test
    @DisplayName("validate should report error for missing target")
    void validate_missingTarget_reportsError() {
      // Arrange
      BotMountConfiguration config = new BotMountConfiguration();
      config.setSource("/valid/source");
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("target")).hasSize(1);
    }
  }

  @Nested
  @DisplayName("for BotPublishConfiguration")
  class BotPublishConfigurationTest {
    @Test
    @DisplayName("validate should be clean if both key and order are set")
    void validate_bothSet_isClean() {
      // Arrange
      BotPublishConfiguration config = new BotPublishConfiguration();
      config.setKey("key");
      config.setOrder("order");
      ValidationReport report = new ValidationReport();

      // Act & Assert
      config.validate(report);
      assertThat(report.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("validate should be clean if neither key nor order are set")
    void validate_neitherSet_isClean() {
      // Arrange
      BotPublishConfiguration config = new BotPublishConfiguration();
      ValidationReport report = new ValidationReport();

      // Act & Assert
      config.validate(report);
      assertThat(report.hasErrors()).isFalse();
    }

    @Test
    @DisplayName("validate should report error if only key is set")
    void validate_onlyKeySet_reportsError() {
      // Arrange
      BotPublishConfiguration config = new BotPublishConfiguration();
      config.setKey("key");
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("order")).hasSize(1);
      assertThat(report.getResults("key")).isNullOrEmpty();
    }

    @Test
    @DisplayName("validate should report error if only order is set")
    void validate_onlyOrderSet_reportsError() {
      // Arrange
      BotPublishConfiguration config = new BotPublishConfiguration();
      config.setOrder("order");
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("key")).hasSize(1);
      assertThat(report.getResults("order")).isNullOrEmpty();
    }
  }

  @Nested
  @DisplayName("for BotSubscribeConfiguration")
  class BotSubscribeConfigurationTest {
    @Test
    @DisplayName("validate should report error for missing key")
    void validate_missingKey_reportsError() {
      // Arrange
      BotSubscribeConfiguration config = new BotSubscribeConfiguration();
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("key")).hasSize(1);
    }
  }

  @Nested
  @DisplayName("for ProactiveBotLifecycleConfiguration")
  class ProactiveBotLifecycleConfigurationTest {
    @Test
    @DisplayName("validate should report error for negative initialDelay")
    void validate_negativeInitialDelay_reportsError() {
      // Arrange
      ProactiveBotLifecycleConfiguration config = new ProactiveBotLifecycleConfiguration();
      config.setInitialDelay(-1);
      config.setPeriod(10);
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("initialDelay")).hasSize(1);
    }

    @Test
    @DisplayName("validate should report error for zero period")
    void validate_zeroPeriod_reportsError() {
      // Arrange
      ProactiveBotLifecycleConfiguration config = new ProactiveBotLifecycleConfiguration();
      config.setInitialDelay(0);
      config.setPeriod(0);
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("period")).hasSize(1);
    }
  }

  @Nested
  @DisplayName("for Invalid Configurations")
  class InvalidConfigurationTest {
    @Test
    @DisplayName("InvalidBrokerConfiguration validate should report error with supported types")
    void invalidBroker_validate_reportsError() {
      // Arrange
      InvalidBrokerConfiguration config = new InvalidBrokerConfiguration();
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("type"))
          .hasSize(1)
          .first()
          .extracting("message")
          .asString()
          .contains("rabbitmq");
    }

    @Test
    @DisplayName("InvalidBotLifecycleConfiguration validate should report error with supported types")
    void invalidLifecycle_validate_reportsError() {
      // Arrange
      InvalidBotLifecycleConfiguration config = new InvalidBotLifecycleConfiguration();
      ValidationReport report = new ValidationReport();

      // Act
      config.validate(report);

      // Assert
      assertThat(report.hasErrors()).isTrue();
      assertThat(report.getResults("type"))
          .hasSize(1)
          .first()
          .extracting("message")
          .asString()
          .contains("proactive", "reactive", "unmanaged");
    }
  }
}
