package com.github.frimtec.android.securesmsproxy.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.Optional;

public class PackageInfoAccessor {

  private final PackageManager packageManager;

  public PackageInfoAccessor(Context context) {
    this.packageManager = context.getPackageManager();
  }

  public Optional<Drawable> getIcon(String packageName) {
    try {
      return Optional.of(this.packageManager.getApplicationIcon(packageName));
    } catch (PackageManager.NameNotFoundException e) {
      return Optional.empty();
    }
  }

  public CharSequence getLabel(String packageName) {
    try {
      return this.packageManager.getApplicationLabel(this.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
    } catch (PackageManager.NameNotFoundException e) {
      return packageName.substring(packageName.lastIndexOf("." + 1));
    }
  }

  public boolean isInstalled(String packageName) {
    try {
      packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

}
