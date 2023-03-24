package com.github.frimtec.android.securesmsproxy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;


class SmsListenerTest {

  private final PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter("ch");

  static final String SECRET = "1234567890123456";

  @Test
  void constructor() {
    SmsListener smsListener = new SmsListener();
    assertThat(smsListener).isNotNull();
  }

  @Test
  void broadcastReceivedSms() {
    Context context = mock(Context.class);
    Application application = new Application(1L, "app1", "listener1", SECRET);
    List<Sms> smsList = Collections.singletonList(new Sms("number", "text"));
    SmsListener.broadcastReceivedSms(context, application, smsList);
    verify(context).sendOrderedBroadcast(any(), isNull());
  }

  @Test
  void onReceiveSmsWithNoRegisteredApplicationNoBroadcast() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    Intent intent = createIntent();
    BiFunction<Application, String, Intent> smsBroadcastIntentFactory = broadcastIntentFactory(mock(Intent.class), ArgumentCaptor.forClass(String.class));
    SmsDecoder smsDecoder = mock(SmsDecoder.class);
    when(smsDecoder.getSmsFromIntent(phoneNumberFormatter, intent)).thenReturn(Collections.singletonList(new Sms("number", "text")));
    SmsListener smsListener = new SmsListener(smsDecoder, dao, smsBroadcastIntentFactory, (context) -> phoneNumberFormatter);
    when(dao.byPhoneNumbers(any())).thenReturn(Collections.emptyMap());

    Context context = mock(Context.class);
    smsListener.onReceive(context, intent);
    verifyNoInteractions(smsBroadcastIntentFactory);
    verifyNoInteractions(context);
  }

  @Test
  void onReceiveSmsWithWithRegisteredApplicationSendBroadcast() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    Intent intent = createIntent();
    Intent broadcastIntent = mock(Intent.class);
    ArgumentCaptor<String> encryptedSmsCaptor = ArgumentCaptor.forClass(String.class);
    BiFunction<Application, String, Intent> smsBroadcastIntentFactory = broadcastIntentFactory(broadcastIntent, encryptedSmsCaptor);
    SmsDecoder smsDecoder = mock(SmsDecoder.class);
    List<Sms> smsList = Collections.singletonList(new Sms("number", "text"));
    when(smsDecoder.getSmsFromIntent(phoneNumberFormatter, intent)).thenReturn(smsList);
    SmsListener smsListener = new SmsListener(smsDecoder, dao, smsBroadcastIntentFactory, (context) -> phoneNumberFormatter);
    Application application = new Application(1L, "app1", "listener1", SECRET);
    when(dao.byPhoneNumbers(any())).thenReturn(Collections.singletonMap("number", Collections.singleton(application)));

    Context context = mock(Context.class);
    smsListener.onReceive(context, intent);
    verify(context).sendOrderedBroadcast(eq(broadcastIntent), isNull());

    String encryptedSms = encryptedSmsCaptor.getValue();
    verify(smsBroadcastIntentFactory).apply(application, encryptedSms);
    assertThat(new Aes(SECRET).decrypt(encryptedSms)).isEqualTo(Sms.toJsonArray(smsList));
  }

  @Test
  void onReceiveSmsWithWithRegisteredApplicationSendBroadcastWithOriginalBroadcastIntentSupplier() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    Intent intent = createIntent();
    SmsDecoder smsDecoder = mock(SmsDecoder.class);
    List<Sms> smsList = Collections.singletonList(new Sms("number", "text"));
    when(smsDecoder.getSmsFromIntent(phoneNumberFormatter, intent)).thenReturn(smsList);
    SmsListener smsListener = new SmsListener(smsDecoder, dao, SmsListener.SMS_BROADCAST_INTENT_SUPPLIER, (context) -> phoneNumberFormatter);
    Application application = new Application(1L, "app1", "listener1", SECRET);
    when(dao.byPhoneNumbers(any())).thenReturn(Collections.singletonMap("number", Collections.singleton(application)));

    Context context = mock(Context.class);
    smsListener.onReceive(context, intent);
    verify(context).sendOrderedBroadcast(any(), isNull());
  }

  @Test
  void onReceiveSmsWithWithTwoRegisteredApplicationSendBroadcastTwice() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    Intent intent = createIntent();
    Intent broadcastIntent = mock(Intent.class);
    ArgumentCaptor<String> encryptedSmsCaptor = ArgumentCaptor.forClass(String.class);
    SmsDecoder smsDecoder = mock(SmsDecoder.class);
    List<Sms> smsList = Collections.singletonList(new Sms("number", "text"));
    when(smsDecoder.getSmsFromIntent(phoneNumberFormatter, intent)).thenReturn(smsList);
    BiFunction<Application, String, Intent> smsBroadcastIntentFactory = broadcastIntentFactory(broadcastIntent, encryptedSmsCaptor);
    SmsListener smsListener = new SmsListener(smsDecoder, dao, smsBroadcastIntentFactory, (context) -> phoneNumberFormatter);
    Application application1 = new Application(1L, "app1", "listener1", SECRET);
    Application application2 = new Application(2L, "app2", "listener2", SECRET.replaceAll("1", "A"));
    when(dao.byPhoneNumbers(any())).thenReturn(Collections.singletonMap("number", new HashSet<>(Arrays.asList(application1, application2))));

    Context context = mock(Context.class);
    smsListener.onReceive(context, intent);
    verify(context, times(2)).sendOrderedBroadcast(eq(broadcastIntent), isNull());

    List<String> encryptedSmsList = encryptedSmsCaptor.getAllValues();
    verify(smsBroadcastIntentFactory).apply(application1, encryptedSmsList.get(0));
    verify(smsBroadcastIntentFactory).apply(application2, encryptedSmsList.get(1));
    assertThat(new Aes(SECRET).decrypt(encryptedSmsList.get(0))).isEqualTo(Sms.toJsonArray(smsList));
    assertThat(new Aes(SECRET.replaceAll("1", "A")).decrypt(encryptedSmsList.get(1))).isEqualTo(Sms.toJsonArray(smsList));
  }

  @Test
  void onReceiveMixedSms() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    Intent intent = createIntent();
    Intent broadcastIntent = mock(Intent.class);
    ArgumentCaptor<String> encryptedSmsCaptor = ArgumentCaptor.forClass(String.class);
    SmsDecoder smsDecoder = mock(SmsDecoder.class);
    Sms sms1 = new Sms("number1", "text1");
    Sms sms2 = new Sms("number2", "text2");
    Sms sms3 = new Sms("number1", "text3");
    List<Sms> smsList = Arrays.asList(sms1, sms2, sms3);
    when(smsDecoder.getSmsFromIntent(phoneNumberFormatter, intent)).thenReturn(smsList);
    BiFunction<Application, String, Intent> smsBroadcastIntentFactory = broadcastIntentFactory(broadcastIntent, encryptedSmsCaptor);
    SmsListener smsListener = new SmsListener(smsDecoder, dao, smsBroadcastIntentFactory, (context) -> phoneNumberFormatter);
    Application application1 = new Application(1L, "name1", "listener1", SECRET);
    Application application2 = new Application(2L, "name2", "listener2", SECRET.replaceAll("1", "A"));

    Map<String, Set<Application>> applicationsMap = new HashMap<>();
    applicationsMap.put("number1", Collections.singleton(application1));
    applicationsMap.put("number2", Collections.singleton(application2));
    when(dao.byPhoneNumbers(new HashSet<>(Arrays.asList("number1", "number2")))).thenReturn(applicationsMap);

    Context context = mock(Context.class);
    smsListener.onReceive(context, intent);
    verify(context, times(2)).sendOrderedBroadcast(eq(broadcastIntent), isNull());

    List<String> encryptedSmsList = encryptedSmsCaptor.getAllValues();
    verify(smsBroadcastIntentFactory).apply(application1, encryptedSmsList.get(0));
    verify(smsBroadcastIntentFactory).apply(application2, encryptedSmsList.get(1));
    assertThat(new Aes(SECRET).decrypt(encryptedSmsList.get(0))).isEqualTo(Sms.toJsonArray(Arrays.asList(sms1, sms3)));
    assertThat(new Aes(SECRET.replaceAll("1", "A")).decrypt(encryptedSmsList.get(1))).isEqualTo(Sms.toJsonArray(Collections.singletonList(sms2)));
  }

  private Intent createIntent() {
    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn("android.provider.Telephony.SMS_RECEIVED");
    return intent;
  }

  private BiFunction<Application, String, Intent> broadcastIntentFactory(Intent broadcastIntentMock, ArgumentCaptor<String> encryptedSmsCaptor) {
    //noinspection unchecked
    BiFunction<Application, String, Intent> broadcastIntentFactory = mock(BiFunction.class);
    when(broadcastIntentFactory.apply(any(), encryptedSmsCaptor.capture())).thenReturn(broadcastIntentMock);
    return broadcastIntentFactory;
  }
}