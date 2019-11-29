package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SmsHelperTest {

  @Test
  public void getSmsFromIntentNullBundleReturnsEmptyList() {
    List<Sms> sms = SmsHelper.getSmsFromIntent(createIntent(null));
    assertThat(sms.size(), is(0));
  }

  @Test
  public void getSmsFromIntentWithNullPdusReturnsEmptyList() {
    List<Sms> sms = SmsHelper.getSmsFromIntent(createIntent(createBundle(1, null)));
    assertThat(sms.size(), is(0));
  }

  @Test
  public void getSmsFromIntentWithSubscriptionReturnsSmsWithSubscription() {
    List<Sms> smsList = SmsHelper.getSmsFromIntent(createIntent(createBundle(1, new Object[]{"pdu1".getBytes()})), (bytes, s) -> createSmsMessage("number", "text"));
    assertThat(smsList.size(), is(1));
    assertThat(smsList.get(0).toString(), is("Sms{number='number', text='text', subscriptionId='1'}"));
  }

  @Test
  public void getSmsFromIntentWithNoSubscriptionReturnsSmsWithDefaultSubscription() {
    List<Sms> smsList = SmsHelper.getSmsFromIntent(createIntent(createBundle(null, new Object[]{"pdu1".getBytes()})), (bytes, s) -> createSmsMessage("number", "text"));
    assertThat(smsList.size(), is(1));
    assertThat(smsList.get(0).toString(), is("Sms{number='number', text='text', subscriptionId='null'}"));
  }

  @Test
  public void getSmsFromIntentWithSplitSmsReturnsMergedSms() {
    List<Sms> smsList = SmsHelper.getSmsFromIntent(createIntent(createBundle(1, new Object[]{"pdu1".getBytes(), "pdu2".getBytes(), "pdu3".getBytes()})), (bytes, format) -> {
      if (Arrays.equals(bytes, "pdu1".getBytes()) || Arrays.equals(bytes, "pdu3".getBytes())) {
        return createSmsMessage("number1", new String(bytes));
      } else {
        return createSmsMessage("number2", "text");
      }
    });
    assertThat(smsList.size(), is(2));
    assertThat(smsList.get(0).toString(), is("Sms{number='number1', text='pdu1pdu3', subscriptionId='1'}"));
    assertThat(smsList.get(1).toString(), is("Sms{number='number2', text='text', subscriptionId='1'}"));
  }

  @Test
  public void sendWithSubscriptionId() {
    SmsManager defaultManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsHelper.send(new Sms("number", "text", 1), defaultManager, (subscriptionId) -> {
      assertThat(subscriptionId, is(1));
      return subscriptionManager;
    });
    verifyNoInteractions(defaultManager);
    verify(subscriptionManager).sendTextMessage("number", null, "text", null, null);
  }

  @Test
  public void sendWithNoSubscriptionId() {
    SmsManager defaultManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsHelper.send(new Sms("number", "text"), defaultManager, (subscriptionId) -> subscriptionManager);
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