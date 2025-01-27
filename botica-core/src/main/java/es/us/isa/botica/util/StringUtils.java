package es.us.isa.botica.util;

import java.util.Random;

public final class StringUtils {
  private StringUtils() {}

  public static String random(long length) {
    return new Random()
        .ints('0', 'z' + 1)
        .filter(i -> i <= '9' || i >= 'A' && i <= 'Z' || i >= 'a')
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  public static String buildEnv(String key, String value) {
    return String.format("%s=%s", key, value);
  }
}
