package com.github.frimtec.android.securesmsproxy.domain;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

class ApplicationRuleTest {

  @Test
  void testProperties() {
    Application application = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    Map<String, RuleStatistics> allowedPhoneNumbers = Map.of("number", new RuleStatistics(1L, 8L, 9L));
    ApplicationRule applicationRule = new ApplicationRule(application, allowedPhoneNumbers);
    assertThat(applicationRule.application()).isEqualTo(application);
    assertThat(applicationRule.allowedPhoneNumbers()).isEqualTo(allowedPhoneNumbers);
  }

  @Test
  void testEquals() {
    Map<String, RuleStatistics> allowedPhoneNumbers = Map.of("number", new RuleStatistics(1L, 0L, 0L));
    Application application1 = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret", new ApplicationStatistics(11L, 0L, 0L));
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);

    assertThat(applicationRule1.equals(applicationRule2)).isFalse();
    assertThat(applicationRule2.equals(applicationRule1)).isFalse();

    assertThat(applicationRule1.equals(new ApplicationRule(new Application(12L, null, null, null, null), allowedPhoneNumbers))).isTrue();
    //noinspection EqualsWithItself
    assertThat(applicationRule1.equals(applicationRule1)).isTrue();

    //noinspection ConstantConditions
    assertThat(applicationRule1.equals(null)).isFalse();

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(applicationRule1.equals("Test")).isFalse();
  }

  @Test
  void testHashCode() {
    Map<String, RuleStatistics> allowedPhoneNumbers = Map.of("number", new RuleStatistics(1L, 0L, 0L));
    Application application1 = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret", new ApplicationStatistics(11L, 0L, 0L));
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);

    assertThat(applicationRule1.hashCode()).isNotEqualTo(applicationRule2.hashCode());
    assertThat(applicationRule2.equals(applicationRule1)).isFalse();
    assertThat(applicationRule1.hashCode()).isEqualTo(new ApplicationRule(new Application(12L, null, null, null, null), allowedPhoneNumbers).hashCode());
  }

  @Test
  void testToString() {
    Map<String, RuleStatistics> allowedPhoneNumbers = Map.of("number", new RuleStatistics(1L, 0L, 0L));
    Application application1 = new Application(12L, "name", "listener", "secret", new ApplicationStatistics(12L, 5L, 3L));
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    assertThat(applicationRule1.toString()).isEqualTo("ApplicationRule[application=Application[id=12, name=name, listener=listener, secret=secret, statistics=ApplicationStatistics[applicationId=12, sendBlockCount=5, loopbackCount=3]], allowedPhoneNumbers={number=RuleStatistics[ruleId=1, sendCount=0, receiveCount=0]}]");
  }

}