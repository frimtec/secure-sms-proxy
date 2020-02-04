package com.github.frimtec.android.securesmsproxyapi.utility;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RandomStringTest {

  @Test
  void nextString() {
    String random = RandomString.nextString(10);
    assertThat(random.length()).isEqualTo(10);
    assertThat(random.length()).isNotEqualTo(RandomString.nextString(10));
  }

  @Test
  void nextStringToSmallLength() {
    assertThrows(IllegalArgumentException.class, () -> RandomString.nextString(0));
  }
}