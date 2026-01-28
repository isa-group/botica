package es.us.isa.botica.director.deploy.dockerjava;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import es.us.isa.botica.director.Director;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildResult extends ResultCallback.Adapter<BuildResponseItem> {
  private static final Logger log = LoggerFactory.getLogger(BuildResult.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
  private static final Pattern STEP_PATTERN = Pattern.compile("^(Step \\d+/\\d+)\\s?:\\s?(.*)");
  private static final Pattern CACHE_PATTERN = Pattern.compile("(?i).*(using cache|cached).*");

  private final long startTime = System.currentTimeMillis();
  private final CountDownLatch finishLatch = new CountDownLatch(1);
  private final String botId;
  private final int errorLogTailSize;

  private final List<String> tailBuffer = Collections.synchronizedList(new LinkedList<>());
  private final File logFile;
  private BufferedWriter fileWriter;

  private String error = null;
  private String currentStep = "Initializing build";
  private boolean cacheDetected = false;

  public BuildResult(String botId, int errorLogTailSize) {
    this.botId = botId;
    this.errorLogTailSize = errorLogTailSize;
    this.logFile = this.initLogFile();
  }

  private File initLogFile() {
    try {
      String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
      String fileName = String.format("%s-%s.log", botId, timestamp);

      Path buildsDir = Director.DATA_DIRECTORY.resolve("builds");
      Files.createDirectories(buildsDir);
      File file = buildsDir.resolve(fileName).toFile();
      this.fileWriter = new BufferedWriter(new FileWriter(file));
      return file;
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize build log file", e);
    }
  }

  @Override
  @SuppressWarnings("deprecation") // Need to support legacy #getError() as well...
  public void onNext(BuildResponseItem item) {
    String stream = item.getStream();
    if (stream != null && !stream.isBlank()) {
      String cleanLine = stream.trim();
      if (!cleanLine.isEmpty()) {
        writeToLog(cleanLine);
        updateTailBuffer(cleanLine);
        processLine(cleanLine);
      }
    }

    if (item.isErrorIndicated()) {
      if (item.getError() != null) {
        this.error = item.getError();
      } else if (item.getErrorDetail() != null) {
        this.error = item.getErrorDetail().getMessage();
      }
    }
  }

  private void writeToLog(String line) {
    try {
      fileWriter.write(line);
      fileWriter.newLine();
    } catch (IOException ignored) {
    }
  }

  private void updateTailBuffer(String line) {
    synchronized (tailBuffer) {
      tailBuffer.add(line);
      if (tailBuffer.size() > errorLogTailSize) {
        tailBuffer.remove(0);
      }
    }
  }

  private void processLine(String line) {
    Matcher stepMatcher = STEP_PATTERN.matcher(line);
    if (stepMatcher.matches()) {
      this.cacheDetected = false;
      this.currentStep = line;
      log.debug("[{}] {}", botId, line);
      return;
    }

    if (CACHE_PATTERN.matcher(line).matches()) {
      this.cacheDetected = true;
    }
  }

  @Override
  public void onComplete() {
    this.closeWriter();
    this.finishLatch.countDown();
    super.onComplete();
  }

  @Override
  public void onError(Throwable throwable) {
    this.closeWriter();
    this.finishLatch.countDown();
    super.onError(throwable);
  }

  private void closeWriter() {
    try {
      if (fileWriter != null) fileWriter.close();
    } catch (IOException ignored) {
    }
  }

  /**
   * Waits for the build to finish for the given timeout. Unlike the standard {@link
   * #awaitCompletion(long, TimeUnit)}, this does NOT close resources automatically.
   */
  public boolean awaitHeartbeat(long timeout, TimeUnit unit) throws InterruptedException {
    return this.finishLatch.await(timeout, unit);
  }

  public void logHeartbeat() {
    log.info("[{}] Build in progress... | {}", botId, shorten(currentStep));
  }

  private String shorten(String text) {
    if (text.length() <= 60) return text;
    return text.substring(0, 57) + "...";
  }

  public void logSummary() {
    long duration = (System.currentTimeMillis() - startTime) / 1000;
    if (cacheDetected) {
      log.info("[{}] Image ready (cached).", botId);
    } else {
      log.info("[{}] Image ready (built in {} seconds).", botId, duration);
    }
    logFile.delete();
  }

  public void logBuildFailure() {
    log.error("--------------------------------------------------");
    log.error("BUILD FAILURE REPORT: {}", botId);
    log.error("--------------------------------------------------");
    if (error != null) {
      log.error("Build error: {}", error);
    }
    List<String> logTail = this.getLogTail();
    if (logTail.isEmpty()) {
      log.error("No build logs captured");
    } else {
      String logs = String.join("\n", logTail);
      log.error("Last {} lines of build output: \n{}\n", logTail.size(), logs);
    }
  }

  public boolean hasError() {
    return error != null;
  }

  public String getError() {
    return error;
  }

  public List<String> getLogTail() {
    synchronized (tailBuffer) {
      int size = tailBuffer.size();
      if (size <= errorLogTailSize) {
        return new ArrayList<>(tailBuffer);
      }
      return new ArrayList<>(tailBuffer.subList(size - errorLogTailSize, size));
    }
  }
}
