package com.github.frimtec.android.securesmsproxy.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.state.DbHelper;
import com.github.frimtec.android.securesmsproxyapi.IsAllowedPhoneNumberContract;

import java.util.function.Function;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static com.github.frimtec.android.securesmsproxyapi.IsAllowedPhoneNumberContract.ALLOWED_PHONE_NUMBERS_PATH;

public class IsAllowedPhoneNumberContentProvider extends ContentProvider {

  private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    URI_MATCHER.addURI(IsAllowedPhoneNumberContract.AUTHORITY, ALLOWED_PHONE_NUMBERS_PATH + "/*", 1);
  }


  private final Function<Context, SQLiteDatabase> databaseFactory;
  private final Function<Uri, Boolean> uriMatcher;
  private SQLiteDatabase db;


  public IsAllowedPhoneNumberContentProvider() {
    this((context) -> new DbHelper(context).getReadableDatabase(), (uri) -> URI_MATCHER.match(uri) == 1);
  }

  IsAllowedPhoneNumberContentProvider(Function<Context, SQLiteDatabase> databaseFactory, Function<Uri, Boolean> uriMatcher) {
    this.databaseFactory = databaseFactory;
    this.uriMatcher = uriMatcher;
  }

  @Override
  public boolean onCreate() {
    return true;
  }

  @Nullable
  public Cursor query(
      @NonNull Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {
    if (!this.uriMatcher.apply(uri)) {
      throw new IllegalArgumentException("Provided uri not supported");
    }

    if (this.db == null || !this.db.isOpen()) {
      this.db = this.databaseFactory.apply(getContext());
    }

    return this.db.query(
        DbHelper.VIEW_APPLICATION_RULE,
        new String[]{TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER},
        TABLE_APPLICATION_COLUMN_NAME + "=?",
        new String[]{uri.getLastPathSegment()},
        null, null, null
    );
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return "vnd.android.cursor.dir/vnd.com.github.frimtec.android.securesmsproxy.provider.allowed_phone_numbers";
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
