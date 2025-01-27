package es.us.isa.botica.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ExecutorUtils {
  private ExecutorUtils() {}

  public static ScheduledExecutorService newDaemonSingleThreadScheduledExecutor() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  public static ScheduledExecutorService newDaemonScheduledThreadPool(int corePoolSize) {
    return Executors.newScheduledThreadPool(
        corePoolSize,
        runnable -> {
          Thread thread = new Thread(runnable);
          thread.setDaemon(true);
          return thread;
        });
  }
}
