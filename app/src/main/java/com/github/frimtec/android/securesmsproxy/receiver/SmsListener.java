package com.github.frimtec.android.securesmsproxy.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.Sms;
import com.github.frimtec.android.securesmsproxy.helper.Aes;
import com.github.frimtec.android.securesmsproxy.helper.SmsHelper;

import java.util.Collections;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
      for (Sms sms : SmsHelper.getSmsFromIntent(intent)) {
        Intent sendSmsIntent = new Intent("com.github.frimtec.android.securesmsproxy.SMS_RECEIVED");
        Aes aes = new Aes("123456789012345678901234");
        sendSmsIntent.putExtra(Intent.EXTRA_TEXT, aes.encrypt(Sms.toJsonArray(Collections.singletonList(sms))));
        sendSmsIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendSmsIntent.setComponent(new ComponentName("com.github.frimtec.android.pikettassist","com.github.frimtec.android.pikettassist.receiver.SmsListener"));
        context.sendOrderedBroadcast(sendSmsIntent, null);
        Log.d(TAG, "SMS received and send broadcast to application: " + sms);
      }
    }
  }

}
