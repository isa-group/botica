package es.us.isa.botica.director.initialize;

import es.us.isa.botica.util.annotation.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectInitializer {
  private static final Logger log = LoggerFactory.getLogger(ProjectInitializer.class);
  private static final String GITHUB_BASE_URL = "https://github.com/isa-group/";
  private static final String GITHUB_ARCHIVE_SUFFIX = "/archive/refs/heads/main.zip";

  public void initialize(String templateAlias, String directoryName)
      throws ProjectInitializationException {
    Path targetDir = Paths.get(directoryName);

    if (Files.exists(targetDir)) {
      throw new ProjectInitializationException(
          String.format(
              "Directory '%s' already exists. Please choose a different name or remove the existing directory.",
              directoryName));
    }

    String repoName = resolveRepoName(templateAlias);
    URL downloadUrl;
    try {
      downloadUrl = new URL(GITHUB_BASE_URL + repoName + GITHUB_ARCHIVE_SUFFIX);
    } catch (IOException e) {
      throw new ProjectInitializationException("Failed to construct a valid download URL.", e);
    }

    log.info("Downloading template '{}' from {}...", repoName, downloadUrl);
    try (InputStream in = downloadUrl.openStream()) {
      unzipFromStream(in, targetDir);
      log.info("Successfully initialized bot project in '{}'.", directoryName);
    } catch (IOException e) {
      throw new ProjectInitializationException(
          String.format(
              "Failed to download or unzip template from %s: %s", downloadUrl, e.getMessage()),
          e);
    }
  }

  @VisibleForTesting
  void unzipFromStream(InputStream inputStream, Path targetDir) throws IOException {
    targetDir = targetDir.toAbsolutePath().normalize();
    Files.createDirectories(targetDir);

    try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
      Path rootDirPrefix = null;
      ZipEntry entry;

      while ((entry = zipIn.getNextEntry()) != null) {
        // On the first iteration, establish the root directory from the zip.
        // We assume the first entry defines the root path (e.g. "botica-seed-java-main/")
        if (rootDirPrefix == null) {
          rootDirPrefix = Path.of(entry.getName());
          zipIn.closeEntry();
          continue;
        }

        // Remove the root (e.g. "botica-seed-java-main/") from the path.
        Path relativePath = rootDirPrefix.relativize(Path.of(entry.getName()));
        Path resolvedPath = targetDir.resolve(relativePath).normalize();

        if (!resolvedPath.startsWith(targetDir)) {
          throw new IOException(
              "Zip entry is outside of the target directory: " + entry.getName()
          );
        }

        if (entry.isDirectory()) {
          Files.createDirectories(resolvedPath);
        } else {
          Files.createDirectories(resolvedPath.getParent());
          Files.copy(zipIn, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zipIn.closeEntry();
      }

      if (rootDirPrefix == null) {
        throw new IOException("Downloaded ZIP file is empty or invalid.");
      }
    }
  }

  private String resolveRepoName(String alias) throws ProjectInitializationException {
    switch (alias.toLowerCase()) {
      case "java":
        return "botica-seed-java";
      case "js":
      case "javascript":
        return "botica-seed-node";
      case "ts":
      case "typescript":
        return "botica-seed-node-ts";
      default:
        throw new ProjectInitializationException(
            String.format(
                "Unknown template alias: '%s'. Available templates: java, js, javascript, ts, typescript.",
                alias));
    }
  }
}
