package es.us.isa.botica.util.configuration.validate;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import es.us.isa.botica.util.configuration.DummyConfiguration;
import es.us.isa.botica.util.configuration.validate.ValidationResult.Type;
import org.junit.jupiter.api.Test;

public class ConfigurationValidationTest {
  @Test
  void testValidateSimple() {
    DummyConfiguration config = new DummyConfiguration();
    ValidationReport report = new Validator().validate(config);

    assertThat(report.getResults("string"))
        .hasSize(1)
        .first()
        .satisfies(
            result -> {
              assertThat(result.getType()).isEqualTo(Type.WARNING);
              assertThat(result.getProperty()).isEqualTo("string");
              assertThat(result.getMessage()).isEqualTo("missing or empty string");
            });

    assertThat(report.getResults("object"))
        .hasSize(1)
        .first()
        .satisfies(
            result -> {
              assertThat(result.getType()).isEqualTo(Type.ERROR);
              assertThat(result.getProperty()).isEqualTo("object");
              assertThat(result.getMessage()).isEqualTo("object must be declared");
            });

    assertThat(report.getChild("object")).isNull();
  }

  @Test
  void testValidateChildren() {
    DummyConfiguration config = new DummyConfiguration();
    config.object = new DummyConfiguration.InnerObject();
    config.object.integer = -1;
    ValidationReport report = new Validator().validate(config);

    assertThat(report.getChild("object"))
        .isNotNull()
        .extracting(
            objectReport -> objectReport.getResults("integer"), as(list(ValidationResult.class)))
        .hasSize(1)
        .first()
        .satisfies(
            result -> {
              assertThat(result.getType()).isEqualTo(Type.WARNING);
              assertThat(result.getProperty()).isEqualTo("integer");
              assertThat(result.getMessage()).isEqualTo("negative number");
            });
  }
}
