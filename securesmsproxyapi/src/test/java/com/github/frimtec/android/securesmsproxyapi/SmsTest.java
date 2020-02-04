package com.github.frimtec.android.securesmsproxyapi;


import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SmsTest {

  @Test
  void getNumber() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getNumber()).isEqualTo("1234");
  }

  @Test
  void getText() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getText()).isEqualTo("Text");
  }

  @Test
  void getSubscriptionId() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.getSubscriptionId()).isEqualTo(5678);
  }

  @Test
  void createSmsWithDefaultSubscription() {
    Sms sms = new Sms("1234", "Text");
    assertThat(sms.getSubscriptionId()).isNull();
  }

  @Test
  void testToString() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.toString()).isEqualTo("Sms{number='1234', text='Text', subscriptionId='5678'}");
  }

  @Test
  void testFromJson() {
    Sms sms = Sms.fromJson("{\"number\":\"1234\",\"text\":\"Text\",\"subscriptionId\":5678}");
    assertThat(sms.getNumber()).isEqualTo("1234");
    assertThat(sms.getText()).isEqualTo("Text");
    assertThat(sms.getSubscriptionId()).isEqualTo(5678);
  }

  @Test
  void testFromJsonArray() {
    List<Sms> smsList = Sms.fromJsonArray("[{\"number\":\"110\",\"text\":\"Text\",\"subscriptionId\":5678}, {\"number\":\"111\",\"text\":\"Text\",\"subscriptionId\":5678}]");
    assertThat(smsList.size()).isEqualTo(2);
    assertThat(smsList.get(0).getNumber()).isEqualTo("110");
    assertThat(smsList.get(1).getNumber()).isEqualTo("111");
  }

  @Test
  void testFromJsonWithBadJson() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> Sms.fromJson("{\"numberX\":\"1234\",\"textX\":\"Text\",\"subscriptionIdX\":5678}"));
    assertThat(exception.getMessage()).startsWith("Cannot parse JSON string");

  }

  @Test
  void testFromJsonArrayWithBadJson() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> Sms.fromJsonArray("{\"numberX\":\"1234\",\"textX\":\"Text\",\"subscriptionIdX\":5678}"));
    assertThat(exception.getMessage()).startsWith("Cannot parse JSON string");
  }

  @Test
  void testFromJsonSubscriptionIdNull() {
    Sms sms = Sms.fromJson("{\"number\":\"1234\",\"text\":\"Text\"}");
    assertThat(sms.getNumber()).isEqualTo("1234");
    assertThat(sms.getText()).isEqualTo("Text");
    assertThat(sms.getSubscriptionId()).isNull();
  }

  @Test
  void testToJsonObject() {
    Sms sms = new Sms("1234", "Text", 5678);
    assertThat(sms.toJson()).isEqualTo("{\"number\":\"1234\",\"text\":\"Text\",\"subscriptionId\":5678}");
  }

  @Test
  void testToJsonObjectFromJsonObject() {
    Sms sms1 = new Sms("1234", "Text", 5678);
    Sms sms2 = Sms.fromJson(sms1.toJson());
    assertThat(sms1.getNumber()).isEqualTo(sms2.getNumber());
    assertThat(sms1.getText()).isEqualTo(sms2.getText());
    assertThat(sms1.getSubscriptionId()).isEqualTo(sms2.getSubscriptionId());
  }

  @Test
  void testToJsonArray() {
    List<Sms> smsList = Arrays.asList(new Sms("110", "Text", 5678), new Sms("111", "Text", 5678));
    assertThat(Sms.toJsonArray(smsList)).isEqualTo("[{\"number\":\"110\",\"text\":\"Text\",\"subscriptionId\":5678},{\"number\":\"111\",\"text\":\"Text\",\"subscriptionId\":5678}]");
  }

  @Test
  void testToJsonObjectSubscriptionIdNull() {
    Sms sms = new Sms("1234", "Text", null);
    assertThat(sms.toJson()).isEqualTo("{\"number\":\"1234\",\"text\":\"Text\"}");
  }

}