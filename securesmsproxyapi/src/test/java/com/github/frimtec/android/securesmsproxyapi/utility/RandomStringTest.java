package com.github.frimtec.android.securesmsproxyapi.utility;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


class RandomStringTest {

  @Test
  void nextString() {
    String random = RandomString.nextString(10);
    assertThat(random.length()).isEqualTo(10);
    assertThat(random).isNotEqualTo(RandomString.nextString(10));
  }

  @Test
  void nextStringToSmallLength() {
    assertThrows(IllegalArgumentException.class, () -> RandomString.nextString(0));
  }
}