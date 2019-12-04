package com.github.frimtec.android.securesmsproxy.utility;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PackageInfoAccessorTest {

  @Test
  public void getIconNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationIcon("package")).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    Optional<Drawable> icon = packageInfoAccessor.getIcon("package");
    assertThat(icon.isPresent(), is(false));
  }

  @Test
  public void getIcon() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    Drawable expectedIcon = mock(Drawable.class);
    when(packageManager.getApplicationIcon("package")).thenReturn(expectedIcon);
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    Optional<Drawable> icon = packageInfoAccessor.getIcon("package");
    assertThat(icon.orElse(null), is(expectedIcon));
  }

  @Test
  public void getLabelNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo("com.package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("com.package");
    assertThat(label, is("package"));
  }

  @Test
  public void getLabelNotFoundNoDots() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo("package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("package");
    assertThat(label, is("package"));
  }

  @Test
  public void getLabel() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    ApplicationInfo applicationInfo = mock(ApplicationInfo.class);
    when(packageManager.getApplicationInfo("com.package", PackageManager.GET_META_DATA)).thenReturn(applicationInfo);
    when(packageManager.getApplicationLabel(applicationInfo)).thenReturn("label");
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    CharSequence label = packageInfoAccessor.getLabel("com.package");
    assertThat(label, is("label"));
  }

  @Test
  public void isInstalledNotFound() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getPackageInfo("com.package", PackageManager.GET_META_DATA)).thenThrow(new PackageManager.NameNotFoundException());
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    boolean installed = packageInfoAccessor.isInstalled("com.package");
    assertThat(installed, is(false));
  }

  @Test
  public void isInstalled() throws PackageManager.NameNotFoundException {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);
    when(context.getPackageManager()).thenReturn(packageManager);
    PackageInfo packageInfo = mock(PackageInfo.class);
    when(packageManager.getPackageInfo("com.package", PackageManager.GET_META_DATA)).thenReturn(packageInfo);
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(context);
    boolean installed = packageInfoAccessor.isInstalled("com.package");
    assertThat(installed, is(true));
  }
}