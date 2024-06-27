package es.us.isa.botica.director.util;

public final class StringUtils {
  private StringUtils() {}

  public static String buildEnv(String key, String value) {
    return String.format("%s=%s", key, value);
  }
}
