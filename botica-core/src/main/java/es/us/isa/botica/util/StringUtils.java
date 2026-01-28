package es.us.isa.botica.util;

import java.util.Random;

public final class StringUtils {
  private StringUtils() {}

  public static String buildEnv(String key, String value) {
    return String.format("%s=%s", key, value);
  }

  public static String random(long length) {
    return new Random()
        .ints('0', 'z' + 1)
        .filter(i -> i <= '9' || i >= 'A' && i <= 'Z' || i >= 'a')
        .limit(length)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  public static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
