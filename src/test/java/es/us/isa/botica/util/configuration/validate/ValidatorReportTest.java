package es.us.isa.botica.util.configuration.validate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ValidatorReportTest {
  @Test
  void testToString() {
    ValidationReport validationReport = buildReport();
    String render = validationReport.toString();

    assertThat(render)
        .isEqualTo(
            "- foo: foo error message!\n"
                + "- bar: bar message...\n"
                + "- baz:\n"
                + "  - foo: error in foo property");
  }

  @Test
  void testToStringEmpty() {
    ValidationReport report = new ValidationReport();
    String render = report.toString();

    assertThat(render).isEqualTo("");
  }

  @Test
  void testToStringChildWithNoResults() {
    ValidationReport parent = new ValidationReport();
    parent.addError("foo", "foo %s message!", "error");
    parent.addChild("baz", new ValidationReport());
    String render = parent.toString();

    assertThat(render).isEqualTo("- foo: foo error message!");
  }

  @Test
  void testRenderErrorFilter() {
    ValidationReport validationReport = buildReport();
    String render = validationReport.render(ValidationResult.Type.ERROR);

    assertThat(render)
        .isEqualTo("- foo: foo error message!\n" + "- baz:\n" + "  - foo: error in foo property");
  }

  @Test
  void testRenderWarningFilter() {
    ValidationReport validationReport = buildReport();
    String render = validationReport.render(ValidationResult.Type.WARNING);

    assertThat(render).isEqualTo("- bar: bar message...");
  }

  private static ValidationReport buildReport() {
    ValidationReport parent = new ValidationReport();
    parent.addError("foo", "foo %s message!", "error");
    parent.addWarning("bar", "bar message...");

    ValidationReport child = new ValidationReport();
    child.addError("foo", "error in foo property");
    parent.addChild("baz", child);

    return parent;
  }
}
