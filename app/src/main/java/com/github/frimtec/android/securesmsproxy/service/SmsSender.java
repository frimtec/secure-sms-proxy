package com.github.frimtec.android.securesmsproxy.service;

import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_SEND_SMS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.utility.Permission;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import java.util.Collections;

public class SmsSender extends BroadcastReceiver {

  private static final String TAG = "SmsSender";

  private final SmsManagerResolver smsManagerResolver;
  private final ApplicationRuleDao dao;

  public SmsSender() {
    this(SmsManagerResolver.create(), new ApplicationRuleDao());
  }

  SmsSender(SmsManagerResolver smsManagerResolver, ApplicationRuleDao dao) {
    super();
    this.smsManagerResolver = smsManagerResolver;
    this.dao = dao;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!ACTION_SEND_SMS.equals(intent.getAction())) {
      Log.w(TAG, "SMS sending blocked because of unrecognized intent.");
      return;
    }
    if (Permission.SMS.isForbidden(context)) {
      Log.w(TAG, "SMS sending blocked because of missing SMS permissions.");
      return;
    }

    Bundle intentExtras = intent.getExtras();
    if (intentExtras != null) {
      String text = intentExtras.getString(EXTRA_TEXT);
      if (text != null) {
        String applicationName = intentExtras.getString(Intent.EXTRA_PACKAGE_NAME);
        Log.v(TAG, "SMS to be send from application: " + applicationName);
        ApplicationRule applicationRule = this.dao.byApplicationName(applicationName);
        if (applicationRule == null) {
          Log.w(TAG, String.format("SMS sending blocked because of unregistered sender application: %s.", applicationName));
          return;
        }
        Aes aes = new Aes(applicationRule.getApplication().getSecret());
        try {
          Sms sms = Sms.fromJson(aes.decrypt(text));
          if (PHONE_NUMBER_LOOPBACK.equals(sms.getNumber())) {
            SmsListener.broadcastReceivedSms(context, applicationRule.getApplication(), Collections.singletonList(sms));
          } else {
            if (applicationRule.getAllowedPhoneNumbers().contains(sms.getNumber())) {
              send(context, sms);
            } else {
              Log.w(TAG, String.format("SMS sending blocked because of not allowed phone number %s of application %s.",
                  sms.getNumber(), applicationRule.getApplication().getName()));
            }
          }
        } catch (Exception e) {
          Log.w(TAG, "SMS cannot be decrypted, secret must be wrong.");
        }
      }
    }
  }

  void send(Context context, Sms sms) {
    smsManagerResolver.resolve(context, sms.getSubscriptionId())
        .sendTextMessage(sms.getNumber(), null, sms.getText(), null, null);
  }

}
