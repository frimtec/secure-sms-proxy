package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SmsHelper {

  private SmsHelper() {
  }

  public static List<Sms> getSmsFromIntent(Intent intent) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      int subscription = bundle.getInt("subscription", -1);
      Object[] pdus = (Object[]) bundle.get("pdus");
      if (pdus != null) {
        return Arrays.stream(pdus)
            .map(pdu -> {
              String format = bundle.getString("format");
              SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, format);
              return new Sms(message.getOriginatingAddress(), message.getMessageBody(), subscription >= 0 ? subscription : null);
            }).collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  public static void send(Sms sms) {
    SmsManager smsManager;
    if (sms.getSubscriptionId() == null) {
      smsManager = SmsManager.getDefault();
    } else {
      smsManager = SmsManager.getSmsManagerForSubscriptionId(sms.getSubscriptionId());
    }
    smsManager.sendTextMessage(sms.getNumber(), null, sms.getText(), null, null);
  }
}
