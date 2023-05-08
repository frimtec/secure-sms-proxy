package com.github.frimtec.android.securesmsproxyapi.utility;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class Aes2Test {

  private static final String SECRET = "this-is-a-test-secret-12";

  @Test
  void testAes() {
    Aes2 aes = new Aes2(SECRET);
    String message = aes.decrypt(aes.encrypt("message"));
    assertThat(message).isEqualTo("message");
  }
}