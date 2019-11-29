package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SmsHelper {

  private SmsHelper() {
  }

  public static List<Sms> getSmsFromIntent(Intent intent) {
    return getSmsFromIntent(intent, SmsMessage::createFromPdu);
  }

  static List<Sms> getSmsFromIntent(Intent intent, BiFunction<byte[], String, SmsMessage> pduDecoder) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      int subscription = bundle.getInt("subscription", -1);
      Object[] pdus = (Object[]) bundle.get("pdus");
      String format = bundle.getString("format");
      if (pdus != null) {
        Map<String, String> smsTextByNumber = new LinkedHashMap<>();
        for (Object pdu : pdus) {
          SmsMessage message = pduDecoder.apply((byte[]) pdu, format);
          String number = message.getOriginatingAddress();
          String text = smsTextByNumber.getOrDefault(number, "");
          smsTextByNumber.put(number, text + message.getMessageBody());
        }
        Integer subscriptionId = subscription >= 0 ? subscription : null;
        return smsTextByNumber.entrySet().stream()
            .map(entry -> new Sms(entry.getKey(), entry.getValue(), subscriptionId))
            .collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  public static void send(Sms sms) {
    send(sms, SmsManager.getDefault(), SmsManager::getSmsManagerForSubscriptionId);
  }

  static void send(Sms sms, SmsManager defaultSmsManager, Function<Integer, SmsManager> subscriptionSmsManagerFactory) {
    SmsManager smsManager = sms.getSubscriptionId() == null ? defaultSmsManager : subscriptionSmsManagerFactory.apply(sms.getSubscriptionId());
    smsManager.sendTextMessage(sms.getNumber(), null, sms.getText(), null, null);
  }
}
