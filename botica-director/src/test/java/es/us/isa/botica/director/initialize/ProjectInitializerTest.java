package es.us.isa.botica.director.initialize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectInitializerTest {
  @TempDir Path tempDir;

  private ProjectInitializer initializer;

  @BeforeEach
  void setUp() {
    initializer = spy(new ProjectInitializer());
  }

  private InputStream createMockZipStream(String repoName) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
      String rootDir = repoName + "-main/";
      zipOut.putNextEntry(new ZipEntry(rootDir));
      zipOut.closeEntry();

      ZipEntry fileEntry = new ZipEntry(rootDir + "file.txt");
      zipOut.putNextEntry(fileEntry);
      zipOut.write("hello".getBytes(StandardCharsets.UTF_8));
      zipOut.closeEntry();

      ZipEntry buildShEntry = new ZipEntry(rootDir + "build.sh");
      zipOut.putNextEntry(buildShEntry);
      zipOut.write("#!/bin/bash\necho build".getBytes(StandardCharsets.UTF_8));
      zipOut.closeEntry();

      ZipEntry dirEntry = new ZipEntry(rootDir + "src/");
      zipOut.putNextEntry(dirEntry);
      zipOut.closeEntry();

      ZipEntry nestedFileEntry = new ZipEntry(rootDir + "src/main.js");
      zipOut.putNextEntry(nestedFileEntry);
      zipOut.write("content".getBytes(StandardCharsets.UTF_8));
      zipOut.closeEntry();
    }
    return new ByteArrayInputStream(out.toByteArray());
  }

  @Test
  @DisplayName("unzipFromStream should correctly create project directory and files")
  void unzipFromStream_shouldCreateProject() throws Exception {
    Path projectPath = tempDir.resolve("my-java-bot");
    try (InputStream mockZip = createMockZipStream("botica-seed-java")) {
      initializer.unzipFromStream(mockZip, projectPath);
    }

    assertThat(projectPath).exists().isDirectory();
    assertThat(projectPath.resolve("file.txt")).exists().hasContent("hello");
    assertThat(projectPath.resolve("src/main.js")).exists().hasContent("content");
  }

  @Test
  @DisplayName("initialize with invalid alias should throw exception")
  void initialize_withInvalidAlias_shouldThrow() {
    assertThatThrownBy(() -> initializer.initialize("python", "my-bot"))
        .isInstanceOf(ProjectInitializationException.class)
        .hasMessageContaining("Unknown template alias: 'python'");
  }

  @Test
  @DisplayName("initialize when directory already exists should throw exception")
  void initialize_whenDirectoryExists_shouldThrow() throws IOException {
    Path existingDir = tempDir.resolve("existing-bot");
    Files.createDirectory(existingDir);

    assertThatThrownBy(() -> initializer.initialize("java", existingDir.toString()))
        .isInstanceOf(ProjectInitializationException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  @DisplayName("initialize when download fails should throw exception")
  void initialize_whenDownloadFails_shouldThrow() throws IOException {
    doThrow(new IOException("No Internet connection"))
        .when(initializer)
        .unzipFromStream(any(), any());

    assertThatThrownBy(() -> initializer.initialize("java", "my-bot-failing-download"))
        .isInstanceOf(ProjectInitializationException.class)
        .hasMessageContaining(
            "Failed to download or unzip template from https://github.com/isa-group/botica-seed-java/archive/refs/heads/main.zip")
        .hasCauseInstanceOf(IOException.class);
  }
}
