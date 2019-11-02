package com.github.frimtec.android.securesmsproxyapi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class SmsTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

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
    assertThat(sms.toString(), is("Sms{number='1234', text='Text', subscriptionId='5678'}"));
  }

  @Test
  public void testFromJson() {
    Sms sms = Sms.fromJson("{\"number\":\"1234\",\"text\":\"Text\",\"subscriptionId\":5678}");
    assertThat(sms.getNumber(), is("1234"));
    assertThat(sms.getText(), is("Text"));
    assertThat(sms.getSubscriptionId(), is(5678));
  }

  @Test
  public void testFromJsonArray() {
    List<Sms> smsList = Sms.fromJsonArray("[{\"number\":\"110\",\"text\":\"Text\",\"subscriptionId\":5678}, {\"number\":\"111\",\"text\":\"Text\",\"subscriptionId\":5678}]");
    assertThat(smsList.size(), is(2));
    assertThat(smsList.get(0).getNumber(), is("110"));
    assertThat(smsList.get(1).getNumber(), is("111"));
  }

  @Test
  public void testFromJsonWithBadJson() {
    this.expectedException.expect(IllegalArgumentException.class);
    this.expectedException.expectMessage(containsString("Cannot parse JSON string"));
    Sms.fromJson("{\"numberX\":\"1234\",\"textX\":\"Text\",\"subscriptionIdX\":5678}");
  }

  @Test
  public void testFromJsonArrayWithBadJson() {
    this.expectedException.expect(IllegalArgumentException.class);
    this.expectedException.expectMessage(containsString("Cannot parse JSON string"));
    Sms.fromJsonArray("{\"numberX\":\"1234\",\"textX\":\"Text\",\"subscriptionIdX\":5678}");
  }

  @Test
  public void testFromJsonSubscriptionIdNull() {
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
  public void testToJsonObjectFromJsonObject() {
    Sms sms1 = new Sms("1234", "Text", 5678);
    Sms sms2 = Sms.fromJson(sms1.toJson());
    assertThat(sms1.getNumber(), is(sms2.getNumber()));
    assertThat(sms1.getText(), is(sms2.getText()));
    assertThat(sms1.getSubscriptionId(), is(sms2.getSubscriptionId()));
  }

  @Test
  public void testToJsonArray() {
    List<Sms> smsList = Arrays.asList(new Sms("110", "Text", 5678), new Sms("111", "Text", 5678));
    assertThat(Sms.toJsonArray(smsList), is("[{\"number\":\"110\",\"text\":\"Text\",\"subscriptionId\":5678},{\"number\":\"111\",\"text\":\"Text\",\"subscriptionId\":5678}]"));
  }

  @Test
  public void testToJsonObjectSubscriptionIdNull() {
    Sms sms = new Sms("1234", "Text", null);
    assertThat(sms.toJson(), is("{\"number\":\"1234\",\"text\":\"Text\"}"));
  }

}