package es.us.isa.botica.util.configuration.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import es.us.isa.botica.util.configuration.ConfigurationFileLoader;
import es.us.isa.botica.util.configuration.ConfigurationLoadingException;
import es.us.isa.botica.util.configuration.DummyConfiguration;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JacksonConfigurationFileLoaderTest {
  private static final String TEST_PROPERTY = "botica.test.property";

  private ConfigurationFileLoader configurationFileLoader;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    configurationFileLoader = new JacksonConfigurationFileLoader();
    System.clearProperty(TEST_PROPERTY);
  }

  @AfterEach
  void tearDown() {
    System.clearProperty(TEST_PROPERTY);
  }

  @Test
  @DisplayName("load should correctly deserialize a valid YAML file")
  void load_validYamlFile_deserializesCorrectly() throws URISyntaxException {
    // Arrange
    File file = getResourceFile("dummy-configuration-file.yml");

    // Act
    DummyConfiguration result = configurationFileLoader.load(file, DummyConfiguration.class);

    // Assert
    assertThatContentsAreReadCorrectly(result);
  }

  @Test
  @DisplayName("load should correctly deserialize a valid JSON file")
  void load_validJsonFile_deserializesCorrectly() throws URISyntaxException {
    // Arrange
    File file = getResourceFile("dummy-configuration-file.json");

    // Act
    DummyConfiguration result = configurationFileLoader.load(file, DummyConfiguration.class);

    // Assert
    assertThatContentsAreReadCorrectly(result);
  }

  @Test
  @DisplayName("load should resolve placeholders using system properties")
  void load_withPlaceholders_resolvesFromSystemProperties() throws IOException {
    // Arrange
    System.setProperty(TEST_PROPERTY, "resolved-value");
    File configFileWithPlaceholder = tempDir.resolve("placeholder.yml").toFile();
    Files.writeString(configFileWithPlaceholder.toPath(), "string: ${" + TEST_PROPERTY + "}");

    // Act
    DummyConfiguration result =
        configurationFileLoader.load(configFileWithPlaceholder, DummyConfiguration.class);

    // Assert
    assertThat(result.string).isEqualTo("resolved-value");
  }

  @Test
  @DisplayName("write should create a YAML file that can be loaded back")
  void write_createsValidYamlFile() {
    // Arrange
    DummyConfiguration originalConfig = new DummyConfiguration();
    originalConfig.string = "written-value";
    originalConfig.object = new DummyConfiguration.InnerObject();
    originalConfig.object.integer = 999;
    File outputFile = tempDir.resolve("output.yml").toFile();

    // Act
    configurationFileLoader.write(originalConfig, outputFile);

    // Assert
    assertThat(outputFile).exists().isNotEmpty();
    DummyConfiguration loadedConfig =
        configurationFileLoader.load(outputFile, DummyConfiguration.class);
    assertThat(loadedConfig.string).isEqualTo("written-value");
    assertThat(loadedConfig.object.integer).isEqualTo(999);
  }

  @Test
  @DisplayName("load should throw ConfigurationLoadingException for a non-existent file")
  void load_nonExistentFile_throwsConfigurationLoadingException() {
    // Arrange
    File nonExistentFile = tempDir.resolve("not-real.yml").toFile();

    // Act & Assert
    assertThatThrownBy(
            () -> configurationFileLoader.load(nonExistentFile, DummyConfiguration.class))
        .isInstanceOf(ConfigurationLoadingException.class)
        .hasMessageContaining("is not a file or does not exist");
  }

  @Test
  @DisplayName("load should throw ConfigurationLoadingException for a malformed file")
  void load_malformedFile_throwsConfigurationLoadingException() throws IOException {
    // Arrange
    File malformedFile = tempDir.resolve("malformed.yml").toFile();
    Files.writeString(malformedFile.toPath(), "string: value\n- invalid-indent:");

    // Act & Assert
    assertThatThrownBy(() -> configurationFileLoader.load(malformedFile, DummyConfiguration.class))
        .isInstanceOf(ConfigurationLoadingException.class)
        .hasMessageContaining("Unable to read the configuration file");
  }

  private void assertThatContentsAreReadCorrectly(DummyConfiguration config) {
    assertThat(config.string).isEqualTo("value");
    assertThat(config.object).isNotNull();
    assertThat(config.object.integer).isEqualTo(1);
  }

  private File getResourceFile(String name) throws URISyntaxException {
    return new File(getClass().getClassLoader().getResource(name).toURI());
  }
}
