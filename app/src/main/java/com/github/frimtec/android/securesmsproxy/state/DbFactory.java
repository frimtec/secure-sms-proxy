package com.github.frimtec.android.securesmsproxy.state;

import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.securesmsproxy.SecureSmsProxyApplication;

import static com.github.frimtec.android.securesmsproxy.state.DbFactory.Mode.READ_ONLY;

@FunctionalInterface
public interface DbFactory {

  enum Mode {
    READ_ONLY,
    WRITABLE
  }

  static DbFactory instance() {
    return (mode -> mode == READ_ONLY ? SecureSmsProxyApplication.getReadableDatabase() : SecureSmsProxyApplication.getWritableDatabase());
  }

  SQLiteDatabase getDatabase(Mode mode);
}
