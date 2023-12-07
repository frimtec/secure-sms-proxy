package com.github.frimtec.android.securesmsproxy.service;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class PhoneNumberFormatter {

  private static final String TAG = "PhoneNumberFormatter";

  private final String networkCountryCode;

  public PhoneNumberFormatter(Context context) {
    this(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkCountryIso());
  }

  PhoneNumberFormatter(String networkCountryCode) {
    this.networkCountryCode = networkCountryCode.toUpperCase();
  }

  public String toE164(String rawNumber) {
    if (isAlphanumericShortCode(rawNumber)) {
      return rawNumber;
    }
    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    try {
      Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(rawNumber, this.networkCountryCode);
      String numberE164 = phoneUtil.format(phoneNumber, E164);
      Log.i(TAG, String.format(
          "Phone number to E164: country: %s; number: >%s< => >%s<",
          this.networkCountryCode,
          rawNumber,
          numberE164)
      );
      return numberE164;
    } catch (NumberParseException e) {
      Log.e(TAG, "Cannot parse phone number", e);
      return rawNumber;
    }
  }

  public static String getFormatNumber(String phoneNumber) {
    return phoneNumber == null || isAlphanumericShortCode(phoneNumber) ?
        phoneNumber : PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().getCountry());
  }

  public static boolean isAlphanumericShortCode(String phoneNumber) {
    for (int i = 0; i < phoneNumber.length(); i++) {
      char ch = phoneNumber.charAt(i);
      if (Character.isLetter(ch)) {
        return true;
      }
    }
    return false;
  }
}
