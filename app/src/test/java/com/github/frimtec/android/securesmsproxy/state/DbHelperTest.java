package com.github.frimtec.android.securesmsproxy.state;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_RECEIVE_COUNT;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_SEND_COUNT;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.VIEW_APPLICATION_RULE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DbHelperTest {

  @Test
  void onCreate() {
    try (DbHelper dbHelper = new DbHelper(mock(Context.class))) {
      SQLiteDatabase db = mock(SQLiteDatabase.class);
      dbHelper.onCreate(db);
      verify(db).execSQL(Mockito.startsWith("CREATE TABLE " + TABLE_APPLICATION));
      verify(db).execSQL(Mockito.startsWith("CREATE TABLE " + TABLE_RULE));
      verify(db).execSQL(Mockito.startsWith("CREATE UNIQUE INDEX idx_" + TABLE_APPLICATION + "_" + TABLE_APPLICATION_COLUMN_NAME));
      verify(db).execSQL(Mockito.startsWith("DROP VIEW IF EXISTS " + VIEW_APPLICATION_RULE));
      verify(db).execSQL(Mockito.startsWith("CREATE VIEW " + VIEW_APPLICATION_RULE));
      verifyNoMoreInteractions(db);
    }
  }

  @Test
  void onUpgradeV1ToV4() {
    try (DbHelper dbHelper = new DbHelper(mock(Context.class))) {
      SQLiteDatabase db = mock(SQLiteDatabase.class);
      dbHelper.onUpgrade(db, 1, 3);
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_SEND_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_RECEIVE_COUNT));
      verify(db).execSQL(Mockito.startsWith("DROP VIEW IF EXISTS " + VIEW_APPLICATION_RULE));
      verify(db).execSQL(Mockito.startsWith("CREATE VIEW " + VIEW_APPLICATION_RULE));
      verify(db).execSQL(Mockito.startsWith("DELETE FROM " + TABLE_APPLICATION));
      verify(db).execSQL(Mockito.startsWith("CREATE UNIQUE INDEX idx_" + TABLE_APPLICATION + "_" + TABLE_APPLICATION_COLUMN_NAME));
      verifyNoMoreInteractions(db);
    }
  }

  @Test
  void onUpgradeV2ToV4() {
    try (DbHelper dbHelper = new DbHelper(mock(Context.class))) {
      SQLiteDatabase db = mock(SQLiteDatabase.class);
      dbHelper.onUpgrade(db, 2, 4);
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_SEND_BLOCKED_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_APPLICATION + " ADD COLUMN " + TABLE_APPLICATION_COLUMN_LOOPBACK_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_SEND_COUNT));
      verify(db).execSQL(Mockito.startsWith("ALTER TABLE " + TABLE_RULE + " ADD COLUMN " + TABLE_RULE_COLUMN_RECEIVE_COUNT));
      verify(db).execSQL(Mockito.startsWith("DROP VIEW IF EXISTS " + VIEW_APPLICATION_RULE));
      verify(db).execSQL(Mockito.startsWith("CREATE VIEW " + VIEW_APPLICATION_RULE));
      verify(db).execSQL(Mockito.startsWith("DELETE FROM " + TABLE_APPLICATION));
      verify(db).execSQL(Mockito.startsWith("CREATE UNIQUE INDEX idx_" + TABLE_APPLICATION + "_" + TABLE_APPLICATION_COLUMN_NAME));
      verifyNoMoreInteractions(db);
    }
  }

  @Test
  void onUpgradeV3ToV4() {
    try (DbHelper dbHelper = new DbHelper(mock(Context.class))) {
      SQLiteDatabase db = mock(SQLiteDatabase.class);
      dbHelper.onUpgrade(db, 3, 4);
      verify(db).execSQL(Mockito.startsWith("DELETE FROM " + TABLE_APPLICATION));
      verify(db).execSQL(Mockito.startsWith("CREATE UNIQUE INDEX idx_" + TABLE_APPLICATION + "_" + TABLE_APPLICATION_COLUMN_NAME));
      verifyNoMoreInteractions(db);
    }
  }

  @Test
  void onUpgradeV4ToV4() {
    try (DbHelper dbHelper = new DbHelper(mock(Context.class))) {
      SQLiteDatabase db = mock(SQLiteDatabase.class);
      dbHelper.onUpgrade(db, 4, 4);
      verifyNoInteractions(db);
    }
  }
}