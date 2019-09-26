package com.github.frimtec.android.securesmsproxy.helper;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class RandomStringTest {

  @Test
  public void nextString() {
    String random = RandomString.nextString(10);
    assertThat(random.length(), is(10));
    assertThat(random.length(), is(not(RandomString.nextString(10))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nextStringToSmallLength() {
    RandomString.nextString(0);
  }
}