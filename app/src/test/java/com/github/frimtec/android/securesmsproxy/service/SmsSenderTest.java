package com.github.frimtec.android.securesmsproxy.service;

import android.content.Intent;
import android.os.Bundle;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.utility.SmsHelper;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SmsSenderTest {

  public static final String SECRET = "1234567890123456";

  @Test
  public void constructor() {
    SmsSender smsSender = new SmsSender();
    assertThat(smsSender, notNullValue());
  }

  @Test
  public void onHandleIntentNullIntent() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    smsSender.onHandleIntent(null);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentBadAction() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn("BAD_ACTION");
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentNullExtras() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentNoExtraText() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(intent.getExtras()).thenReturn(bundle);
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentNoApplicationName() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(intent.getExtras()).thenReturn(bundle);
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentApplicationNameNotFound() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentBadEncryption() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn("any");
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("number")));
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentOk() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms("number", "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("number")));
    smsSender.onHandleIntent(intent);

    ArgumentCaptor<Sms> smsCaptor = ArgumentCaptor.forClass(Sms.class);
    verify(smsHelper).send(smsCaptor.capture());
    assertThat(smsCaptor.getValue().toString(), is(sms.toString()));
  }

  @Test
  public void onHandleIntentLoopback() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms(PHONE_NUMBER_LOOPBACK, "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton(PHONE_NUMBER_LOOPBACK)));
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }

  @Test
  public void onHandleIntentNotAllowedNumber() {
    ApplicationRuleDao dao = mock(ApplicationRuleDao.class);
    SmsHelper smsHelper = mock(SmsHelper.class);
    SmsSender smsSender = new SmsSender(smsHelper, dao);

    Intent intent = mock(Intent.class);
    when(intent.getAction()).thenReturn(SecureSmsProxyFacade.ACTION_SEND_SMS);
    Bundle bundle = mock(Bundle.class);
    Sms sms = new Sms("number", "text");
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(sms.toJson()));
    when(bundle.getString(Intent.EXTRA_PACKAGE_NAME)).thenReturn("application");
    when(intent.getExtras()).thenReturn(bundle);
    when(dao.byApplicationName("application")).thenReturn(new ApplicationRule(new Application(1L, "application", "listener", SECRET), Collections.singleton("otherNumber")));
    smsSender.onHandleIntent(intent);

    verifyNoInteractions(smsHelper);
  }
}