package com.github.frimtec.android.securesmsproxy.helper;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;

import androidx.core.app.ActivityCompat;

import com.github.frimtec.android.securesmsproxy.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxy.helper.Feature.RequestCodes.PERMISSION_CHANGED_REQUEST_CODE;

public enum Feature {
  PERMISSION_SMS(true, true, R.string.permission_sms_title, context -> allPermissionsGranted(context, PermissionSets.SMS.getPermissions()), (context, fragment) -> {
    requestPermissionsWithExplanation(context, fragment, PermissionSets.SMS.getPermissions(), R.string.permission_sms_title, R.string.permission_sms_text);
  });

  private final boolean sensitive;
  private final boolean permissionType;
  private final int nameResourceId;
  private final Function<Context, Boolean> allowed;
  private final BiConsumer<Context, Fragment> request;

  Feature(boolean sensitive, boolean permissionType, int nameResourceId, Function<Context, Boolean> allowed, BiConsumer<Context, Fragment> request) {
    this.sensitive = sensitive;
    this.permissionType = permissionType;
    this.nameResourceId = nameResourceId;
    this.allowed = allowed;
    this.request = request;
  }

  public final boolean isAllowed(Context context) {
    return this.allowed.apply(context);
  }

  public final void request(Context context, Fragment fragment) {
    request.accept(context, fragment);
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

  private static void requestPermissionsWithExplanation(Context context, Fragment fragment, String[] permissions, int titleResourceId, int textResourceId) {
    NotificationHelper.requirePermissions(context, titleResourceId, textResourceId, (dialogInterface, integer) -> requestPermissions(fragment, permissions));
  }

  private static void requestPermissions(Fragment fragment, String[] permissions) {
    ActivityCompat.requestPermissions(fragment.getActivity(), permissions, PERMISSION_CHANGED_REQUEST_CODE);
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
