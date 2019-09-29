package com.github.frimtec.android.securesmsproxyapi;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SmsTest {

  @Test
  public void getNumber() {
    Sms sms = new Sms("1234", "Text");
    assertThat(sms.getNumber(), is("1234"));
  }

  @Test
  public void getText() {
    Sms sms = new Sms("1234", "Text");
    assertThat(sms.getText(), is("Text"));
  }

  @Test
  public void testToString() {
    Sms sms = new Sms("1234", "Text");
    assertThat(sms.toString(), CoreMatchers.allOf(
        containsString("1234"),
        containsString("Text")
    ));
  }

}