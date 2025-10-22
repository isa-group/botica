package es.us.isa.botica.util.configuration.validate;

import static org.assertj.core.api.Assertions.assertThat;

import es.us.isa.botica.util.configuration.DummyConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationResult.Type;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationReportTest {
  @Test
  @DisplayName("addError and addWarning should correctly add results")
  void addResult_addsErrorAndWarningCorrectly() {
    // Arrange
    ValidationReport report = new ValidationReport();

    // Act
    report.addError("prop1", "error message %s", "A");
    report.addWarning("prop2", "warning message");

    // Assert
    assertThat(report.getResults("prop1"))
        .hasSize(1)
        .first()
        .satisfies(
            r -> {
              assertThat(r.getType()).isEqualTo(Type.ERROR);
              assertThat(r.getMessage()).isEqualTo("error message A");
            });

    assertThat(report.getResults("prop2"))
        .hasSize(1)
        .first()
        .satisfies(
            r -> {
              assertThat(r.getType()).isEqualTo(Type.WARNING);
              assertThat(r.getMessage()).isEqualTo("warning message");
            });
  }

  @Test
  @DisplayName("hasErrors should return true if direct or child reports have errors")
  void hasErrors_returnsTrue_ifDirectOrChildErrorsExist() {
    // Arrange
    ValidationReport parentWithDirectError = new ValidationReport();
    parentWithDirectError.addError("test", "error");

    ValidationReport parentWithChildError = new ValidationReport();
    ValidationReport childWithError = new ValidationReport();
    childWithError.addError("childTest", "error");
    parentWithChildError.addChild("child", childWithError);

    ValidationReport cleanReport = new ValidationReport();
    cleanReport.addWarning("test", "warning");

    // Act & Assert
    assertThat(parentWithDirectError.hasErrors()).isTrue();
    assertThat(parentWithChildError.hasErrors()).isTrue();
    assertThat(cleanReport.hasErrors()).isFalse();
  }

  @Test
  @DisplayName("countErrors should correctly count all direct and child errors")
  void countErrors_countsAllDirectAndChildErrors() {
    // Arrange
    ValidationReport report = buildComprehensiveReport();

    // Act
    long errorCount = report.countErrors();

    // Assert
    assertThat(errorCount).isEqualTo(2); // One in parent, one in child
  }

  @Test
  @DisplayName("countWarnings should correctly count all direct and child warnings")
  void countWarnings_countsAllDirectAndChildWarnings() {
    // Arrange
    ValidationReport report = buildComprehensiveReport();

    // Act
    long warningCount = report.countWarnings();

    // Assert
    assertThat(warningCount).isEqualTo(2); // One in parent, one in child
  }

  @Test
  @DisplayName("registerChild should add a validated child report")
  void registerChild_withValidatable_addsValidatedChildReport() {
    // Arrange
    ValidationReport report = new ValidationReport();
    DummyConfiguration.InnerObject childConfig = new DummyConfiguration.InnerObject();
    childConfig.integer = -5; // This causes a warning

    // Act
    report.registerChild("inner", childConfig);

    // Assert
    assertThat(report.getChildren()).containsKey("inner");
    ValidationReport childReport = report.getChild("inner");
    assertThat(childReport).isNotNull();
    assertThat(childReport.hasWarnings()).isTrue();
    assertThat(childReport.getResults("integer")).hasSize(1);
  }

  @Test
  @DisplayName("registerChild with List should create indexed child reports")
  void registerChild_withList_createsIndexedChildReports() {
    // Arrange
    ValidationReport report = new ValidationReport();
    DummyConfiguration.InnerObject child1 = new DummyConfiguration.InnerObject();
    child1.integer = -1; // Warning
    DummyConfiguration.InnerObject child2 = new DummyConfiguration.InnerObject();
    child2.integer = 1; // Valid

    // Act
    report.registerChild("items", List.of(child1, child2));

    // Assert
    assertThat(report.getChildren()).containsKeys("items[0]", "items[1]");
    assertThat(report.getChild("items[0]").hasWarnings()).isTrue();
    assertThat(report.getChild("items[1]").hasWarnings()).isFalse();
  }

  @Test
  @DisplayName("registerChild with null object should add a 'missing property' error")
  void registerChild_withNullObject_addsMissingPropertyError() {
    // Arrange
    ValidationReport report = new ValidationReport();

    // Act
    report.registerChild("myProp", (Validatable) null);

    // Assert
    assertThat(report.hasErrors()).isTrue();
    assertThat(report.getResults("myProp"))
        .hasSize(1)
        .first()
        .extracting(ValidationResult::getMessage)
        .isEqualTo("missing property");
  }

  @Test
  @DisplayName("render should filter and display only ERROR messages")
  void render_errorFilter_displaysOnlyErrors() {
    // Arrange
    ValidationReport validationReport = buildComprehensiveReport();
    String expectedRender =
        """
        - foo: foo error message!
        - baz:
          - foo: error in foo property""";

    // Act
    String render = validationReport.render(Type.ERROR);

    // Assert
    assertThat(render).isEqualToNormalizingNewlines(expectedRender);
  }

  @Test
  @DisplayName("toString should render a complete report with results and nested children")
  void toString_withResultsAndChildren_rendersFullReport() {
    // Arrange
    ValidationReport validationReport = buildComprehensiveReport();
    String expectedRender =
        """
        - foo: foo error message!
        - bar: bar warning message...
        - baz:
          - foo: error in foo property
          - qux: qux warning...""";

    // Act
    String render = validationReport.toString();

    // Assert
    assertThat(render).isEqualToNormalizingNewlines(expectedRender);
  }

  // Helper method to build a consistent report for testing
  private static ValidationReport buildComprehensiveReport() {
    ValidationReport parent = new ValidationReport();
    parent.addError("foo", "foo %s message!", "error");
    parent.addWarning("bar", "bar warning message...");

    ValidationReport child = new ValidationReport();
    child.addError("foo", "error in foo property");
    child.addWarning("qux", "qux warning...");
    parent.addChild("baz", child);

    return parent;
  }
}
