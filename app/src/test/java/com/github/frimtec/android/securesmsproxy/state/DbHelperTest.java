package com.github.frimtec.android.securesmsproxy.state;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.VIEW_APPLICATION_RULE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class DbHelperTest {

  @Test
  void onCreate() {
    DbHelper dbHelper = new DbHelper(mock(Context.class));
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    dbHelper.onCreate(db);
    verify(db).execSQL(Mockito.startsWith("CREATE TABLE " + TABLE_APPLICATION));
    verify(db).execSQL(Mockito.startsWith("CREATE TABLE " + TABLE_RULE));
    verify(db).execSQL(Mockito.startsWith("CREATE VIEW " + VIEW_APPLICATION_RULE));
    verifyNoMoreInteractions(db);
  }

  @Test
  void onUpgradeV1ToV1() {
    DbHelper dbHelper = new DbHelper(mock(Context.class));
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    dbHelper.onUpgrade(db, 1, 1);
    verifyNoInteractions(db);
  }
}