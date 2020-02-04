package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SmsHelperTest {

  @Test
  void getSmsFromIntentNullBundleReturnsEmptyList() {
    SmsHelper smsHelper = new SmsHelper();
    List<Sms> sms = smsHelper.getSmsFromIntent(createIntent(null));
    assertThat(sms.size()).isEqualTo(0);
  }

  @Test
  void getSmsFromIntentWithNullPdusReturnsEmptyList() {
    SmsHelper smsHelper = new SmsHelper();
    List<Sms> sms = smsHelper.getSmsFromIntent(createIntent(createBundle(1, null)));
    assertThat(sms.size()).isEqualTo(0);
  }

  @Test
  void getSmsFromIntentWithSubscriptionReturnsSmsWithSubscription() {
    SmsHelper smsHelper = new SmsHelper((bytes, s) -> createSmsMessage("number", "text"), null, null);
    List<Sms> smsList = smsHelper.getSmsFromIntent(createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number', text='text', subscriptionId='1'}");
  }

  @Test
  void getSmsFromIntentWithNoSubscriptionReturnsSmsWithDefaultSubscription() {
    SmsHelper smsHelper = new SmsHelper((bytes, s) -> createSmsMessage("number", "text"), null, null);
    List<Sms> smsList = smsHelper.getSmsFromIntent(createIntent(createBundle(null, new Object[]{"pdu1".getBytes()})));
    assertThat(smsList.size()).isEqualTo(1);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number', text='text', subscriptionId='null'}");
  }

  @Test
  void getSmsFromIntentWithSplitSmsReturnsMergedSms() {
    SmsHelper smsHelper = new SmsHelper((bytes, format) -> {
      if (Arrays.equals(bytes, "pdu1".getBytes()) || Arrays.equals(bytes, "pdu3".getBytes())) {
        return createSmsMessage("number1", new String(bytes));
      } else {
        return createSmsMessage("number2", "text");
      }
    }, null, null);
    List<Sms> smsList = smsHelper.getSmsFromIntent(createIntent(createBundle(1, new Object[]{"pdu1".getBytes(), "pdu2".getBytes(), "pdu3".getBytes()})));
    assertThat(smsList.size()).isEqualTo(2);
    assertThat(smsList.get(0).toString()).isEqualTo("Sms{number='number1', text='pdu1pdu3', subscriptionId='1'}");
    assertThat(smsList.get(1).toString()).isEqualTo("Sms{number='number2', text='text', subscriptionId='1'}");
  }

  @Test
  void sendWithSubscriptionId() {
    SmsManager defaultManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsHelper smsHelper = new SmsHelper(null, defaultManager, (subscriptionId) -> {
      assertThat(subscriptionId).isEqualTo(1);
      return subscriptionManager;
    });
    smsHelper.send(new Sms("number", "text", 1));
    verifyNoInteractions(defaultManager);
    verify(subscriptionManager).sendTextMessage("number", null, "text", null, null);
  }

  @Test
  void sendWithNoSubscriptionId() {
    SmsManager defaultManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsHelper smsHelper = new SmsHelper(null, defaultManager, (subscriptionId) -> subscriptionManager);
    smsHelper.send(new Sms("number", "text"));
    verify(defaultManager).sendTextMessage("number", null, "text", null, null);
    verifyNoInteractions(subscriptionManager);
  }

  private SmsMessage createSmsMessage(String number, String text) {
    SmsMessage smsMessage = mock(SmsMessage.class);
    when(smsMessage.getOriginatingAddress()).thenReturn(number);
    when(smsMessage.getMessageBody()).thenReturn(text);
    return smsMessage;
  }

  private Bundle createBundle(Integer subscription, Object[] pdus) {
    Bundle bundle = mock(Bundle.class);
    if (subscription != null) {
      when(bundle.getInt("subscription", -1)).thenReturn(subscription);
    } else {
      when(bundle.getInt("subscription", -1)).thenReturn(-1);
    }

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