package com.github.frimtec.android.securesmsproxy.domain;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

class ApplicationTest {

  @Test
  void testProperties() {
    Map<String, RuleStatistics> allowedPhoneNumbers = Map.of("number", new RuleStatistics(1L, 0L, 0L));
    Application application = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    assertThat(application.id()).isEqualTo(12L);
    assertThat(application.name()).isEqualTo("name");
    assertThat(application.listener()).isEqualTo("listener");
    assertThat(application.secret()).isEqualTo("secret");
  }

  @Test
  void testEquals() {
    Application application1 = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    Application application2 = new Application(11L, "name", "listener", "secret", new ApplicationStatistics(11L, 0L, 0L));
    assertThat(application1.equals(application2)).isFalse();
    assertThat(application2.equals(application1)).isFalse();

    assertThat(application1.equals(new Application(12L, null, null, null, null))).isTrue();
    //noinspection EqualsWithItself
    assertThat(application1.equals(application1)).isTrue();

    //noinspection ConstantConditions
    assertThat(application1.equals(null)).isFalse();

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(application1.equals("Test")).isFalse();
  }

  @Test
  void testHashCode() {
    Application application1 = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    Application application2 = new Application(11L, "name", "listener", "secret", new ApplicationStatistics(11L, 5L, 3L));
    assertThat(application1.hashCode()).isNotEqualTo(application2.hashCode());
    assertThat(application2.equals(application1)).isFalse();
    assertThat(application1.hashCode()).isEqualTo(new Application(12L, null, null, null, null).hashCode());
  }

  @Test
  void testToString() {
    Application application = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    assertThat(application.toString()).isEqualTo("Application[id=12, name=name, listener=listener, secret=secret, statistics=ApplicationStatistics[applicationId=12, sendBlockCount=5, loopbackCount=3]]");
  }

}