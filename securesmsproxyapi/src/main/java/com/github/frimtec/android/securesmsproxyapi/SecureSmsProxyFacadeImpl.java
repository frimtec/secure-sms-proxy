package com.github.frimtec.android.securesmsproxyapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.ALLOWED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.MISSING_SMS_PERMISSION;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_EXTRAS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_REFERRER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_SECRET;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.REJECTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.UNKNOWN;

final class SecureSmsProxyFacadeImpl implements SecureSmsProxyFacade {

  private static final String TAG = "SecureSmsProxyFacadeImpl";

  private static final ComponentName SECURE_SMS_PROXY_COMPONENT = new ComponentName(
      S2SMP_PACKAGE_NAME,
      S2SMP_PACKAGE_NAME + ".receiver.SmsSender"
  );

  private final Context context;

  SecureSmsProxyFacadeImpl(Context context) {
    this.context = context;
  }

  @Override
  public void register(Activity callerActivity, int requestCode, List<String> phoneNumbersToAllow, Class<? extends BroadcastReceiver> smsBroadCastReceiverClass) {
    Intent intent = new Intent(ACTION_REGISTER);
    intent.putStringArrayListExtra(EXTRA_PHONE_NUMBERS, new ArrayList<>(phoneNumbersToAllow));
    intent.putExtra(EXTRA_LISTENER_CLASS, smsBroadCastReceiverClass.getCanonicalName());
    callerActivity.startActivityForResult(intent, requestCode);
  }

  @Override
  public RegistrationResult getRegistrationResult(int resultCode, Intent data) {
    switch (resultCode) {
      case RESULT_OK:
        Bundle extras = data.getExtras();
        String secret = extras != null ? extras.getString(EXTRA_SECRET) : null;
        return secret != null ? new RegistrationResult(ALLOWED, secret) : new RegistrationResult(NO_SECRET);
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
    Intent sendSmsIntent = new Intent(ACTION_SEND_SMS);
    Aes aes = new Aes(secret);
    sendSmsIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, this.context.getApplicationContext().getPackageName());
    sendSmsIntent.putExtra(EXTRA_TEXT, aes.encrypt(sms.toJson()));
    sendSmsIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    sendSmsIntent.setComponent(SECURE_SMS_PROXY_COMPONENT);
    ComponentName componentName = context.startService(sendSmsIntent);
    Log.d(TAG, "SMS send: " + componentName);
  }

  @Override
  public List<Sms> extractReceivedSms(Intent smsReceivedIntent, String secret) {
    if (!ACTION_BROADCAST_SMS_RECEIVED.equals(smsReceivedIntent.getAction())) {
      return Collections.emptyList();
    }
    Bundle bundle = smsReceivedIntent.getExtras();
    Aes aes = new Aes(secret);
    try {
      return bundle != null ? Sms.fromJsonArray(aes.decrypt(bundle.getString(EXTRA_TEXT))) : Collections.emptyList();
    } catch (Exception e) {
      Log.e(TAG, "Cannot decrypt received message, secret must be wrong.");
      return Collections.emptyList();
    }
  }

}
