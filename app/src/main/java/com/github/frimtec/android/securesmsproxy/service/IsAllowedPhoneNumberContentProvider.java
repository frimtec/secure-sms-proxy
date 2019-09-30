package com.github.frimtec.android.securesmsproxy.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.SecureSmsProxyApplication;
import com.github.frimtec.android.securesmsproxy.state.DbHelper;
import com.github.frimtec.android.securesmsproxyapi.IsAllowedPhoneNumberContract;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static com.github.frimtec.android.securesmsproxyapi.IsAllowedPhoneNumberContract.ALLOWED_PHONE_NUMBERS_PATH;

public class IsAllowedPhoneNumberContentProvider extends ContentProvider {

  private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    URI_MATCHER.addURI(IsAllowedPhoneNumberContract.AUTHORITY, ALLOWED_PHONE_NUMBERS_PATH + "/*", 1);
  }

  @Nullable
  public Cursor query(
      @NonNull Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    SQLiteDatabase db = SecureSmsProxyApplication.getReadableDatabase();
    if (URI_MATCHER.match(uri) != 1) {
      throw new IllegalArgumentException("Provided uri not supported");
    }
    return db.query(DbHelper.VIEW_APPLICATION_RULE, new String[]{TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER}, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{uri.getLastPathSegment()}, null, null, null);
  }

  @Override
  public boolean onCreate() {
    return false;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return "vnd.android.cursor.dir/vnd.com.example.provider.allowed_phone_numbers";
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
    throw new UnsupportedOperationException("Not supported");
  }
}
