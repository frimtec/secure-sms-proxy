package com.github.frimtec.android.securesmsproxy.service;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.state.DbFactory;
import com.github.frimtec.android.securesmsproxy.state.DbHelper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao.ALL_COLUMNS;
import static com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao.SECRET_LENGTH;
import static com.github.frimtec.android.securesmsproxy.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.securesmsproxy.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_ID;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.VIEW_APPLICATION_RULE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ApplicationRuleDaoTest {

  @Test
  void defaultConstructor() {
    ApplicationRuleDao dao = new ApplicationRuleDao();
    assertThat(dao).isNotNull();
  }

  @Test
  void insertOrUpdateForInsert() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase dbRead = mock(SQLiteDatabase.class);
    SQLiteDatabase dbWrite = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Collections.emptyList());
    when(dbRead.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{"app_1"}, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(dbRead);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(dbWrite);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    String secret = dao.insertOrUpdate("app_1", "listener_1", new LinkedHashSet<>(Arrays.asList("789", "000")));
    assertThat(secret.length()).isEqualTo(SECRET_LENGTH);
    verify(dbWrite).insert(eq(TABLE_APPLICATION), Mockito.isNull(), any());
    verify(dbWrite, times(2)).insert(eq(TABLE_RULE), Mockito.isNull(), any());
    verifyNoMoreInteractions(dbWrite);
  }

  @Test
  void insertOrUpdateForUpdate() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase dbRead = mock(SQLiteDatabase.class);
    SQLiteDatabase dbWrite = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Arrays.asList(
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "123"),
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "456")
    ));
    when(dbRead.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{"app_1"}, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(dbRead);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(dbWrite);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    String secret = dao.insertOrUpdate("app_1", "listener_1_changed", new LinkedHashSet<>(Arrays.asList("123", "789", "000", "111")));
    assertThat(secret).isEqualTo("secret_1");
    verify(dbWrite).update(eq(TABLE_APPLICATION), any(), eq(TABLE_APPLICATION_COLUMN_ID + "=?"), eq(new String[]{String.valueOf(1L)}));
    verify(dbWrite, times(3)).insert(eq(TABLE_RULE), Mockito.isNull(), any());
    verifyNoMoreInteractions(dbWrite);
  }

  @Test
  void byApplicationNullCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{"app_1"}, null, null, null))
        .thenReturn(null);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    ApplicationRule applicationRules = dao.byApplicationName("app_1");
    assertThat(applicationRules).isNull();
  }

  @Test
  void byApplicationEmptyCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Collections.emptyList());
    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{"app_1"}, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    ApplicationRule applicationRules = dao.byApplicationName("app_1");
    assertThat(applicationRules).isNull();
  }

  @Test
  void byApplicationName() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Arrays.asList(
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "123"),
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "456")
    ));
    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{"app_1"}, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    ApplicationRule applicationRules = dao.byApplicationName("app_1");
    assertThat(applicationRules.toString()).isEqualTo(
        "ApplicationRule{application=Application{id=1, name='app_1', listener='listener_1', secret='secret_1'}, allowedPhoneNumbers=[123, 456]}");
  }

  @Test
  void allWithNullCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, null, null, null, null, null))
        .thenReturn(null);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    List<ApplicationRule> applicationRules = dao.all();
    assertThat(applicationRules.isEmpty()).isTrue();
  }

  @Test
  void allWithEmptyCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Collections.emptyList());
    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, null, null, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    List<ApplicationRule> applicationRules = dao.all();
    assertThat(applicationRules.isEmpty()).isTrue();
  }

  @Test
  void all() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Arrays.asList(
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "123"),
        Arrays.asList(2L, "app_2", "listener_2", "secret_2", "123"),
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "456")
    ));
    when(db.query(DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS, null, null, null, null, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    List<ApplicationRule> applicationRules = dao.all();
    assertThat(applicationRules.toString()).isEqualTo(
        "[ApplicationRule{application=Application{id=1, name='app_1', listener='listener_1', secret='secret_1'}, allowedPhoneNumbers=[123, 456]}, " +
            "ApplicationRule{application=Application{id=2, name='app_2', listener='listener_2', secret='secret_2'}, allowedPhoneNumbers=[123]}]");
  }

  @Test
  void byPhoneNumbersNullCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    String rawQuery = "SELECT null FROM " + VIEW_APPLICATION_RULE + " WHERE " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER + " IN (null)";
    when(db.rawQuery(rawQuery, null)).thenReturn(null);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    Map<String, Set<Application>> applicationsByNumber = dao.byPhoneNumbers(new LinkedHashSet<>(Arrays.asList("123", "456")));
    assertThat(applicationsByNumber.size()).isEqualTo(0);
  }

  @Test
  void byPhoneNumbersEmptyCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    String rawQuery = "SELECT null FROM " + VIEW_APPLICATION_RULE + " WHERE " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER + " IN (null)";
    Cursor cursor = createCursor(Collections.emptyList());
    when(db.rawQuery(rawQuery, null)).thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    Map<String, Set<Application>> applicationsByNumber = dao.byPhoneNumbers(new LinkedHashSet<>(Arrays.asList("123", "456")));
    assertThat(applicationsByNumber.size()).isEqualTo(0);
  }

  @Test
  void byPhoneNumbers() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    Cursor cursor = createCursor(Arrays.asList(
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "123"),
        Arrays.asList(2L, "app_2", "listener_2", "secret_2", "123"),
        Arrays.asList(1L, "app_1", "listener_1", "secret_1", "456")
    ));
    String rawQuery = "SELECT null FROM " + VIEW_APPLICATION_RULE + " WHERE " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER + " IN (null)";
    when(db.rawQuery(rawQuery, null))
        .thenReturn(cursor);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    Map<String, Set<Application>> applicationsByNumber = dao.byPhoneNumbers(new LinkedHashSet<>(Arrays.asList("123", "456")));
    assertThat(applicationsByNumber.toString()).isEqualTo(
        "{123=[Application{id=1, name='app_1', listener='listener_1', secret='secret_1'}, Application{id=2, name='app_2', listener='listener_2', secret='secret_2'}], " +
            "456=[Application{id=1, name='app_1', listener='listener_1', secret='secret_1'}]}");
  }

  private Cursor createCursor(List<List<Object>> expectedApplicationRuleValues) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(!expectedApplicationRuleValues.isEmpty());
    if (!expectedApplicationRuleValues.isEmpty()) {
      Boolean[] values = new Boolean[expectedApplicationRuleValues.size()];
      Arrays.fill(values, true);
      values[values.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(expectedApplicationRuleValues.size() > 1, values);

      List<Long> ids = expectedApplicationRuleValues.stream().map(row -> (Long) row.get(0)).collect(Collectors.toList());
      when(cursor.getLong(0)).thenReturn(ids.get(0), ids.subList(1, ids.size()).toArray(new Long[0]));

      List<String> names = expectedApplicationRuleValues.stream().map(row -> (String) row.get(1)).collect(Collectors.toList());
      when(cursor.getString(1)).thenReturn(names.get(0), names.subList(1, names.size()).toArray(new String[0]));

      List<String> listeners = expectedApplicationRuleValues.stream().map(row -> (String) row.get(2)).collect(Collectors.toList());
      when(cursor.getString(2)).thenReturn(listeners.get(0), listeners.subList(1, listeners.size()).toArray(new String[0]));

      List<String> secrets = expectedApplicationRuleValues.stream().map(row -> (String) row.get(3)).collect(Collectors.toList());
      when(cursor.getString(3)).thenReturn(secrets.get(0), secrets.subList(1, secrets.size()).toArray(new String[0]));

      List<String> numbers = expectedApplicationRuleValues.stream().map(row -> (String) row.get(4)).collect(Collectors.toList());
      when(cursor.getString(4)).thenReturn(numbers.get(0), numbers.subList(1, numbers.size()).toArray(new String[0]));
    }
    return cursor;
  }

  @Test
  void delete() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    ApplicationRuleDao dao = new ApplicationRuleDao(dbFactory);

    dao.delete(new Application(1L, "name", "listener", "secret"));

    verify(db).delete(TABLE_APPLICATION, TABLE_APPLICATION_COLUMN_ID + "=?", new String[]{String.valueOf(1L)});
  }
}