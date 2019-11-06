package com.github.frimtec.android.securesmsproxy.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.utility.Permission;
import com.github.frimtec.android.securesmsproxy.utility.SmsHelper;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.Collections;

import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_SEND_SMS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK;

public class SmsSender extends IntentService {

  private static final String TAG = "SmsSender";

  public SmsSender() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent == null || !ACTION_SEND_SMS.equals(intent.getAction())) {
      Log.w(TAG, "SMS sending blocked because of unrecognized intent.");
      return;
    }
    if (!Permission.SMS.isAllowed(this)) {
      Log.w(TAG, "SMS sending blocked because of missing SMS permissions.");
      return;
    }

    Bundle intentExtras = intent.getExtras();
    if (intentExtras != null) {
      String text = intentExtras.getString(EXTRA_TEXT);
      if (text != null) {
        String applicationName = intentExtras.getString(Intent.EXTRA_PACKAGE_NAME);
        Log.v(TAG, "SMS to be send from application: " + applicationName);
        ApplicationRuleDao dao = new ApplicationRuleDao();
        ApplicationRule applicationRule = dao.byApplicationName(applicationName);
        if (applicationRule == null) {
          Log.w(TAG, String.format("SMS sending blocked because of unregistered sender application: %s.", applicationName));
          return;
        }
        Aes aes = new Aes(applicationRule.getApplication().getSecret());
        try {
          Sms sms = Sms.fromJson(aes.decrypt(text));
          if (PHONE_NUMBER_LOOPBACK.equals(sms.getNumber())) {
            SmsListener.broadcastReceivedSms(this, applicationRule.getApplication(), Collections.singletonList(sms));
          } else {
            if (applicationRule.getAllowedPhoneNumbers().contains(sms.getNumber())) {
              SmsHelper.send(sms);
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
}
