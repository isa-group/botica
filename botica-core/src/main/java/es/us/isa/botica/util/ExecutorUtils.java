package es.us.isa.botica.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ExecutorUtils {
  private ExecutorUtils() {}

  public static ScheduledExecutorService newDaemonSingleThreadScheduledExecutor() {
    return Executors.newSingleThreadScheduledExecutor(ExecutorUtils::newDaemonThreadFactory);
  }

  public static ExecutorService newDaemonFixedThreadPool(int corePoolSize) {
    return Executors.newFixedThreadPool(corePoolSize, ExecutorUtils::newDaemonThreadFactory);
  }

  public static ScheduledExecutorService newDaemonScheduledThreadPool(int corePoolSize) {
    return Executors.newScheduledThreadPool(corePoolSize, ExecutorUtils::newDaemonThreadFactory);
  }

  private static Thread newDaemonThreadFactory(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    return thread;
  }
}
