package com.github.frimtec.android.securesmsproxy.domain;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ApplicationTest {

  @Test
  public void testProperties() {
    Application application = new Application(12L, "name", "listener", "secret");
    assertThat(application.getId(), is(12L));
    assertThat(application.getName(), is("name"));
    assertThat(application.getListener(), is("listener"));
    assertThat(application.getSecret(), is("secret"));
  }

  @Test
  public void testEquals() {
    Application application1 = new Application(12L, "name", "listener", "secret");
    Application application2 = new Application(11L, "name", "listener", "secret");
    assertThat(application1.equals(application2), is(false));
    assertThat(application2.equals(application1), is(false));

    assertThat(application1.equals(new Application(12L, null, null, null)), is(true));
    //noinspection EqualsWithItself
    assertThat(application1.equals(application1), is(true));

    //noinspection ConstantConditions
    assertThat(application1.equals(null), is(false));

    //noinspection EqualsBetweenInconvertibleTypes
    assertThat(application1.equals("Test"), is(false));
  }

  @Test
  public void testHashCode() {
    Application application1 = new Application(12L, "name", "listener", "secret");
    Application application2 = new Application(11L, "name", "listener", "secret");
    assertThat(application1.hashCode(), is(not(application2.hashCode())));
    assertThat(application2.equals(application1), is(false));
    assertThat(application1.hashCode(), is(new Application(12L, null, null, null).hashCode()));
  }

  @Test
  public void testToString() {
    Application application = new Application(12L, "name", "listener", "secret");
    assertThat(application.toString(), is("Application{id=12, name='name', listener='listener', secret='secret'}"));
  }

}