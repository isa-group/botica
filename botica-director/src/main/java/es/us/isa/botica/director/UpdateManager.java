package es.us.isa.botica.director;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateManager {
  private static final Logger log = LoggerFactory.getLogger(UpdateManager.class);
  private static final String GITHUB_API_URL =
      "https://api.github.com/repos/isa-group/botica/releases/latest";
  private static final String RELEASE_ASSET_NAME = "botica-director.jar";
  private static final String UPDATE_JAR_NAME = "botica-director.jar.update";
  private static final int UPDATE_EXIT_CODE = 99;

  public void checkForUpdates() {
    try {
      String currentVersion = getCurrentVersion();
      if (currentVersion == null) {
        log.warn(
            "Could not determine current version. Skipping update check."
                + " Ensure the project is built with Maven and the"
                + " maven-jar-plugin is configured.");
        return;
      }

      VersionInfo latestVersionInfo = fetchLatestVersionInfo();
      if (isNewer(currentVersion, latestVersionInfo.version)) {
        log.warn("--- NEW VERSION AVAILABLE ---");
        log.warn("A new version of Botica Director is available: {}", latestVersionInfo.version);
        log.warn("You are currently on version: {}", currentVersion);
        log.warn("");
        log.warn(
            "Updating could introduce breaking changes. "
                + "Ensure your bots are compatible before proceeding.");
        log.warn("You can always re-download version {} if you encounter issues.", currentVersion);

        if (promptForUpdate()) {
          performUpdate(latestVersionInfo.downloadUrl);
        }
      } else {
        log.info("You are running the latest version of Botica Director: {}", currentVersion);
      }
    } catch (IOException e) {
      log.error("Update check failed due to a network error: {}", e.getMessage());
    } catch (Exception e) {
      log.error("An unexpected error occurred during the update check.", e);
    }
  }

  private String getCurrentVersion() {
    return DirectorBootstrap.class.getPackage().getImplementationVersion();
  }

  private VersionInfo fetchLatestVersionInfo() throws IOException {
    URL url = new URL(GITHUB_API_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

    if (conn.getResponseCode() != 200) {
      throw new IOException("Failed: HTTP error code: " + conn.getResponseCode());
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
      String jsonText = br.lines().collect(Collectors.joining());
      JSONObject release = new JSONObject(jsonText);
      String tagName = release.getString("tag_name");

      JSONArray assets = release.getJSONArray("assets");
      for (int i = 0; i < assets.length(); i++) {
        JSONObject asset = assets.getJSONObject(i);
        if (RELEASE_ASSET_NAME.equals(asset.getString("name"))) {
          return new VersionInfo(tagName, asset.getString("browser_download_url"));
        }
      }
    }
    throw new IOException("Could not find director JAR asset in the latest release.");
  }

  private boolean isNewer(String currentVersion, String latestVersionTag) {
    String latestVersion =
        latestVersionTag.startsWith("v") ? latestVersionTag.substring(1) : latestVersionTag;
    if (currentVersion.equals(latestVersion)) {
      return false;
    }

    String[] currentParts = currentVersion.split("\\.");
    String[] latestParts = latestVersion.split("\\.");

    try {
      int length = Math.max(currentParts.length, latestParts.length);
      for (int i = 0; i < length; i++) {
        int current = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
        int latest = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
        if (latest > current) {
          return true;
        }
        if (current > latest) {
          return false;
        }
      }
    } catch (NumberFormatException e) {
      return true; // Not a number but not identical. Safer to assume latest is newer.
    }
    return false;
  }

  private boolean promptForUpdate() {
    System.out.print("Do you want to download and install it now? (y/N): ");
    try (Scanner scanner = new Scanner(System.in)) {
      String input = scanner.nextLine().trim().toLowerCase();
      return "y".equals(input);
    }
  }

  private void performUpdate(String downloadUrl) {
    try {
      Path currentJarPath =
          Paths.get(
              UpdateManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      if (!currentJarPath.toString().endsWith(".jar")) {
        log.error("Cannot perform auto-update: Application is not running from a JAR file.");
        return;
      }

      Path updateJarPath = currentJarPath.getParent().resolve(UPDATE_JAR_NAME);
      log.info("Downloading new version from {}...", downloadUrl);
      downloadFileWithProgress(downloadUrl, updateJarPath);
      log.info("Download complete.");
      log.info("Installing update...");

      boolean usingWrapper = "true".equals(System.getenv("BOTICA_WRAPPER_ACTIVE"));

      if (usingWrapper) {
        System.exit(UPDATE_EXIT_CODE);
      } else {
        updateWithDetachedScript(currentJarPath, updateJarPath);
        System.exit(0);
      }

    } catch (IOException | URISyntaxException e) {
      log.error("Failed to perform update: {}", e.getMessage(), e);
    }
  }

  /** Downloads a file from a URL to a target path, displaying progress in the console. */
  private void downloadFileWithProgress(String downloadUrl, Path targetPath) throws IOException {
    URL url = new URL(downloadUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();

    long totalBytes = connection.getContentLengthLong();
    long downloadedBytes = 0;
    int bufferSize = 4096;

    try (InputStream in = connection.getInputStream();
        OutputStream out = Files.newOutputStream(targetPath)) {
      byte[] buffer = new byte[bufferSize];
      int bytesRead;
      long lastPrintedTime = System.currentTimeMillis();

      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        downloadedBytes += bytesRead;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPrintedTime > 200 || downloadedBytes == totalBytes) {
          printDownloadProgress(downloadedBytes, totalBytes);
          lastPrintedTime = currentTime;
        }
      }
      System.out.println();
    }
  }

  /** Prints the download progress to the console, overwriting the previous line. */
  private void printDownloadProgress(long downloadedBytes, long totalBytes) {
    if (totalBytes <= 0) {
      System.out.printf("\rDownloading: %s", formatSize(downloadedBytes));
    } else {
      int percentage = (int) (100 * downloadedBytes / totalBytes);
      System.out.printf(
          "\33[2K\rDownloading: %d%% (%s / %s)",
          percentage, formatSize(downloadedBytes), formatSize(totalBytes));
    }
    System.out.flush();
  }

  /** Formats a byte count into a human-readable string (e.g., 1.5 KB, 2.3 MB). */
  private String formatSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String unit = "KMG".charAt(exp - 1) + "";
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
  }

  /** Creates a self-deleting, detached script for users not running via the wrapper. */
  private void updateWithDetachedScript(Path currentJarPath, Path updateJarPath)
      throws IOException {
    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    File scriptFile;
    String scriptContent;

    if (isWindows) {
      scriptFile = File.createTempFile("director-update", ".bat");
      scriptContent =
          "@echo off\r\n"
              + "timeout /t 2 /nobreak > nul\r\n"
              + "del \""
              + currentJarPath
              + "\"\r\n"
              + "rename \""
              + updateJarPath
              + "\" \""
              + currentJarPath.getFileName()
              + "\"\r\n"
              + "(goto) 2>nul & del \"%~f0\""; // Self-delete
    } else {
      scriptFile = File.createTempFile("director-update", ".sh");
      scriptContent =
          "#!/bin/sh\n"
              + "sleep 2\n"
              + "rm \""
              + currentJarPath
              + "\"\n"
              + "mv \""
              + updateJarPath
              + "\" \""
              + currentJarPath
              + "\"\n"
              + "rm -- \"$0\""; // Self-delete
      scriptFile.setExecutable(true, true);
    }

    try (PrintWriter writer = new PrintWriter(scriptFile)) {
      writer.print(scriptContent);
    }

    new ProcessBuilder(scriptFile.getAbsolutePath()).start();
  }

  private static class VersionInfo {
    final String version;
    final String downloadUrl;

    VersionInfo(String version, String downloadUrl) {
      this.version = version;
      this.downloadUrl = downloadUrl;
    }
  }
}
