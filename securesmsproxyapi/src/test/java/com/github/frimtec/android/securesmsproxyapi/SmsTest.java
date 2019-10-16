package com.github.frimtec.android.securesmsproxyapi;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class SmsTest {

  @Test
  public void getNumber() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getNumber(), is("1234"));
  }

  @Test
  public void getText() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getText(), is("Text"));
  }

  @Test
  public void getSubscriptionId() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getSubscriptionId(), is(5678));
  }

  @Test
  public void createSmsWithDefaultSubscription() {
    Sms sms = new Sms("1234", "Text");
    assertThat(sms.getSubscriptionId(), is(nullValue()));
  }

  @Test
  public void testToString() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.toString(), CoreMatchers.allOf(
        containsString("1234"),
        containsString("Text"),
        containsString("5678")
    ));
  }

  @Test
  public void testFromJson() {
    Sms sms = Sms.fromJson("{\"number\":\"1234\",\"text\":\"Text\",\"subscriptionId\":5678}");
    assertThat(sms.getNumber(), is("1234"));
    assertThat(sms.getText(), is("Text"));
    assertThat(sms.getSubscriptionId(), is(5678));
  }

  @Test
  public void testFromJson_subscriptionId_null() {
    Sms sms = Sms.fromJson("{\"number\":\"1234\",\"text\":\"Text\"}");
    assertThat(sms.getNumber(), is("1234"));
    assertThat(sms.getText(), is("Text"));
    assertThat(sms.getSubscriptionId(), is(nullValue()));
  }

  @Test
  public void testToJsonObject() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.toJson(), is("{\"number\":\"1234\",\"text\":\"Text\",\"subscriptionId\":5678}"));
  }

  @Test
  public void testToJsonObject_subscriptionId_null() {
    Sms sms = new Sms("1234", "Text", null);
    assertThat(sms.toJson(), is("{\"number\":\"1234\",\"text\":\"Text\"}"));
  }

}