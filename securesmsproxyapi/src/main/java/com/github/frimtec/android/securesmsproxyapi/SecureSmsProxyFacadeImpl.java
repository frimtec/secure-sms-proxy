package com.github.frimtec.android.securesmsproxyapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxyapi.IsAllowedPhoneNumberContract.CONTENT_URI;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.MISSING_SMS_PERMISSION;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_EXTRAS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_REFERRER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_SECRET;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.REJECTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.UNKNOWN;

final class SecureSmsProxyFacadeImpl implements SecureSmsProxyFacade {

  private static final String TAG = "SecureSmsProxyFacadeImpl";

  private static final ComponentName SECURE_SMS_PROXY_COMPONENT = new ComponentName(
      S2MSP_PACKAGE_NAME,
      S2MSP_PACKAGE_NAME + ".service.SmsSender"
  );

  private final Context context;
  private final PackageManager packageManager;
  private final Function<String, Intent> actionIntentFactory;

  SecureSmsProxyFacadeImpl(Context context) {
    this(context, Intent::new);
  }

  SecureSmsProxyFacadeImpl(Context context, Function<String, Intent> actionIntentFactory) {
    this.context = context;
    this.packageManager = context.getPackageManager();
    this.actionIntentFactory = actionIntentFactory;
  }

  @Override
  public boolean areSmsPermissionsGranted() {
    PackageManager packageManager = context.getPackageManager();
    return packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME) == PERMISSION_GRANTED &&
        packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME) == PERMISSION_GRANTED;
  }

  @Override
  public void register(
      Activity callerActivity,
      int requestCode,
      Set<String> phoneNumbersToAllow,
      Class<? extends BroadcastReceiver> smsBroadCastReceiverClass) {
    Intent intent = this.actionIntentFactory.apply(ACTION_REGISTER);
    intent.putStringArrayListExtra(EXTRA_PHONE_NUMBERS, new ArrayList<>(phoneNumbersToAllow));
    intent.putExtra(EXTRA_LISTENER_CLASS, smsBroadCastReceiverClass.getCanonicalName());
    callerActivity.startActivityForResult(intent, requestCode);
  }

  @Override
  public void register(
      Activity callerActivity,
      int requestCode,
      Class<? extends BroadcastReceiver> smsBroadCastReceiverClass) {
    register(callerActivity, requestCode, Collections.emptySet(), smsBroadCastReceiverClass);
  }

  @Override
  public RegistrationResult getRegistrationResult(int resultCode, Intent data) {
    switch (resultCode) {
      case RESULT_OK:
        Bundle extras = data.getExtras();
        String secret = extras != null ? extras.getString(EXTRA_SECRET) : null;
        return secret != null ? new RegistrationResult(secret) : new RegistrationResult(NO_SECRET);
      case RESULT_CANCELED:
        return new RegistrationResult(REJECTED);
      case REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION:
        return new RegistrationResult(MISSING_SMS_PERMISSION);
      case REGISTRATION_RESULT_CODE_NO_REFERRER:
        return new RegistrationResult(NO_REFERRER);
      case REGISTRATION_RESULT_CODE_NO_EXTRAS:
        return new RegistrationResult(NO_EXTRAS);
      default:
        return new RegistrationResult(UNKNOWN);
    }
  }

  @Override
  public void sendSms(Sms sms, String secret) {
    Intent sendSmsIntent = this.actionIntentFactory.apply(ACTION_SEND_SMS);
    Aes aes = new Aes(secret);
    sendSmsIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, this.context.getApplicationContext().getPackageName());
    sendSmsIntent.putExtra(EXTRA_TEXT, aes.encrypt(sms.toJson()));
    sendSmsIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    sendSmsIntent.setComponent(SECURE_SMS_PROXY_COMPONENT);
    context.startService(sendSmsIntent);
  }

  @Override
  public List<Sms> extractReceivedSms(Intent smsReceivedIntent, String secret) {
    if (!ACTION_BROADCAST_SMS_RECEIVED.equals(smsReceivedIntent.getAction())) {
      return Collections.emptyList();
    }
    Bundle bundle = smsReceivedIntent.getExtras();
    Aes aes = new Aes(secret);
    try {
      String encryptedMessages = bundle != null ? bundle.getString(EXTRA_TEXT) : null;
      return encryptedMessages != null ? Sms.fromJsonArray(aes.decrypt(encryptedMessages)) : Collections.emptyList();
    } catch (Exception e) {
      Log.e(TAG, "Cannot decrypt received message, secret must be wrong.");
      return Collections.emptyList();
    }
  }

  @Override
  public Installation getInstallation() {
    String appVersion;
    try {
      PackageInfo packageInfo = this.packageManager.getPackageInfo(S2MSP_PACKAGE_NAME, 0);
      appVersion = packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      appVersion = null;
    }
    String apiVersion = BuildConfig.VERSION_NAME;
    return new Installation(apiVersion, appVersion, Uri.parse("https://github.com/frimtec/secure-sms-proxy/releases/download/" + apiVersion + "/app-release.apk"));
  }

  @Override
  public boolean isAllowed(Set<String> phoneNumbers) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(Uri.withAppendedPath(CONTENT_URI, this.context.getApplicationContext().getPackageName()), new String[0], null, null, null)) {
      Set<String> allowedNumbers = new HashSet<>();
      if (cursor != null && cursor.moveToFirst()) {
        do {
          allowedNumbers.add(cursor.getString(0));
        } while (cursor.moveToNext());
      }
      return allowedNumbers.containsAll(phoneNumbers);
    }
  }
}
