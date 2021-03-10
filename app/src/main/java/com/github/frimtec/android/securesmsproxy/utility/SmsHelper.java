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

public class SmsHelper {

  private final BiFunction<byte[], String, SmsMessage> pduDecoder;
  private final SmsManager defaultSmsManager;
  private final Function<Integer, SmsManager> subscriptionSmsManagerFactory;

  public SmsHelper() {
    this(SmsMessage::createFromPdu, SmsManager.getDefault(), SmsManager::getSmsManagerForSubscriptionId);
  }

  SmsHelper(BiFunction<byte[], String, SmsMessage> pduDecoder, SmsManager defaultSmsManager, Function<Integer, SmsManager> subscriptionSmsManagerFactory) {
    this.pduDecoder = pduDecoder;
    this.defaultSmsManager = defaultSmsManager;
    this.subscriptionSmsManagerFactory = subscriptionSmsManagerFactory;
  }

  public List<Sms> getSmsFromIntent(Intent intent) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      int subscription = bundle.getInt("subscription", -1);
      Object[] pdus = (Object[]) bundle.get("pdus");
      String format = bundle.getString("format");
      if (pdus != null) {
        Map<String, String> smsTextByNumber = new LinkedHashMap<>();
        for (Object pdu : pdus) {
          SmsMessage message = this.pduDecoder.apply((byte[]) pdu, format);
          String number = message.getOriginatingAddress();
          // some carriers do not send the "+"
          if (number != null && isDigitsOnly(number)) {
            number = "+" + number;
          }
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

  public void send(Sms sms) {
    SmsManager smsManager = sms.getSubscriptionId() == null ? this.defaultSmsManager : this.subscriptionSmsManagerFactory.apply(sms.getSubscriptionId());
    smsManager.sendTextMessage(sms.getNumber(), null, sms.getText(), null, null);
  }

  private static boolean isDigitsOnly(CharSequence str) {
    final int len = str.length();
    for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
      cp = Character.codePointAt(str, i);
      if (!Character.isDigit(cp)) {
        return false;
      }
    }
    return true;
  }
}
