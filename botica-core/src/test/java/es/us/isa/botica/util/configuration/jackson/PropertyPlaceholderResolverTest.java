package es.us.isa.botica.util.configuration.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PropertyPlaceholderResolverTest {
  private static final String TEST_PROPERTY = "botica.test.property";
  private ObjectMapper objectMapper;

  private static class TargetConfig {
    public String stringValue;
    public Integer intValue;
    public Long longValue;
    public Double doubleValue;
    public Boolean boolValue;
    public NestedConfig nested;
  }

  private static class NestedConfig {
    public String nestedString;
  }

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    System.clearProperty(TEST_PROPERTY);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty(TEST_PROPERTY);
  }

  @Test
  @DisplayName("resolve should substitute a placeholder with a system property")
  void resolve_withSystemProperty_substitutesPlaceholder() throws JsonProcessingException {
    // Arrange
    System.setProperty(TEST_PROPERTY, "value-from-system");
    String content = "{\"stringValue\": \"${" + TEST_PROPERTY + "}\"}";

    // Act
    TargetConfig result =
        PropertyPlaceholderResolver.resolve(objectMapper, content, TargetConfig.class);

    // Assert
    assertThat(result.stringValue).isEqualTo("value-from-system");
  }

  @Test
  @DisplayName("resolve should use default value when system property is not set")
  void resolve_withoutSystemProperty_usesDefaultValue() throws JsonProcessingException {
    // Arrange
    String content = "{\"stringValue\": \"${" + TEST_PROPERTY + ":default-value}\"}";

    // Act
    TargetConfig result =
        PropertyPlaceholderResolver.resolve(objectMapper, content, TargetConfig.class);

    // Assert
    assertThat(result.stringValue).isEqualTo("default-value");
  }

  @Test
  @DisplayName("resolve should prefer system property over default value")
  void resolve_withSystemPropertyAndDefault_prefersSystemProperty() throws JsonProcessingException {
    // Arrange
    System.setProperty(TEST_PROPERTY, "value-from-system");
    String content = "{\"stringValue\": \"${" + TEST_PROPERTY + ":default-value}\"}";

    // Act
    TargetConfig result =
        PropertyPlaceholderResolver.resolve(objectMapper, content, TargetConfig.class);

    // Assert
    assertThat(result.stringValue).isEqualTo("value-from-system");
  }

  @Test
  @DisplayName("resolve should ignore placeholder if property and default are missing")
  void resolve_missingPropertyAndDefault_removesPlaceholder() throws JsonProcessingException {
    // Arrange
    String content = "{\"stringValue\": \"prefix-${" + TEST_PROPERTY + "}-suffix\"}";

    // Act
    TargetConfig result =
        PropertyPlaceholderResolver.resolve(objectMapper, content, TargetConfig.class);

    // Assert
    assertThat(result.stringValue).isEqualTo("prefix-${" + TEST_PROPERTY + "}-suffix");
  }

  @Test
  @DisplayName("resolve should recursively process nested objects and arrays")
  void resolve_withNestedObjectsAndArrays_recursivelyProcesses() throws JsonProcessingException {
    // Arrange
    System.setProperty(TEST_PROPERTY, "nested-value");
    String content =
        """
        {
          "stringValue": "top-level",
          "nested": {
            "nestedString": "value is ${%s}"
          },
          "arrayValue": [ "one", "${%s:default}" ]
        }
        """
            .formatted(TEST_PROPERTY, TEST_PROPERTY);

    // Act
    JsonNode resultNode =
        PropertyPlaceholderResolver.resolve(objectMapper, content, JsonNode.class);

    // Assert
    assertThat(resultNode.path("nested").path("nestedString").asText())
        .isEqualTo("value is nested-value");
    assertThat(resultNode.path("arrayValue").get(1).asText()).isEqualTo("nested-value");
  }

  @Test
  @DisplayName("resolve should correctly infer numeric and boolean types from resolved values")
  void resolve_withTypedValues_infersCorrectJsonTypes() throws JsonProcessingException {
    // Arrange
    System.setProperty("BOTICA_INT", "123");
    System.setProperty("BOTICA_LONG", "1234567890123");
    System.setProperty("BOTICA_DOUBLE", "123.45");
    System.setProperty("BOTICA_BOOL_TRUE", "true");
    System.setProperty("BOTICA_BOOL_FALSE", "False"); // Case-insensitive
    System.setProperty("BOTICA_STRING", "not-a-number");

    String content =
        """
        {
          "intValue": "${BOTICA_INT}",
          "longValue": "${BOTICA_LONG}",
          "doubleValue": "${BOTICA_DOUBLE}",
          "boolValue": "${BOTICA_BOOL_TRUE}",
          "nested": {
            "nestedString": "${BOTICA_STRING}",
            "nestedBool": "${BOTICA_BOOL_FALSE}"
          }
        }
        """;

    // Act
    JsonNode resultNode =
        PropertyPlaceholderResolver.resolve(objectMapper, content, JsonNode.class);

    // Assert
    assertThat(resultNode.path("intValue").isNumber()).isTrue();
    assertThat(resultNode.path("intValue").asInt()).isEqualTo(123);

    assertThat(resultNode.path("longValue").isNumber()).isTrue();
    assertThat(resultNode.path("longValue").asLong()).isEqualTo(1234567890123L);

    assertThat(resultNode.path("doubleValue").isNumber()).isTrue();
    assertThat(resultNode.path("doubleValue").asDouble()).isEqualTo(123.45);

    assertThat(resultNode.path("boolValue").isBoolean()).isTrue();
    assertThat(resultNode.path("boolValue").asBoolean()).isTrue();

    assertThat(resultNode.path("nested").path("nestedString").isTextual()).isTrue();
    assertThat(resultNode.path("nested").path("nestedString").asText()).isEqualTo("not-a-number");

    assertThat(resultNode.path("nested").path("nestedBool").isBoolean()).isTrue();
    assertThat(resultNode.path("nested").path("nestedBool").asBoolean()).isFalse();
  }
}
