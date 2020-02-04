package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PackageInfoAccessorTest {

  @Test
  void getIconNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationIcon("package")).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    Optional<Drawable> icon = packageInfoAccessor.getIcon("package");
    assertThat(icon.isPresent()).isFalse();
  }

  @Test
  void getIcon() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    Drawable expectedIcon = mock(Drawable.class);
    when(packageManager.getApplicationIcon("package")).thenReturn(expectedIcon);
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    Optional<Drawable> icon = packageInfoAccessor.getIcon("package");
    assertThat(icon.orElse(null)).isEqualTo(expectedIcon);
  }

  @Test
  void getLabelNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo("com.package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("com.package");
    assertThat(label).isEqualTo("package");
  }

  @Test
  void getLabelNotFoundNoDots() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo("package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("package");
    assertThat(label).isEqualTo("package");
  }

  @Test
  void getLabel() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
    when(packageManager.getApplicationInfo("com.package", PackageManager.GET_META_DATA)).thenReturn(applicationInfo);
    when(packageManager.getApplicationLabel(applicationInfo)).thenReturn("label");
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("com.package");
    assertThat(label).isEqualTo("label");
  }

  @Test
  void isInstalledNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getPackageInfo("com.package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    boolean installed = packageInfoAccessor.isInstalled("com.package");
    assertThat(installed).isFalse();
  }

  @Test
  void isInstalled() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    PackageInfo packageInfo = mock(PackageInfo.class);
    when(packageManager.getPackageInfo("com.package", PackageManager.GET_META_DATA)).thenReturn(packageInfo);
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    boolean installed = packageInfoAccessor.isInstalled("com.package");
    assertThat(installed).isTrue();
  }
}