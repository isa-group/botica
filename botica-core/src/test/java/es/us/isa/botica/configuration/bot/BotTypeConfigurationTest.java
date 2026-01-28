package es.us.isa.botica.configuration.bot;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.util.configuration.validate.ValidationReport;
import es.us.isa.botica.util.configuration.validate.ValidationResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BotTypeConfigurationTest {
  // --- getReplicas() Logic Tests ---

  @Test
  @DisplayName("getReplicas should return explicit value when set")
  void getReplicas_explicitValue_returnsValue() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setReplicas(5);

    // Act & Assert
    assertThat(config.getReplicas()).isEqualTo(5);
  }

  @Test
  @DisplayName("getReplicas should return 0 when replicas is null but instances exist")
  void getReplicas_nullWithInstances_returnsZero() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setDeclaredInstances(Map.of("bot-a", new BotInstanceConfiguration()));
    // replicas field is null by default

    // Act & Assert
    assertThat(config.getReplicas()).isZero();
  }

  @Test
  @DisplayName(
      "getReplicas should return 1 as default when replicas is null and no instances exist")
  void getReplicas_nullWithoutInstances_returnsOne() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setDeclaredInstances(Collections.emptyMap());
    // replicas field is null by default

    // Act & Assert
    assertThat(config.getReplicas()).isEqualTo(1);
  }

  // --- buildInstances() Logic Tests ---

  @Test
  @DisplayName("buildInstances should return declared instances plus replicas")
  void buildInstances_withDeclaredAndReplicas_returnsCombinedList() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("my-bot");
    BotInstanceConfiguration declaredInstance = new BotInstanceConfiguration();
    declaredInstance.setId("my-bot-predefined");
    config.setDeclaredInstances(Map.of("my-bot-predefined", declaredInstance));
    config.setReplicas(2);

    // Act
    List<BotInstanceConfiguration> instances = config.buildInstances();

    // Assert
    assertThat(instances).hasSize(3);
    assertThat(instances)
        .extracting(BotInstanceConfiguration::getId)
        .containsExactlyInAnyOrder("my-bot-predefined", "my-bot-1", "my-bot-2");

    // Verify back-reference and ID are set correctly for generated replicas
    BotInstanceConfiguration replica1 =
        instances.stream().filter(i -> i.getId().equals("my-bot-1")).findFirst().orElse(null);
    assertThat(replica1).isNotNull();
    assertThat(replica1.getTypeConfiguration()).isEqualTo(config);
  }

  @Test
  @DisplayName("buildInstances should only return replicas when no instances are declared")
  void buildInstances_onlyReplicas_returnsGeneratedList() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("my-bot");
    config.setReplicas(3);

    // Act
    List<BotInstanceConfiguration> instances = config.buildInstances();

    // Assert
    assertThat(instances).hasSize(3);
    assertThat(instances)
        .extracting(BotInstanceConfiguration::getId)
        .containsExactly("my-bot-1", "my-bot-2", "my-bot-3");
  }

  // --- Validation Tests ---

  @Test
  @DisplayName("validate should report error for missing both image and build")
  void validate_missingImageAndBuild_reportsError() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("image/build"))
        .hasSize(1)
        .first()
        .extracting(ValidationResult::getMessage)
        .isEqualTo("must specify either 'image' or 'build'");
  }

  @Test
  @DisplayName("validate should report error for specifying both image and build")
  void validate_bothImageAndBuild_reportsError() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    config.setImage("valid-image");
    config.setBuild("./valid-build-path");
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("image/build"))
        .hasSize(1)
        .first()
        .extracting(ValidationResult::getMessage)
        .isEqualTo("cannot specify both 'image' and 'build'; please choose one");
  }

  @Test
  @DisplayName("validate should be valid with only image specified")
  void validate_onlyImage_producesCleanReport() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    config.setImage("valid-image");
    config.setReplicas(1);
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.hasWarnings()).isFalse();
  }

  @Test
  @DisplayName("validate should be valid with only build specified")
  void validate_onlyBuild_producesCleanReport() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    config.setBuild("./valid-build-path");
    config.setReplicas(1);
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.hasWarnings()).isFalse();
  }

  @Test
  @DisplayName("validate should report error for negative replicas")
  void validate_negativeReplicas_reportsError() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    config.setImage("valid-image");
    config.setReplicas(-1);
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("replicas"))
        .hasSize(1)
        .first()
        .extracting(ValidationResult::getMessage)
        .isEqualTo("negative number of replicas");
  }

  @Test
  @DisplayName("validate should produce a clean report for a valid configuration")
  void validate_validConfig_producesCleanReport() {
    // Arrange
    BotTypeConfiguration config = new BotTypeConfiguration();
    config.setId("valid-id");
    config.setImage("valid-image");
    config.setReplicas(1);
    ValidationReport report = new ValidationReport();

    // Act
    config.validate(report);

    // Assert
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.hasWarnings()).isFalse();
  }
}
