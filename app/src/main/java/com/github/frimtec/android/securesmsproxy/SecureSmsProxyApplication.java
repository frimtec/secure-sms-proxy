package com.github.frimtec.android.securesmsproxy;

import static android.content.Intent.EXTRA_BUG_REPORT;
import static com.github.frimtec.android.securesmsproxy.ui.SendLogActivity.ACTION_SEND_LOG;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.state.DbHelper;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SecureSmsProxyApplication extends Application {

  private static final String TAG = "SecureSmsProxyApplication";

  private static DbHelper dbHelper;

  public static SQLiteDatabase getWritableDatabase() {
    return dbHelper.getWritableDatabase();
  }

  public static SQLiteDatabase getReadableDatabase() {
    return dbHelper.getReadableDatabase();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    dbHelper = new DbHelper(this);
    try (SQLiteDatabase writableDatabase = getWritableDatabase()) {
      writableDatabase.execSQL("PRAGMA foreign_keys=ON;");
    }
  }

  private void handleUncaughtException(Thread thread, Throwable e) {
    Log.e(TAG, "Unhandled exception occurred", e);
    Intent intent = new Intent();
    intent.setAction(ACTION_SEND_LOG);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(EXTRA_BUG_REPORT, createReport(thread, e));
    startActivity(intent);
    Process.killProcess(Process.myPid());
  }

  private String createReport(Thread thread, Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    String model = Build.MODEL;
    if (!model.startsWith(Build.MANUFACTURER)) {
      model = Build.MANUFACTURER + " " + model;
    }

    PackageManager manager = this.getPackageManager();
    PackageInfo info = null;
    try {
      info = manager.getPackageInfo(this.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e2) {
      // ignore
    }

    writer.println("App name: S2MSP" + (info == null ? "" : " (" + info.packageName + ")"));
    writer.println("App version: " + (info == null ? "NOT AVAILABLE" : BuildConfig.VERSION_CODE));
    writer.println("Android version: " + Build.VERSION.SDK_INT);
    writer.println("Device: " + model);
    writer.println("Thread name: " + thread.getName());
    writer.println();
    writer.println("Exception stack trace:");
    e.printStackTrace(writer);
    return stringWriter.getBuffer().toString();
  }
}
