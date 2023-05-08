package com.github.frimtec.android.securesmsproxyapi.utility;

import java.security.SecureRandom;

public class Random {

  private static final char[] SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  private static final java.util.Random RANDOM = new SecureRandom();

  /**
   * Generates a random string.
   */
  public static String nextString(int length) {
    if (length < 1) {
      throw new IllegalArgumentException("Length must be bigger than 0");
    }
    char[] buffer = new char[length];
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
    }
    return new String(buffer);
  }

  public static byte[] nextBytes(int length) {
    if (length < 1) {
      throw new IllegalArgumentException("Length must be bigger than 0");
    }
    byte[] buffer = new byte[length];
    RANDOM.nextBytes(buffer);
    return buffer;
  }
}
