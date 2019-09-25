package com.github.frimtec.android.securesmsproxy.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.domain.Sms;
import com.github.frimtec.android.securesmsproxy.helper.Aes;
import com.github.frimtec.android.securesmsproxy.helper.SmsHelper;

import static android.content.Intent.EXTRA_TEXT;

public class SmsSender extends IntentService {

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
          Aes aes = new Aes("123456789012345678901234");
          Sms sms = Sms.fromJson(aes.decrypt(text));
          if ("loopback".equals(sms.getNumber())) {
            // TODO: 25.09.2019 do loopback
          } else {
            SmsHelper.send(sms);
          }
        }
      }
    }
  }
}
