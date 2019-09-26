package com.github.frimtec.android.securesmsproxy.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;

import com.github.frimtec.android.securesmsproxy.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxy.helper.Feature.RequestCodes.PERMISSION_CHANGED_REQUEST_CODE;

public enum Feature {
  PERMISSION_SMS(true, true, R.string.permission_sms_title, activity -> allPermissionsGranted(activity, PermissionSets.SMS.getPermissions()), (activity) -> {
    requestPermissionsWithExplanation(activity, PermissionSets.SMS.getPermissions(), R.string.permission_sms_title, R.string.permission_sms_text);
  });

  private final boolean sensitive;
  private final boolean permissionType;
  private final int nameResourceId;
  private final Function<Context, Boolean> allowed;
  private final Consumer<Activity> request;

  Feature(boolean sensitive, boolean permissionType, int nameResourceId, Function<Context, Boolean> allowed, Consumer<Activity> request) {
    this.sensitive = sensitive;
    this.permissionType = permissionType;
    this.nameResourceId = nameResourceId;
    this.allowed = allowed;
    this.request = request;
  }

  public final boolean isAllowed(Context context) {
    return this.allowed.apply(context);
  }

  public final void request(Activity activity) {
    request.accept(activity);
  }

  public boolean isSensitive() {
    return sensitive;
  }

  public boolean isPermissionType() {
    return permissionType;
  }

  public int getNameResourceId() {
    return nameResourceId;
  }

  private static boolean allPermissionsGranted(Context context, String[] permissions) {
    return Stream.of(permissions)
        .noneMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

  private static void requestPermissionsWithExplanation(Activity activity, String[] permissions, int titleResourceId, int textResourceId) {
    NotificationHelper.requirePermissions(activity, titleResourceId, textResourceId, (dialogInterface, integer) -> requestPermissions(activity, permissions));
  }

  private static void requestPermissions(Activity activity, String[] permissions) {
    ActivityCompat.requestPermissions(activity, permissions, PERMISSION_CHANGED_REQUEST_CODE);
  }

  public final static class RequestCodes {

    public final static int PERMISSION_CHANGED_REQUEST_CODE = 1;
  }

  private enum PermissionSets {
    SMS(new HashSet<>(Arrays.asList(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS)));

    private final String[] permissions;

    PermissionSets(Set<String> permissions) {
      this.permissions = permissions.toArray(new String[0]);
    }

    public String[] getPermissions() {
      return permissions;
    }
  }
}
