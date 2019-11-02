package com.github.frimtec.android.securesmsproxy.domain;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ApplicationRuleTest {

  @Test
  public void testProperties() {
    Application application= new Application(12L, "name", "listener", "secret");
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    ApplicationRule applicationRule = new ApplicationRule(application, allowedPhoneNumbers);
    assertThat(applicationRule .getApplication(), is(application));
    assertThat(applicationRule .getAllowedPhoneNumbers(), is(allowedPhoneNumbers));
  }

  @Test
  public void testEquals() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret");
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);

    assertThat(applicationRule1.equals(applicationRule2), is(false));
    assertThat(applicationRule2.equals(applicationRule1), is(false));

    assertThat(applicationRule1.equals(new ApplicationRule(new Application(12L, null, null, null), allowedPhoneNumbers)), is(true));
    //noinspection EqualsWithItself
    assertThat(applicationRule1.equals(applicationRule1), is(true));

    //noinspection ConstantConditions
    assertThat(applicationRule1.equals(null), is(false));

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(applicationRule1.equals("Test"), is(false));
  }

  @Test
  public void testHashCode() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    Application application2 = new Application(11L, "name", "listener", "secret");
    ApplicationRule applicationRule2 = new ApplicationRule(application2, allowedPhoneNumbers);
    assertThat(applicationRule1.hashCode(), is(not(applicationRule2.hashCode())));
    assertThat(applicationRule2.equals(applicationRule1), is(false));
    assertThat(applicationRule1.hashCode(), is(new ApplicationRule(new Application(12L, null, null, null), allowedPhoneNumbers).hashCode()));
  }

  @Test
  public void testToString() {
    Set<String> allowedPhoneNumbers = Collections.singleton("number");
    Application application1 = new Application(12L, "name", "listener", "secret");
    ApplicationRule applicationRule1 = new ApplicationRule(application1, allowedPhoneNumbers);
    assertThat(applicationRule1.toString(), is("ApplicationRule{application=Application{id=12, name='name', listener='listener', secret='secret'}, allowedPhoneNumbers=[number]}"));
  }

}