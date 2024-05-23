package es.us.isa.botica.util.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

public class JacksonConfigurationFileLoaderTest {
  private final ConfigurationFileLoader configurationFileLoader =
      new JacksonConfigurationFileLoader();

  @Test
  void testLoadYamlFile() {
    File file = getResource("dummy-configuration-file.yml");

    DummyConfigurationFile dummyConfigurationFile =
        configurationFileLoader.load(file, DummyConfigurationFile.class);

    assertThatContentsAreReadCorrectly(dummyConfigurationFile);
  }

  @Test
  void testLoadJsonFile() {
    File file = getResource("dummy-configuration-file.json");

    DummyConfigurationFile dummyConfigurationFile =
        configurationFileLoader.load(file, DummyConfigurationFile.class);

    assertThatContentsAreReadCorrectly(dummyConfigurationFile);
  }

  static void assertThatContentsAreReadCorrectly(DummyConfigurationFile dummyConfigurationFile) {
    assertThat(dummyConfigurationFile.string).isEqualTo("value");
    assertThat(dummyConfigurationFile.object).isNotNull();
    assertThat(dummyConfigurationFile.object.integer).isEqualTo(1);
  }

  private File getResource(String name) {
    return new File(getClass().getClassLoader().getResource(name).getFile());
  }
}
