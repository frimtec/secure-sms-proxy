package com.github.frimtec.android.securesmsproxy.domain;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

  @Test
  void testProperties() {
    Application application = new Application(12L, "name", "listener", "secret");
    assertThat(application.getId()).isEqualTo(12L);
    assertThat(application.getName()).isEqualTo("name");
    assertThat(application.getListener()).isEqualTo("listener");
    assertThat(application.getSecret()).isEqualTo("secret");
  }

  @Test
  void testEquals() {
    Application application1 = new Application(12L, "name", "listener", "secret");
    Application application2 = new Application(11L, "name", "listener", "secret");
    assertThat(application1.equals(application2)).isFalse();
    assertThat(application2.equals(application1)).isFalse();

    assertThat(application1.equals(new Application(12L, null, null, null))).isTrue();
    //noinspection EqualsWithItself
    assertThat(application1.equals(application1)).isTrue();

    assertThat(application1.equals(null)).isFalse();

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(application1.equals("Test")).isFalse();
  }

  @Test
  void testHashCode() {
    Application application1 = new Application(12L, "name", "listener", "secret");
    Application application2 = new Application(11L, "name", "listener", "secret");
    assertThat(application1.hashCode()).isNotEqualTo(application2.hashCode());
    assertThat(application2.equals(application1)).isFalse();
    assertThat(application1.hashCode()).isEqualTo(new Application(12L, null, null, null).hashCode());
  }

  @Test
  void testToString() {
    Application application = new Application(12L, "name", "listener", "secret");
    assertThat(application.toString()).isEqualTo("Application{id=12, name='name', listener='listener', secret='secret'}");
  }

}