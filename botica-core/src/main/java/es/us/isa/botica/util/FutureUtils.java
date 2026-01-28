package es.us.isa.botica.util;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class FutureUtils {
  private FutureUtils() {}

  public static void awaitCompletion(CompletableFuture<?>... futures) {
    try {
      CompletableFuture.allOf(futures).join();
    } catch (CompletionException e) {
      throw e.getCause() instanceof RuntimeException
          ? (RuntimeException) e.getCause()
          : new RuntimeException(e.getCause());
    }
  }

  public static <T> void awaitCompletion(Collection<CompletableFuture<T>> futures) {
    awaitCompletion(futures.toArray(CompletableFuture[]::new));
  }
}
