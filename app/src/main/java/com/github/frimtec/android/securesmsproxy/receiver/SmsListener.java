package com.github.frimtec.android.securesmsproxy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.Sms;
import com.github.frimtec.android.securesmsproxy.helper.SmsHelper;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
      for (Sms sms : SmsHelper.getSmsFromIntent(intent)) {
        Log.d(TAG, "SMS received: " + sms);
      }
    }
  }

}
