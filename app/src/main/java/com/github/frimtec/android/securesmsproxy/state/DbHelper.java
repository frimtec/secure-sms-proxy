package com.github.frimtec.android.securesmsproxy.state;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;


public class DbHelper extends SQLiteOpenHelper {

  public static final String TABLE_APPLICATION = "t_application";
  public static final String TABLE_APPLICATION_COLUMN_ID = "_id";
  public static final String TABLE_APPLICATION_COLUMN_NAME = "name";
  public static final String TABLE_APPLICATION_COLUMN_LISTENER = "listener";
  public static final String TABLE_APPLICATION_COLUMN_SECRET = "secret";
  public static final String TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT = "send_blocked_count";
  public static final String TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT = "loopback_count";
  public static final String TABLE_RULE = "t_rule";
  public static final String TABLE_RULE_COLUMN_ID = "_id";
  public static final String TABLE_RULE_COLUMN_APPLICATION_ID = "application_id";
  public static final String TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER = "allowed_phone_number";
  public static final String TABLE_RULE_COLUMN_SEND_COUNT = "send_count";
  public static final String TABLE_RULE_COLUMN_RECEIVE_COUNT = "receive_count";
  public static final String VIEW_APPLICATION_RULE = "v_application_rule";

  private static final String TAG = "DbHelper";
  private static final String DB_NAME = "S2MSP.db";
  private static final int DB_VERSION = 3;

  public DbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "Create DB");
    db.execSQL("CREATE TABLE " + TABLE_APPLICATION + " (" +
        "  " + TABLE_APPLICATION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  " + TABLE_APPLICATION_COLUMN_NAME + " TEXT NOT NULL," +
        "  " + TABLE_APPLICATION_COLUMN_LISTENER + " TEXT NOT NULL," +
        "  " + TABLE_APPLICATION_COLUMN_SECRET + " TEXT NOT NULL," +
        "  " + TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT + " INTEGER NOT NULL DEFAULT 0," +
        "  " + TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT + " INTEGER NOT NULL DEFAULT 0" +
        ");");
    db.execSQL("CREATE TABLE " + TABLE_RULE + " (" +
        "  " + TABLE_RULE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  " + TABLE_RULE_COLUMN_APPLICATION_ID + " INTEGER REFERENCES " + TABLE_APPLICATION + " (" + TABLE_APPLICATION_COLUMN_ID + ") ON DELETE CASCADE," +
        "  " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER + " TEXT NOT NULL," +
        "  " + TABLE_RULE_COLUMN_SEND_COUNT + " INTEGER NOT NULL DEFAULT 0," +
        "  " + TABLE_RULE_COLUMN_RECEIVE_COUNT + " INTEGER NOT NULL DEFAULT 0" +
        ");");
    createOrUpdateView(db);
  }

  private static void createOrUpdateView(SQLiteDatabase db) {
    db.execSQL("DROP VIEW IF EXISTS " + VIEW_APPLICATION_RULE);
    db.execSQL("CREATE VIEW " + VIEW_APPLICATION_RULE + " AS" +
        "  SELECT A.*, R.*" +
        "  FROM " + TABLE_APPLICATION + " A LEFT JOIN " + TABLE_RULE + " R ON R." + TABLE_RULE_COLUMN_APPLICATION_ID + "=A." + TABLE_APPLICATION_COLUMN_ID);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i(TAG, String.format("Upgrade DB from %d to %d", oldVersion, newVersion));
    if (oldVersion < 3) {
      db.execSQL("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT + " INTEGER NOT NULL DEFAULT 0");
      db.execSQL("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT + " INTEGER NOT NULL DEFAULT 0");
      db.execSQL("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_SEND_COUNT + " INTEGER NOT NULL DEFAULT 0");
      db.execSQL("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_RECEIVE_COUNT + " INTEGER NOT NULL DEFAULT 0");
      createOrUpdateView(db);
    }
  }
}
