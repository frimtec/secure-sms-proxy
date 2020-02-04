package com.github.frimtec.android.securesmsproxyapi.utility;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AesTest {

  private static final String SECRET = "this-is-a-test-secret-12";

  @Test
  void testAes() {
    Aes aes = new Aes(SECRET);
    String message = aes.decrypt(aes.encrypt("message"));
    assertThat(message).isEqualTo("message");
  }
}