package com.github.frimtec.android.securesmsproxyapi;

import android.net.Uri;

public final class IsAllowedPhoneNumberContract {

  private IsAllowedPhoneNumberContract() {
  }

  public static final String AUTHORITY = "com.github.frimtec.android.securesmsproxy.provider";
  public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
  public static final String ALLOWED_PHONE_NUMBERS_PATH = "allowed_phone_numbers";

  public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, ALLOWED_PHONE_NUMBERS_PATH);
}
