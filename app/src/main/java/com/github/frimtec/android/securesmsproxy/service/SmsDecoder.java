package com.github.frimtec.android.securesmsproxy.service;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SmsDecoder {

  private static final String TAG = "SmsDecoder";

  private final BiFunction<byte[], String, SmsMessage> pduDecoder;

  public SmsDecoder() {
    this(SmsMessage::createFromPdu);
  }

  SmsDecoder(BiFunction<byte[], String, SmsMessage> pduDecoder) {
    this.pduDecoder = pduDecoder;
  }

  public List<Sms> getSmsFromIntent(String countryCode, Intent intent) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      int subscription = bundle.getInt("subscription", -1);
      Object[] pdus = (Object[]) bundle.get("pdus");
      String format = bundle.getString("format");
      if (pdus != null) {
        Map<String, String> smsTextByNumber = new LinkedHashMap<>();
        for (Object pdu : pdus) {
          SmsMessage message = this.pduDecoder.apply((byte[]) pdu, format);
          String number = formatPhoneNumber(countryCode, message.getOriginatingAddress());
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

  private static String formatPhoneNumber(String countryCode, String raw) {
    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    try {
      Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(raw, countryCode.toUpperCase());
      String formatted = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
      Log.i(TAG, String.format("Phone number formatting: country: %s; number >%s< => >%s<", countryCode, raw, formatted));
      return formatted;
    } catch (NumberParseException e) {
      Log.e(TAG, "Cannot parse phone number", e);
      return raw;
    }
  }
}
