package com.github.frimtec.android.securesmsproxyapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.Optional;

public interface SecureSmsProxyFacade {

  String ACTION_REGISTER = "com.github.frimtec.android.securesmsproxy.intent.action.REGISTER";
  String EXTRA_LISTENER_CLASS = "com.github.frimtec.android.securesmsproxy.intent.extra.LISTENER_CLASS";
  String EXTRA_PHONE_NUMBERS = "com.github.frimtec.android.securesmsproxy.intent.extra.PHONE_NUMBERS";
  String EXTRA_SECRET = "com.github.frimtec.android.securesmsproxy.intent.extra.SECRET";

  String S2SMP_PACKAGE_NAME = "com.github.frimtec.android.securesmsproxy";
  String ACTION_SEND_SMS = S2SMP_PACKAGE_NAME + ".SEND_SMS";
  String ACTION_BROADCAST_SMS_RECEIVED = S2SMP_PACKAGE_NAME + ".SMS_RECEIVED";

  int REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION = 1;
  int REGISTRATION_RESULT_CODE_NO_REFERRER = 2;
  int REGISTRATION_RESULT_CODE_NO_EXTRAS = 3;

  String PHONE_NUMBER_LOOPBACK = "loopback";

  /**
   * Returns a instance of the facade.
   *
   * @param context context
   * @return facade
   */
  static SecureSmsProxyFacade instance(Context context) {
    return new SecureSmsProxyFacadeImpl(context);
  }

  /**
   * Registration result.
   * @see #getRegistrationResult(int, Intent)
   */
  class RegistrationResult {

    /**
     * Registration return code.
     */
    enum ReturnCode {
      ALLOWED(true),
      NO_SECRET(false),
      REJECTED(false),
      MISSING_SMS_PERMISSION(false),
      NO_REFERRER(false),
      NO_EXTRAS(false),
      UNKNOWN(false);
      private final boolean success;

      ReturnCode(boolean success) {
        this.success = success;
      }

      /**
       * Returns whether the return code is a success or an error.
       * @return true: success; false: error
       */
      public boolean isSuccess() {
        return success;
      }
    }

    private final ReturnCode returnCode;
    private final String secret;

    RegistrationResult(ReturnCode returnCode, String secret) {
      this.returnCode = returnCode;
      this.secret = secret;
    }

    RegistrationResult(ReturnCode returnCode) {
      this.returnCode = returnCode;
      this.secret = null;
    }

    /**
     * Returns the return code.
     * @return return code
     */
    public ReturnCode getReturnCode() {
      return returnCode;
    }

    /**
     * Returns the secret to be used to encrypt/decrypt SMS in the exchange with the secure SMS proxy.
     * @return secret or empty if registration was not successful
     */
    public Optional<String> getSecret() {
      return Optional.ofNullable(secret);
    }
  }

  /**
   * Registers the current application to be allowed to send and receive SMS to a given list of phone numbers.
   * @param callerActivity caller activity; this activity will receive the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param requestCode request code that will be used to identify the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param phoneNumbersToAllow list of phone numbers to get approved
   * @param smsBroadCastReceiverClass broad cast receiver class of the current application where received SMS will be broadcast to
   * @see #getRegistrationResult(int, Intent)
   */
  void register(Activity callerActivity, int requestCode, List<String> phoneNumbersToAllow, Class<? extends BroadcastReceiver> smsBroadCastReceiverClass);

  /**
   * Returns registration result from result code and data intent that your activity will receive
   * with the {@link Activity#onActivityResult(int, int, Intent)} hook upon result.
   * @param resultCode result code from {@code onActivityResult}
   * @param data data from {@code onActivityResult}
   * @return registration result
   * @see #register(Activity, int, List, Class)
   */
  RegistrationResult getRegistrationResult(int resultCode, Intent data);

  /**
   * Sends SMS.
   *
   * @param sms    sms to send
   * @param secret secret to encrypt sms
   */
  void sendSms(Sms sms, String secret);

  /**
   * Extracts received SMS from a intent.
   *
   * @param smsReceivedIntent SMS received intent
   * @param secret            secret to decrypt SMS
   * @return list of received SMS
   */
  List<Sms> extractReceivedSms(Intent smsReceivedIntent, String secret);
}
