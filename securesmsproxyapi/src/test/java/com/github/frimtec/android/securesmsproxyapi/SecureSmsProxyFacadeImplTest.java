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

import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import org.junit.Test;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecureSmsProxyFacadeImplTest {

  public static final String SECRET = "1234567890123456";

  @Test
  public void areSmsPermissionsGrantedForYes() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted, is(true));
  }

  @Test
  public void areSmsPermissionsGrantedForNoSend() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted, is(false));
  }

  @Test
  public void areSmsPermissionsGrantedForNoReceive() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_GRANTED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted, is(false));
  }

  @Test
  public void areSmsPermissionsGrantedForNoSendAndNoReceive() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    Context context = context(packageInfo);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    PackageManager packageManager = context.getPackageManager();
    when(packageManager.checkPermission(RECEIVE_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    when(packageManager.checkPermission(SEND_SMS, S2MSP_PACKAGE_NAME)).thenReturn(PERMISSION_DENIED);
    boolean granted = facade.areSmsPermissionsGranted();
    assertThat(granted, is(false));
  }

  @Test
  public void getInstallationForExistingProxyApp() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = S2MSP_PACKAGE_NAME;
    packageInfo.versionName = "1.0.1";
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(packageInfo));
    SecureSmsProxyFacade.Installation installation = facade.getInstallation();
    assertThat(installation.getApiVersion(), is(BuildConfig.VERSION_NAME));
    assertThat(installation.getAppVersion(), is(Optional.of("1.0.1")));
  }

  @Test
  public void getInstallationForNonExistingProxyApp() throws PackageManager.NameNotFoundException {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context((PackageInfo) null));
    SecureSmsProxyFacade.Installation installation = facade.getInstallation();
    assertThat(installation.getApiVersion(), is(BuildConfig.VERSION_NAME));
    assertThat(installation.getAppVersion(), is(Optional.empty()));
  }

  @Test
  public void isAllowedNotFound() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>(Arrays.asList("1", "5"))));
    boolean allowed = facade.isAllowed(new HashSet<>(Arrays.asList("111", "5")));
    assertThat(allowed, is(false));
  }

  @Test
  public void isAllowedFound() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>(Arrays.asList("1", "5", "111"))));
    boolean allowed = facade.isAllowed(new HashSet<>(Arrays.asList("111", "5")));
    assertThat(allowed, is(true));
  }

  @Test
  public void isAllowedNoResult() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    boolean allowed = facade.isAllowed(Collections.singleton("111"));
    assertThat(allowed, is(false));
  }

  @Test
  public void isAllowedNullCursor() {
    @SuppressWarnings("unchecked")
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context((Set) null));
    boolean allowed = facade.isAllowed(Collections.singleton("111"));
    assertThat(allowed, is(false));
  }

  private static class TestBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
  }

  @Test
  public void register() {
    Intent actionIntent = mock(Intent.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(mock(Context.class), (action) -> {
      assertThat(action, is(ACTION_REGISTER));
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
  public void getRegistrationResultResultCancelled() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_CANCELED, intent);
    assertThat(registrationResult.getReturnCode(), is(REJECTED));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultMissingSmsPermission() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION, intent);
    assertThat(registrationResult.getReturnCode(), is(MISSING_SMS_PERMISSION));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultNoReferrer() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_NO_REFERRER, intent);
    assertThat(registrationResult.getReturnCode(), is(NO_REFERRER));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultNoExtras() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(REGISTRATION_RESULT_CODE_NO_EXTRAS, intent);
    assertThat(registrationResult.getReturnCode(), is(NO_EXTRAS));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultUnknown() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(999, intent);
    assertThat(registrationResult.getReturnCode(), is(UNKNOWN));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultResultOkButNoExtras() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode(), is(NO_SECRET));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultResultOkButNoSecret() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    Bundle bundle = mock(Bundle.class);
    when(intent.getExtras()).thenReturn(bundle);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode(), is(NO_SECRET));
    assertThat(registrationResult.getSecret().isPresent(), is(false));
  }

  @Test
  public void getRegistrationResultResultOk() {
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context(new HashSet<>()));
    Intent intent = mock(Intent.class);
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_SECRET)).thenReturn("SECRET");
    when(intent.getExtras()).thenReturn(bundle);
    SecureSmsProxyFacade.RegistrationResult registrationResult = facade.getRegistrationResult(RESULT_OK, intent);
    assertThat(registrationResult.getReturnCode(), is(ALLOWED));
    assertThat(registrationResult.getSecret().isPresent(), is(true));
    assertThat(registrationResult.getSecret().get(), is("SECRET"));
  }

  @Test
  public void sendSms() {
    Intent actionIntent = mock(Intent.class);
    Context context = mock(Context.class);
    Context applicationContext = mock(Context.class);
    when(context.getApplicationContext()).thenReturn(applicationContext);
    when(applicationContext.getPackageName()).thenReturn("application");
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context, (action) -> {
      assertThat(action, is(ACTION_SEND_SMS));
      return actionIntent;
    });
    Sms sms = new Sms("number", "text");
    ArgumentCaptor<String> smsTextCaptor = ArgumentCaptor.forClass(String.class);
    when(actionIntent.putExtra(Mockito.eq(EXTRA_TEXT), smsTextCaptor.capture())).thenReturn(actionIntent);

    facade.sendSms(sms, SECRET);
    verify(context).startService(actionIntent);
    verify(actionIntent).putExtra(Intent.EXTRA_PACKAGE_NAME, "application");
    verify(actionIntent).addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    verify(actionIntent).setComponent(any(ComponentName.class));
    assertThat(new Aes(SECRET).decrypt(smsTextCaptor.getValue()), is(sms.toJson()));
  }

  @Test
  public void extractReceivedSmsWrongAction() {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn("wrong");
    Context context = mock(Context.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(receivedSms.size(), is(0));
  }

  @Test
  public void extractReceivedSmsWrongEncrypten() {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn(ACTION_BROADCAST_SMS_RECEIVED);
    Context context = mock(Context.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET.substring(1) + "A").encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(receivedSms.size(), is(0));
  }

  @Test
  public void extractReceivedSms() {
    Intent receivedIntent = mock(Intent.class);
    when(receivedIntent.getAction()).thenReturn(ACTION_BROADCAST_SMS_RECEIVED);
    Context context = mock(Context.class);
    SecureSmsProxyFacade facade = new SecureSmsProxyFacadeImpl(context);
    List<Sms> smsList = Arrays.asList(new Sms("number1", "text1"), new Sms("number2", "text2"));
    Bundle bundle = mock(Bundle.class);
    when(bundle.getString(EXTRA_TEXT)).thenReturn(new Aes(SECRET).encrypt(Sms.toJsonArray(smsList)));
    when(receivedIntent.getExtras()).thenReturn(bundle);
    List<Sms> receivedSms = facade.extractReceivedSms(receivedIntent, SECRET);
    assertThat(Sms.toJsonArray(receivedSms), is(Sms.toJsonArray(smsList)));
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