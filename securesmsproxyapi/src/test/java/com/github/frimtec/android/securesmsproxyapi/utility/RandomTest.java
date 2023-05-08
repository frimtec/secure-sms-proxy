package com.github.frimtec.android.securesmsproxyapi.utility;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


class RandomTest {

  @Test
  void nextString() {
    String random = Random.nextString(10);
    assertThat(random.length()).isEqualTo(10);
    assertThat(random).isNotEqualTo(Random.nextString(10));
  }

  @Test
  void nextBytes() {
    byte[] random = Random.nextBytes(10);
    assertThat(random.length).isEqualTo(10);
    assertThat(random).isNotEqualTo(Random.nextBytes(10));
  }

  @Test
  void nextStringToSmallLength() {
    assertThrows(IllegalArgumentException.class, () -> Random.nextString(0));
  }

  @Test
  void nextBytesToSmallLength() {
    assertThrows(IllegalArgumentException.class, () -> Random.nextBytes(0));
  }
}