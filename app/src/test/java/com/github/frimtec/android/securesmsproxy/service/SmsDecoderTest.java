package com.github.frimtec.android.securesmsproxy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class SmsDecoderTest {

  private final PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter("ch");

  @Test
  void getSmsFromIntentNullBundleReturnsEmptyList() {
    SmsDecoder smsDecoder = new SmsDecoder();
    List<Sms> sms = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(null));
    assertThat(sms.size()).isEqualTo(0);
  }

  @Test
  void getSmsFromIntentWithNullPdusReturnsEmptyList() {
    SmsDecoder smsDecoder = new SmsDecoder();
    List<Sms> sms = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, null)));
    assertThat(sms.size()).isEqualTo(0);
  }

  @Test
  void getSmsFromIntentWithInternationalNumber() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, s) -> createSmsMessage("+4179000000", "text"));
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='+4179000000', text='text', subscriptionId='1'}");
  }

  @Test
  void getSmsFromIntentWithInternationalNumberMissingPlus() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, s) -> createSmsMessage("41790000000", "text"));
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='+41790000000', text='text', subscriptionId='1'}");
  }

  @Test
  void getSmsFromIntentWithLocalNumber() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, s) -> createSmsMessage("0791234567", "text"));
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='+41791234567', text='text', subscriptionId='1'}");
  }

  @Test
  void getSmsFromIntentWithSubscriptionReturnsSmsWithSubscription() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, s) -> createSmsMessage("number", "text"));
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number', text='text', subscriptionId='1'}");
  }

  @Test
  void getSmsFromIntentWithNoSubscriptionReturnsSmsWithDefaultSubscription() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, s) -> createSmsMessage("number", "text"));
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(null, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number', text='text', subscriptionId='null'}");
  }

  @Test
  void getSmsFromIntentWithSplitSmsReturnsMergedSms() {
    SmsDecoder smsDecoder = new SmsDecoder((bytes, format) -> {
      if (Arrays.equals(bytes, "pdu1".getBytes()) || Arrays.equals(bytes, "pdu3".getBytes())) {
        return createSmsMessage("number1", new String(bytes));
      } else {
        return createSmsMessage("number2", "text");
      }
    });
    List<Sms> smsList = smsDecoder.getSmsFromIntent(phoneNumberFormatter, createIntent(createBundle(1, new Object[]{"pdu1".getBytes(), "pdu2".getBytes(), "pdu3".getBytes()})));
    assertThat(smsList.size()).isEqualTo(2);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number1', text='pdu1pdu3', subscriptionId='1'}");
    assertThat(smsList.get(1).toString()).isEqualTo("Sms{number='number2', text='text', subscriptionId='1'}");
  }

  private SmsMessage createSmsMessage(String number, String text) {
    SmsMessage smsMessage = mock(SmsMessage.class);
    when(smsMessage.getOriginatingAddress()).thenReturn(number);
    when(smsMessage.getMessageBody()).thenReturn(text);
    return smsMessage;
  }

  private Bundle createBundle(Integer subscription, Object[] pdus) {
    Bundle bundle = mock(Bundle.class);
    when(bundle.getInt("subscription", -1)).thenReturn(Objects.requireNonNullElse(subscription, -1));

    when(bundle.get("pdus")).thenReturn(pdus);
    when(bundle.getString("format")).thenReturn("format");
    return bundle;
  }

  private Intent createIntent(Bundle bundle) {
    Intent intent = mock(Intent.class);
    when(intent.getExtras()).thenReturn(bundle);
    return intent;
  }
}