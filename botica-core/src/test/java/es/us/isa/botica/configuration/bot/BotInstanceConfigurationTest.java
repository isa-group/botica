package es.us.isa.botica.configuration.bot;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationReport;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BotInstanceConfigurationTest {
  private BotTypeConfiguration parentType;
  private BotInstanceConfiguration instance;

  @BeforeEach
  void setUp() {
    // Parent BotTypeConfiguration to test inheritance
    parentType = new BotTypeConfiguration();
    parentType.setPorts(List.of("8080:80"));
    parentType.setEnvironment(List.of("PARENT_VAR=parent_value"));
    parentType.setLifecycleConfiguration(new ReactiveBotLifecycleConfiguration());

    instance = new BotInstanceConfiguration();
    instance.setTypeConfiguration(parentType);
  }

  // --- Getter Logic Tests ---

  @Test
  @DisplayName("getPorts should return parent's ports if instance has none")
  void getPorts_instanceHasNone_returnsParentPorts() {
    // Act & Assert
    assertThat(instance.getPorts()).containsExactly("8080:80");
  }

  @Test
  @DisplayName("getPorts should return only instance's ports if parent has none")
  void getPorts_parentHasNone_returnsInstancePorts() {
    // Arrange
    parentType.setPorts(Collections.emptyList());
    instance.setOwnPorts(List.of("9090:90"));

    // Act & Assert
    assertThat(instance.getPorts()).containsExactly("9090:90");
  }

  @Test
  @DisplayName("getPorts should return a combined list of parent and instance ports")
  void getPorts_bothHavePorts_returnsCombinedList() {
    // Arrange
    instance.setOwnPorts(List.of("9090:90"));

    // Act & Assert
    assertThat(instance.getPorts()).containsExactly("8080:80", "9090:90");
  }

  @Test
  @DisplayName("getEnvironment should return a combined list of parent and instance environment variables")
  void getEnvironment_bothHaveEnv_returnsCombinedList() {
    // Arrange
    instance.setOwnEnvironment(List.of("CHILD_VAR=child_value"));

    // Act & Assert
    assertThat(instance.getEnvironment())
        .containsExactly("PARENT_VAR=parent_value", "CHILD_VAR=child_value");
  }

  @Test
  @DisplayName("getLifecycleConfiguration should return parent's config if instance has none")
  void getLifecycleConfiguration_instanceHasNone_returnsParentConfig() {
    // Act & Assert
    assertThat(instance.getLifecycleConfiguration()).isInstanceOf(ReactiveBotLifecycleConfiguration.class);
  }

  @Test
  @DisplayName("getLifecycleConfiguration should return instance's config when it is defined")
  void getLifecycleConfiguration_instanceHasOwn_returnsInstanceConfig() {
    // Arrange
    instance.setOwnLifecycleConfiguration(new ProactiveBotLifecycleConfiguration());

    // Act & Assert
    assertThat(instance.getLifecycleConfiguration()).isInstanceOf(ProactiveBotLifecycleConfiguration.class);
  }

  // --- Validation Tests ---

  @Test
  @DisplayName("validate should report error for missing ID")
  void validate_missingId_reportsError() {
    // Arrange
    ValidationReport report = new ValidationReport();

    // Act
    instance.validate(report); // ID is null by default

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("id"))
        .hasSize(1)
        .first()
        .extracting("message")
        .isEqualTo("missing or empty id");
  }

  @Test
  @DisplayName("validate should produce a clean report for a valid instance")
  void validate_validInstance_producesCleanReport() {
    // Arrange
    instance.setId("valid-id");
    ValidationReport report = new ValidationReport();

    // Act
    instance.validate(report);

    // Assert
    assertThat(report.hasErrors()).isFalse();
    assertThat(report.hasWarnings()).isFalse();
  }
}
