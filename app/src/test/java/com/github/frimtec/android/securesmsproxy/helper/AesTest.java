package com.github.frimtec.android.securesmsproxy.helper;


import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AesTest {

  private static final String SECRET = "this-is-a-test-secret-12";

  @Test
  public void testAes() {
    Aes aes = new Aes(SECRET);
    String message = aes.decrypt(aes.encrypt("message"));
    assertThat(message, is("message"));
  }
}