package com.github.frimtec.android.securesmsproxy.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.github.frimtec.android.securesmsproxy.state.DbHelper;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IsAllowedPhoneNumberContentProviderTest {

  @Test
  void onCreate() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider();
    boolean onCreate = provider.onCreate();
    assertTrue(onCreate);
  }

  @Test
  void queryNotMatchingUri() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider(null, (uri) -> false);
    //noinspection resource
    assertThrows(IllegalArgumentException.class, () -> provider.query(mock(Uri.class), new String[0], null, null, null));
  }

  @Test
  void queryMatchingUri() {
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(db.isOpen()).thenReturn(false);
    when(db.isOpen()).thenReturn(true);

    AtomicInteger dbCreationCounter = new AtomicInteger(0);
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider((context -> {
      dbCreationCounter.incrementAndGet();
      return db;
    }), (uri) -> true);
    Uri uri = mock(Uri.class);
    when(uri.getLastPathSegment()).thenReturn("application");
    Cursor expectedCursor = mock(Cursor.class);
    when(db.query(
        DbHelper.VIEW_APPLICATION_RULE,
        new String[]{TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER},
        TABLE_APPLICATION_COLUMN_NAME + "=?",
        new String[]{"application"},
        null, null, null
    )).thenReturn(expectedCursor);
    Cursor cursor = provider.query(uri, new String[0], null, null, null);
    assertThat(cursor).isEqualTo(expectedCursor);
    assertThat(dbCreationCounter.get()).isEqualTo(1);

    cursor = provider.query(uri, new String[0], null, null, null);
    assertThat(cursor).isEqualTo(expectedCursor);
    assertThat(dbCreationCounter.get()).isEqualTo(1);
  }

  @Test
  void getType() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider();
    String type = provider.getType(mock(Uri.class));
    assertThat(type).isEqualTo("vnd.android.cursor.dir/vnd.com.github.frimtec.android.securesmsproxy.provider.allowed_phone_numbers");
  }

  @Test
  void insert() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider();
    assertThrows(UnsupportedOperationException.class, () -> provider.insert(mock(Uri.class), mock(ContentValues.class)));
  }

  @Test
  void delete() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider();
    assertThrows(UnsupportedOperationException.class, () -> provider.delete(mock(Uri.class), "selection", new String[0]));
  }

  @Test
  void update() {
    IsAllowedPhoneNumberContentProvider provider = new IsAllowedPhoneNumberContentProvider();
    assertThrows(UnsupportedOperationException.class, () -> provider.update(mock(Uri.class), mock(ContentValues.class), "selection", new String[0]));
  }
}