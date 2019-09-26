package com.github.frimtec.android.securesmsproxy.helper;

import java.security.SecureRandom;
import java.util.Random;

public class RandomString {

  private static final char[] SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
  private static final Random RANDOM = new SecureRandom();

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
}
