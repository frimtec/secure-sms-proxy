package com.github.frimtec.android.securesmsproxy.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.domain.Sms;
import com.github.frimtec.android.securesmsproxy.helper.Aes;
import com.github.frimtec.android.securesmsproxy.helper.SmsHelper;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;

import java.util.Collections;

import static android.content.Intent.EXTRA_TEXT;

public class SmsSender extends IntentService {

  private static final String PHONE_NUMBER_LOOPBACK = "loopback";
  private static final String TAG = "SmsSender";

  public SmsSender() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null && "com.github.frimtec.android.securesmsproxy.SEND_SMS".equals(intent.getAction())) {
      Bundle intentExtras = intent.getExtras();
      if (intentExtras != null) {
        String text = intentExtras.getString(EXTRA_TEXT);
        if (text != null) {
          String applicationName = intentExtras.getString(Intent.EXTRA_PACKAGE_NAME);
          ApplicationRuleDao dao = new ApplicationRuleDao();
          ApplicationRule applicationRule = dao.byApplicationName(applicationName);
          if (applicationRule == null) {
            Log.w(TAG, "Sms send blocked for unregistered application: " + applicationName);
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
                Log.w(TAG, String.format("Sms send blocked for not allowed phone number %s of application %s", sms.getNumber(), applicationRule.getApplication().getName()));
              }
            }
          } catch (Exception e) {
            Log.w(TAG, "SMS can not be decrypted, secret must be false");
          }
        }
      }
    }
  }
}
