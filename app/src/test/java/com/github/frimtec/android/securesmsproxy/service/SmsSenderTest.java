package com.github.frimtec.android.securesmsproxy.service;

import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxy.service.SmsManagerResolver.resolverForAndroidFromS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.Function;

class SmsSenderTest {

  private final Function<Context, PhoneNumberFormatter> phoneNumberFormatterProvider =
      (context) -> new PhoneNumberFormatter("ch");

  static final String SECRET = "1234567890123456";

  @Test
  void constructor() {
    SmsSender smsSender = new SmsSender();
    assertThat(smsSender).isNotNull();
  }

  @Test
  void onHandleIntentBadAction() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn("BAD_ACTION");
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentNullExtras() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentNoExtraText() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(intent.getExtras()).thenReturn(bundle);
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentNoApplicationName() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(intent.getExtras()).thenReturn(bundle);
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentApplicationNameNotFound() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentBadEncryption() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("number")));
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentOk() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManager smsManager = mock(SmsManager.class);
    SmsSender smsSender = new SmsSender((context, subscriptionId) -> smsManager, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms("number", "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("number")));
    Context context = mock(Context.class);
    when(context.getSystemService(eq(SmsManager.class))).thenReturn(smsManager);
    smsSender.onReceive(context, intent);

    verify(smsManager).sendTextMessage(eq(sms.getNumber()), isNull(), eq(sms.getText()), isNull(), isNull());
  }

  @Test
  void onHandleIntentLoopback() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms(PHONE_NUMBER_LOOPBACK, "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton(PHONE_NUMBER_LOOPBACK)));
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void onHandleIntentNotAllowedNumber() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsManagerResolver smsManagerResolver = mock(SmsManagerResolver.class);
    SmsSender smsSender = new SmsSender(smsManagerResolver, dao, this.phoneNumberFormatterProvider);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms("number", "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("otherNumber")));
    Context context = mock(Context.class);
    smsSender.onReceive(context, intent);

    verifyNoInteractions(smsManagerResolver);
  }

  @Test
  void sendWithSubscriptionId() {
    SmsManager defaultSmsManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsSender smsSender = new SmsSender(resolverForAndroidFromS(), null, this.phoneNumberFormatterProvider);
    Context context = mock(Context.class);
    when(context.getSystemService(eq(SmsManager.class))).thenReturn(defaultSmsManager);
    when(defaultSmsManager.createForSubscriptionId(1)).thenReturn(subscriptionManager);
    smsSender.send(context, 1, "number", "text");
    verify(defaultSmsManager).createForSubscriptionId(1);
    verifyNoMoreInteractions(defaultSmsManager);
    verify(subscriptionManager).sendTextMessage("number", null, "text", null, null);
  }

  @Test
  void sendWithNoSubscriptionId() {
    SmsManager defaultSmsManager = mock(SmsManager.class);
    SmsManager subscriptionManager = mock(SmsManager.class);
    SmsSender smsSender = new SmsSender(resolverForAndroidFromS(), null, this.phoneNumberFormatterProvider);
    Context context = mock(Context.class);
    when(context.getSystemService(eq(SmsManager.class))).thenReturn(defaultSmsManager);
    when(defaultSmsManager.createForSubscriptionId(1)).thenReturn(subscriptionManager);
    smsSender.send(context, null, "number", "text");
    verify(defaultSmsManager).sendTextMessage("number", null, "text", null, null);
    verifyNoInteractions(subscriptionManager);
  }
}