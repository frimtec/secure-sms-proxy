package com.github.frimtec.android.securesmsproxyapi.utility;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Helper to evaluate the type of a phone number.
 *
 * @since 3.3.0
 */
public enum PhoneNumberType {
  STANDARD(true, false, true),
  NUMERIC_SHORT_CODE(true, true, true),
  ALPHANUMERIC_SHORT_CODE(true, true, false),
  EMPTY(false, false, false);

  private final boolean valid;
  private final boolean shortCode;
  private final boolean sendSupport;

  PhoneNumberType(boolean valid, boolean shortCode, boolean sendSupport) {
    this.valid = valid;
    this.shortCode = shortCode;
    this.sendSupport = sendSupport;
  }

  public boolean isValid() {
    return valid;
  }

  public boolean isShortCode() {
    return shortCode;
  }

  public boolean isSendSupport() {
    return sendSupport;
  }

  /**
   * Returns the type of the given phone number.
   *
   * @param phoneNumber phone number
   * @param context context to get the country code of the telephony manager, used to detect numeric short codes
   * @return phone number type
   **/
  public static PhoneNumberType fromNumber(
      String phoneNumber,
      Context context
  ) {
    return fromNumber(phoneNumber, networkCountryIso(context));
  }

  /**
   * Returns the type of the given phone number.
   *
   * @param phoneNumber phone number
   * @param twoLetterIsoCountryCode country code, used to detect numeric short codes
   * @return phone number type
   **/
  public static PhoneNumberType fromNumber(
      String phoneNumber,
      String twoLetterIsoCountryCode
  ) {
    if (phoneNumber == null) {
      return PhoneNumberType.EMPTY;
    }
    String trimmedPhoneNumber = phoneNumber.trim();
    if (trimmedPhoneNumber.isEmpty()) {
      return PhoneNumberType.EMPTY;
    }
    if (isAlphanumericShortCode(trimmedPhoneNumber)) {
      return PhoneNumberType.ALPHANUMERIC_SHORT_CODE;
    }
    if (twoLetterIsoCountryCode != null && isNumericShortCode(trimmedPhoneNumber, twoLetterIsoCountryCode.toUpperCase())) {
      return PhoneNumberType.NUMERIC_SHORT_CODE;
    }
    return PhoneNumberType.STANDARD;
  }

  public static String networkCountryIso(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    return telephonyManager != null ? telephonyManager.getNetworkCountryIso() : "";
  }

  private static boolean isAlphanumericShortCode(String phoneNumber) {
    for (int i = 0; i < phoneNumber.length(); i++) {
      char ch = phoneNumber.charAt(i);
      if (Character.isLetter(ch)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isNumericShortCode(String phoneNumber, String countryCode) {
    return switch (countryCode) {
      case "BW" -> inLengthRange(phoneNumber, 3, 3);
      case "CL", "DK", "NP", "NZ" -> inLengthRange(phoneNumber, 3, 4);
      case "CH", "IT" -> inLengthRange(phoneNumber, 3, 5);
      case "BE", "ID", "ES", "MA", "NL", "PA", "TR" -> inLengthRange(phoneNumber, 4, 4);
      case "DE", "DO", "HU", "NG", "NO" -> inLengthRange(phoneNumber, 4, 5);
      case "BR", "FR", "GB", "GR", "SG", "SE" -> inLengthRange(phoneNumber, 5, 5);
      case "CA", "FI" -> inLengthRange(phoneNumber, 5, 6);
      case "IE", "IN" -> phoneNumber.charAt(0) == '5' && inLengthRange(phoneNumber, 5, 5);
      case "US" -> phoneNumber.charAt(0) != '1' && inLengthRange(phoneNumber, 5, 6);
      default -> false;
    };
  }

  private static boolean inLengthRange(String phoneNumber, int min, int max) {
    int length = phoneNumber.length();
    return length >= min && length <= max;
  }

}
