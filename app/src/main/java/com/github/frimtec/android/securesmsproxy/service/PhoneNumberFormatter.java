package com.github.frimtec.android.securesmsproxy.service;

import static com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType.STANDARD;
import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat.E164;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class PhoneNumberFormatter {

  private static final String TAG = "PhoneNumberFormatter";

  private final String networkCountryCode;

  public PhoneNumberFormatter(Context context) {
    this(PhoneNumberType.networkCountryIso(context));
  }

  PhoneNumberFormatter(String networkCountryCode) {
    this.networkCountryCode = networkCountryCode.toUpperCase();
  }

  public String toE164(String rawNumber) {
    PhoneNumberType phoneNumberType = PhoneNumberType.fromNumber(rawNumber, this.networkCountryCode);
    if (phoneNumberType != STANDARD) {
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

  public static String getFormattedNumber(String phoneNumber) {
    String defaultCountryIso = Locale.getDefault().getCountry();
    return switch (PhoneNumberType.fromNumber(phoneNumber, defaultCountryIso)) {
      case EMPTY -> phoneNumber;
      case STANDARD -> PhoneNumberUtils.formatNumber(phoneNumber, defaultCountryIso);
      case NUMERIC_SHORT_CODE, ALPHANUMERIC_SHORT_CODE -> phoneNumber.trim();
    };
  }
}