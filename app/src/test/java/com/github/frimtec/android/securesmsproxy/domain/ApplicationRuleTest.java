package com.github.frimtec.android.securesmsproxy.domain;


import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationRuleTest {

  @Test
  void testProperties() {
    Application application = new Application(12L, "name", "listener", "secret");
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    ApplicationRule applicationRule = new ApplicationRule(application, allowedPhoneNumbers);
    assertThat(applicationRule.getApplication()).isEqualTo(application);
    assertThat(applicationRule.getAllowedPhoneNumbers()).isEqualTo(allowedPhoneNumbers);
  }

  @Test
  void testEquals() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret");
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);

    assertThat(applicationRule1.equals(applicationRule2)).isFalse();
    assertThat(applicationRule2.equals(applicationRule1)).isFalse();

    assertThat(applicationRule1.equals(new ApplicationRule(new Application(12L, null, null, null), allowedPhoneNumbers))).isTrue();
    //noinspection EqualsWithItself
    assertThat(applicationRule1.equals(applicationRule1)).isTrue();

    //noinspection ConstantConditions
    assertThat(applicationRule1.equals(null)).isFalse();

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(applicationRule1.equals("Test")).isFalse();
  }

  @Test
  void testHashCode() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret");
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);
    assertThat(applicationRule1.hashCode()).isNotEqualTo(applicationRule2.hashCode());
    assertThat(applicationRule2.equals(applicationRule1)).isFalse();
    assertThat(applicationRule1.hashCode()).isEqualTo(new ApplicationRule(new Application(12L, null, null, null), allowedPhoneNumbers).hashCode());
  }

  @Test
  void testToString() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    assertThat(applicationRule1.toString()).isEqualTo("ApplicationRule{application=Application{id=12, name='name', listener='listener', secret='secret'}, allowedPhoneNumbers=[number]}");
  }

}