package com.github.frimtec.android.securesmsproxy.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.frimtec.android.securesmsproxy.R;

import java.util.function.BiConsumer;


public class AlertDialogHelper {

  public static void requirePermissions(Context context, int titleResourceId, int textResourceId, BiConsumer<DialogInterface, Integer> action) {
    requireFeature(context, context.getString(R.string.permission_required) + " " + context.getString(titleResourceId), textResourceId, action);
  }

  public static void requireFeature(Context context, int titleResourceId, int textResourceId, BiConsumer<DialogInterface, Integer> action) {
    requireFeature(context, context.getString(titleResourceId), textResourceId, action);
  }

  private static void requireFeature(Context context, String title, int textResourceId, BiConsumer<DialogInterface, Integer> action) {
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(textResourceId)
        .setCancelable(true)
        .setPositiveButton("OK", action::accept)
        .create();
    alertDialog.show();
  }

  public static void areYouSure(Context context, DialogInterface.OnClickListener onYes, DialogInterface.OnClickListener onNo) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(R.string.general_are_you_sure)
        .setPositiveButton(R.string.general_yes, onYes)
        .setNegativeButton(R.string.general_no, onNo)
        .show();
  }
}
