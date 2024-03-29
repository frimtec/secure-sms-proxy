package com.github.frimtec.android.securesmsproxyapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecureSmsProxyFacade {

  String ACTION_REGISTER = "com.github.frimtec.android.securesmsproxy.intent.action.REGISTER";
  String EXTRA_LISTENER_CLASS = "com.github.frimtec.android.securesmsproxy.intent.extra.LISTENER_CLASS";
  String EXTRA_PHONE_NUMBERS = "com.github.frimtec.android.securesmsproxy.intent.extra.PHONE_NUMBERS";
  String EXTRA_SECRET = "com.github.frimtec.android.securesmsproxy.intent.extra.SECRET";

  String S2MSP_PACKAGE_NAME = "com.github.frimtec.android.securesmsproxy";
  String ACTION_SEND_SMS = S2MSP_PACKAGE_NAME + ".SEND_SMS";
  String ACTION_BROADCAST_SMS_RECEIVED = S2MSP_PACKAGE_NAME + ".SMS_RECEIVED";

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
   * Returns whether the SMS receive and send permissions are granted for the S2MSP application.
   * <p/>
   * If the permissions are not granted, the S2MSP application is not able to send or receive SMS.
   *
   * @return true: permissions are granted; false: permissions are not granted
   */
  boolean areSmsPermissionsGranted();

  /**
   * Registration result.
   *
   * @see #getRegistrationResult(int, Intent)
   */
  class RegistrationResult {

    /**
     * Registration return code.
     */
    @SuppressWarnings("unused")
    public enum ReturnCode {
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
       *
       * @return true: success; false: error
       */
      public boolean isSuccess() {
        return success;
      }
    }

    private final ReturnCode returnCode;
    private final String secret;

    RegistrationResult(String secret) {
      this.returnCode = ReturnCode.ALLOWED;
      this.secret = secret;
    }

    RegistrationResult(ReturnCode returnCode) {
      this.returnCode = returnCode;
      this.secret = null;
    }

    /**
     * Returns the return code.
     *
     * @return return code
     */
    public ReturnCode getReturnCode() {
      return returnCode;
    }

    /**
     * Returns the secret to be used to encrypt/decrypt SMS in the exchange with the secure SMS proxy.
     *
     * @return secret or empty if registration was not successful
     */
    public Optional<String> getSecret() {
      return Optional.ofNullable(secret);
    }
  }

  /**
   * Registers the current application to be allowed to send and receive SMS to a given list of phone numbers.
   *
   * @param callerActivity            caller activity; this activity will receive the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param requestCode               request code that will be used to identify the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param phoneNumbersToAllow       list of phone numbers to get approved
   * @param smsBroadCastReceiverClass broad cast receiver class of the current application where received SMS will be broadcast to
   * @see #getRegistrationResult(int, Intent)
   */
  void register(Activity callerActivity, int requestCode, Set<String> phoneNumbersToAllow, Class<? extends BroadcastReceiver> smsBroadCastReceiverClass);

  /**
   * Registers the current application for communication without granting any phone numbers (no UI interactions required).
   *
   * @param callerActivity            caller activity; this activity will receive the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param requestCode               request code that will be used to identify the result in the {@link Activity#onActivityResult(int, int, Intent)} hook
   * @param smsBroadCastReceiverClass broad cast receiver class of the current application where received SMS will be broadcast to
   * @see #getRegistrationResult(int, Intent)
   * @since 1.3.5 - noop if installed S2MSP app is older
   */
  void register(Activity callerActivity, int requestCode, Class<? extends BroadcastReceiver> smsBroadCastReceiverClass);

  /**
   * Returns registration result from result code and data intent that your activity will receive
   * with the {@link Activity#onActivityResult(int, int, Intent)} hook upon result.
   *
   * @param resultCode result code from {@code onActivityResult}
   * @param data       data from {@code onActivityResult}
   * @return registration result
   * @see #register(Activity, int, Set, Class)
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


  /**
   * Installation information.
   *
   * @see #getInstallation()
   */
  @SuppressWarnings("unused")
  class Installation {

    public enum AppCompatibility {
      NOT_YET_SUPPORTED(false),
      UPDATE_RECOMMENDED(true),
      SUPPORTED(true),
      NO_MORE_SUPPORTED(false),
      NOT_INSTALLED(false);

      private final boolean supported;

      AppCompatibility(boolean supported) {
        this.supported = supported;
      }

      public boolean isSupported() {
        return supported;
      }
    }

    private final String apiVersion;
    private final String appVersion;

    private final AppCompatibility appCompatibility;

    private final Uri downloadLink;

    public Installation(
        String apiVersion,
        String appVersion,
        AppCompatibility appCompatibility,
        Uri downloadLink
    ) {
      this.apiVersion = apiVersion;
      this.appVersion = appVersion;
      this.appCompatibility = appCompatibility;
      this.downloadLink = downloadLink;
    }

    /**
     * Returns the version of the S2MSP API library.
     *
     * @return S2MSP API library version
     */
    public String getApiVersion() {
      return apiVersion;
    }

    /**
     * Returns the version of the installed S2MSP app.
     *
     * @return S2MSP app version or empty if S2MSP app is not installed
     */
    public Optional<String> getAppVersion() {
      return Optional.ofNullable(appVersion);
    }

    /**
     * Returns the compatibility between the API and the app.
     * @return compatibility
     * @since 2.8.0
     */
    public AppCompatibility getAppCompatibility() {
      return appCompatibility;
    }

    /**
     * Returns download link of the S2MSP application that fits best to the current API version.
     *
     * @return download link for the S2MSP app
     */
    public Uri getDownloadLink() {
      return downloadLink;
    }
  }

  /**
   * Returns the installation of the S2MSP application.
   * <p/>
   * For Android 11-R and higher it is required to declare the following queries section in your
   * applications {@code AndroidManifest.xml}, otherwise you will get always empty installation
   * information as the result:
   * <code>
   * <br>
   * <br>
   * &lt;manifest package="your.app"&gt;<br>
   * &nbsp;&nbsp;&lt;queries&gt;<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;package android:name="com.github.frimtec.android.securesmsproxy" /&gt;<br>
   * &nbsp;&nbsp;&lt;/queries&gt;<br>
   * &nbsp;&nbsp;...
   * </code>
   *
   * @return installation
   */
  Installation getInstallation();

  /**
   * Checks whether the current application is allowed to send/receive SMS to all of the given phone numbers or not.
   *
   * @param phoneNumbers phone numbers to check
   * @return true: all phone numbers are allowed; false: at least one of the phone numbers is not allowed
   */
  boolean isAllowed(Set<String> phoneNumbers);
}
