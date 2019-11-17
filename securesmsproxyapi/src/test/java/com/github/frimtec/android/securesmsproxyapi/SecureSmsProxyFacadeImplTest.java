package com.github.frimtec.android.securesmsproxyapi;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.S2SMP_PACKAGE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecureSmsProxyFacadeImplTest {

  @Test
  public void getInstallationForExistingProxyApp() throws PackageManager.NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = S2SMP_PACKAGE_NAME;
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
    when(contentResolver.query(Mockito.any(), Mockito.any(), Mockito.isNull(), Mockito.isNull(), Mockito.isNull())).thenReturn(cursor);
    when(context.getContentResolver()).thenReturn(contentResolver);
    when(context.getApplicationContext()).thenReturn(context);
    when(context.getPackageName()).thenReturn(clientApplication);
    return context;
  }

  private PackageManager packageManager(PackageInfo packageInfo) throws PackageManager.NameNotFoundException {
    PackageManager packageManager = mock(PackageManager.class);
    if (packageInfo != null) {
      when(packageManager.getPackageInfo(S2SMP_PACKAGE_NAME, 0)).thenReturn(packageInfo);
    } else {
      when(packageManager.getPackageInfo(S2SMP_PACKAGE_NAME, 0)).thenThrow(new PackageManager.NameNotFoundException());
    }
    return packageManager;
  }
}