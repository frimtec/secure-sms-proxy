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
import android.os.Bundle;

import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes2;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_TEXT;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_BROADCAST_SMS_RECEIVED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_REGISTER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_SEND_SMS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_LISTENER_CLASS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_PHONE_NUMBERS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_SECRET;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility.NOT_YET_SUPPORTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility.NO_MORE_SUPPORTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility.SUPPORTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility.UPDATE_RECOMMENDED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_NO_EXTRAS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_NO_REFERRER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.ALLOWED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.MISSING_SMS_PERMISSION;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_EXTRAS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_REFERRER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.NO_SECRET;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.REJECTED;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult.ReturnCode.UNKNOWN;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.S2MSP_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecureSmsProxyFacadeImplTest {

  static final String SECRET = "1234567890123456";

  @Test
  void areSmsPermissionsGrantedForYes() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted).isTrue();
  }

  @Test
  void areSmsPermissionsGrantedForNoSend() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted).isFalse();
  }

  @Test
  void areSmsPermissionsGrantedForNoReceive() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted).isFalse();
  }

  @Test
  void areSmsPermissionsGrantedForNoSendAndNoReceive() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted).isFalse();
  }

  @Test
  void getInstallationForExistingProxyApp() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = S2MSP_PACKAGE_NAME;
    packageInfo.versionName = "3.0.1";
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(packageInfo));
    SecureSmsProxyFacade.Installation installation = facade.getInstallation();
    assertThat(installation.getApiVersion()).isEqualTo(BuildConfig.VERSION_NAME);
    assertThat(installation.getAppVersion()).isEqualTo(Optional.of("3.0.1"));
    assertThat(installation.getAppCompatibility()).isEqualTo(SUPPORTED);
  }

  @Test
  void getInstallationForNonExistingProxyApp() throws PackageManager.NameNotFoundException {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context((PackageInfo) null));
    SecureSmsProxyFacade.Installation installation = facade.getInstallation();
    assertThat(installation.getApiVersion()).isEqualTo(BuildConfig.VERSION_NAME);
    assertThat(installation.getAppVersion()).isEqualTo(Optional.empty());
    assertThat(installation.getAppCompatibility()).isEqualTo(AppCompatibility.NOT_INSTALLED);
  }

  @Test
  void detectCompatibilitySameVersionReturnsSupported() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.1", "2.0.1");
    assertThat(appCompatibility).isEqualTo(SUPPORTED);
  }

  @Test
  void detectCompatibilityDevVersionReturnsSupported1() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("$version", "$version");
    assertThat(appCompatibility).isEqualTo(SUPPORTED);
  }
  @Test
  void detectCompatibilityDevVersionReturnsSupported2() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("$version", "1.0.1");
    assertThat(appCompatibility).isEqualTo(SUPPORTED);
  }

  @Test
  void detectCompatibilityDevVersionReturnsSupported3() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("1.0.1", "$version");
    assertThat(appCompatibility).isEqualTo(SUPPORTED);
  }

  @Test
  void detectCompatibilityNoMoreSupported() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.1", "1.9.9");
    assertThat(appCompatibility).isEqualTo(NO_MORE_SUPPORTED);
  }

  @Test
  void detectCompatibilityNotYetSupported() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.1", "4.0.0");
    assertThat(appCompatibility).isEqualTo(NOT_YET_SUPPORTED);
  }

  @Test
  void detectCompatibilityUpdateRecommended() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.1", "2.0.0");
    assertThat(appCompatibility).isEqualTo(UPDATE_RECOMMENDED);
  }

  @Test
  void detectCompatibilitySupportedForNewerVersion1() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.1", "2.0.2");
    assertThat(appCompatibility).isEqualTo(SUPPORTED);
  }

  @Test
  void detectCompatibilitySupportedForNewerVersion2() {
    AppCompatibility appCompatibility = SecureSmsProxyFacadeImpl.detectCompatibility("2.0.11", "2.0.2");
    assertThat(appCompatibility).isEqualTo(UPDATE_RECOMMENDED);
  }

  @Test
  void isAllowedNotFound() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>(Arrays.asList("1", "5"))));
    boolean allowed = facade.isAllowed(new HashSet<>(Arrays.asList("111", "5")));
    assertThat(allowed).isFalse();
  }

  @Test
  void isAllowedFound() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>(Arrays.asList("1", "5", "111"))));
    boolean allowed = facade.isAllowed(new HashSet<>(Arrays.asList("111", "5")));
    assertThat(allowed).isTrue();
  }

  @Test
  void isAllowedNoResult() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    boolean allowed = facade.isAllowed(Collections.singleton("111"));
    assertThat(allowed).isFalse();
  }

  @Test
  void isAllowedNullCursor() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context((Set) null));
    boolean allowed = facade.isAllowed(Collections.singleton("111"));
    assertThat(allowed).isFalse();
  }

  private static class TestBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
  }

  @Test
  void register() {
    Intent actionIntent = mock(Intent.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(mock(Context.class), (action) -> {
      assertThat(action).isEqualTo(ACTION_REGISTER);
      return actionIntent;
    });
    Activity activity = mock(Activity.class);
    Set<String> numbersToAllow = new LinkedHashSet<>(Arrays.asList("111", "123"));
    facade.register(activity, 12, numbersToAllow, TestBroadCastReceiver.class);
    verify(activity).startActivityForResult(actionIntent, 12);
    verify(actionIntent).putStringArrayListExtra(EXTRA_PHONE_NUMBERS, new ArrayList<>(numbersToAllow));
    verify(actionIntent).putExtra(EXTRA_LISTENER_CLASS, "com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacadeImplTest.TestBroadCastReceiver");
  }

  @Test
  void registerWithNoPhoneNumbers() {
    Intent actionIntent = mock(Intent.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(mock(Context.class), (action) -> {
      assertThat(action).isEqualTo(ACTION_REGISTER);
      return actionIntent;
    });
    Activity activity = mock(Activity.class);
    facade.register(activity, 12, TestBroadCastReceiver.class);
    verify(activity).startActivityForResult(actionIntent, 12);
    verify(actionIntent).putStringArrayListExtra(EXTRA_PHONE_NUMBERS, new ArrayList<>());
    verify(actionIntent).putExtra(EXTRA_LISTENER_CLASS, "com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacadeImplTest.TestBroadCastReceiver");
  }

  @Test
  void getRegistrationResultResultCancelled() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_CANCELED, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(REJECTED);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultMissingSmsPermission() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(MISSING_SMS_PERMISSION);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultNoReferrer() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_NO_REFERRER, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(NO_REFERRER);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultNoExtras() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_NO_EXTRAS, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(NO_EXTRAS);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultUnknown() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(999, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(UNKNOWN);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultResultOkButNoExtras() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(NO_SECRET);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultResultOkButNoSecret() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    Bundle bundle = mock(Bundle.class);
    when(intent.getExtras()).thenReturn(bundle);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(NO_SECRET);
    assertThat(registrationResult.getSecret().isPresent()).isFalse();
  }

  @Test
  void getRegistrationResultResultOk() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_SECRET)).thenReturn("SECRET");
    when(intent.getExtras()).thenReturn(bundle);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode()).isEqualTo(ALLOWED);
    assertThat(registrationResult.getSecret().isPresent()).isTrue();
    assertThat(registrationResult.getSecret().get()).isEqualTo("SECRET");
  }

  @Test
  void sendSmsForUnknownAppVersion() throws PackageManager.NameNotFoundException {
    Intent actionIntent = mock(Intent.class);
    Context context = context(new PackageInfo());
    Context applicationContext = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(applicationContext);
    when(applicationContext.getPackageName()).thenReturn("application");
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context, (action) -> {
      assertThat(action).isEqualTo(ACTION_SEND_SMS);
      return actionIntent;
    });
    Sms sms = new Sms("number", "text");
    ArgumentCaptor<String> smsTextCaptor = ArgumentCaptor.forClass(String.class);
    when(actionIntent.putExtra(Mockito.eq(EXTRA_TEXT), smsTextCaptor.capture())).thenReturn(actionIntent);

    facade.sendSms(sms, SECRET);
    verify(context).sendBroadcast(actionIntent, null);
    verify(actionIntent).putExtra(Intent.EXTRA_PACKAGE_NAME, "application");
    verify(actionIntent).addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    verify(actionIntent).setComponent(any(ComponentName.class));
    assertThat(new Aes2(SECRET).decrypt(smsTextCaptor.getValue())).isEqualTo(sms.toJson());
  }

  @Test
  void extractReceivedSmsWrongAction() {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn("wrong");
    Context context = mock(Context.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes2(SECRET).encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(receivedSms.size()).isEqualTo(0);
  }

  @Test
  void extractReceivedSmsWrongEncryption() throws PackageManager.NameNotFoundException {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn(ACTION_BROADCAST_SMS_RECEIVED);
    Context context = context(new PackageInfo());
    Context applicationContext = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(applicationContext);
    when(applicationContext.getPackageName()).thenReturn("application");
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes2(SECRET.substring(1) + "A").encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(receivedSms.size()).isEqualTo(0);
  }

  @Test
  void extractReceivedSms() throws PackageManager.NameNotFoundException {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn(ACTION_BROADCAST_SMS_RECEIVED);
    Context context = context(new PackageInfo());
    Context applicationContext = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(applicationContext);
    when(applicationContext.getPackageName()).thenReturn("application");
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes2(SECRET).encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(Sms.toJsonArray(receivedSms)).isEqualTo(Sms.toJsonArray(smsList));
  }

  private Context context(PackageInfo packageInfo) throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = packageManager(packageInfo);
    when(context.getPackageManager()).thenReturn(packageManager);
    return context;
  }

  private Context context(Set<String> allowedNumbers) {
    String clientApplication = "clientApplication";
    Context context = mock(Context.class);
    ContentResolver contentResolver = mock(ContentResolver.class);
    Cursor cursor = null;
    if (allowedNumbers != null) {
      List<String> allowedNumbersList = new ArrayList<>(allowedNumbers);
      cursor = mock(Cursor.class);
      when(cursor.moveToFirst()).thenReturn(!allowedNumbers.isEmpty());
      if (!allowedNumbers.isEmpty()) {
        Boolean[] values = new Boolean[allowedNumbers.size()];
        Arrays.fill(values, true);
        values[values.length - 1] = false;
        when(cursor.moveToNext()).thenReturn(allowedNumbers.size() > 1, values);
        when(cursor.getString(0)).thenReturn(allowedNumbersList.get(0), allowedNumbersList.subList(1, allowedNumbersList.size()).toArray(new String[0]));
      }
    }
    when(contentResolver.query(any(), any(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull())).thenReturn(cursor);
    when(context.getContentResolver()).thenReturn(contentResolver);
    when(context.getApplicationContext()).thenReturn(context);
    when(context.getPackageName()).thenReturn(clientApplication);
    when(context.getPackageManager()).thenReturn(mock(PackageManager.class));
    return context;
  }

  private PackageManager packageManager(PackageInfo packageInfo) throws PackageManager.NameNotFoundException {
    PackageManager packageManager = mock(PackageManager.class);
    if (packageInfo != null) {
      when(packageManager.getPackageInfo(S2MSP_PACKAGE_NAME, 0)).thenReturn(packageInfo);
    } else {
      when(packageManager.getPackageInfo(S2MSP_PACKAGE_NAME, 0)).thenThrow(new PackageManager.NameNotFoundException());
    }
    return packageManager;
  }
}