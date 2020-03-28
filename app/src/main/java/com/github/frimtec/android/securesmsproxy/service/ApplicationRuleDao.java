package com.github.frimtec.android.securesmsproxy.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.state.DbFactory;
import com.github.frimtec.android.securesmsproxy.state.DbHelper;
import com.github.frimtec.android.securesmsproxyapi.utility.RandomString;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.securesmsproxy.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.securesmsproxy.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_ID;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_LISTENER;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_SECRET;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_APPLICATION_ID;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.VIEW_APPLICATION_RULE;

public class ApplicationRuleDao {

  static final int SECRET_LENGTH = 24;

  static final String[] ALL_COLUMNS = {
      TABLE_APPLICATION_COLUMN_ID,
      TABLE_APPLICATION_COLUMN_NAME,
      TABLE_APPLICATION_COLUMN_LISTENER,
      TABLE_APPLICATION_COLUMN_SECRET,
      TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER
  };

  private final DbFactory dbFactory;

  public ApplicationRuleDao() {
    this(DbFactory.instance());
  }

  ApplicationRuleDao(DbFactory dbFactory) {
    this.dbFactory = dbFactory;
  }

  public String insertOrUpdate(String applicationName, String listener, Set<String> allowedPhoneNumbers) {
    ApplicationRule applicationRule = byApplicationName(applicationName);
    String secret;
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    if (applicationRule != null) {
      secret = applicationRule.getApplication().getSecret();
      ContentValues applicationValues = new ContentValues();
      applicationValues.put(TABLE_APPLICATION_COLUMN_LISTENER, listener);
      db.update(TABLE_APPLICATION, applicationValues, TABLE_APPLICATION_COLUMN_ID + "=?",
          new String[]{String.valueOf(applicationRule.getApplication().getId())});
      ContentValues ruleValues = new ContentValues();
      ruleValues.put(TABLE_RULE_COLUMN_APPLICATION_ID, applicationRule.getApplication().getId());
      allowedPhoneNumbers.stream()
          .filter(phoneNumber -> !applicationRule.getAllowedPhoneNumbers().contains(phoneNumber))
          .forEach(phoneNumber -> {
            ruleValues.put(TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER, phoneNumber);
            db.insert(TABLE_RULE, null, ruleValues);
          });
    } else {
      secret = RandomString.nextString(SECRET_LENGTH);
      ContentValues values = new ContentValues();
      values.put(TABLE_APPLICATION_COLUMN_NAME, applicationName);
      values.put(TABLE_APPLICATION_COLUMN_LISTENER, listener);
      values.put(TABLE_APPLICATION_COLUMN_SECRET, secret);
      long id = db.insert(TABLE_APPLICATION, null, values);
      ContentValues ruleValues = new ContentValues();
      ruleValues.put(TABLE_RULE_COLUMN_APPLICATION_ID, id);
      allowedPhoneNumbers.forEach(phoneNumber -> {
        ruleValues.put(TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER, phoneNumber);
        db.insert(TABLE_RULE, null, ruleValues);
      });
    }
    return secret;
  }

  public ApplicationRule byApplicationName(String applicationName) {
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(
        DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS,
        TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{applicationName}, null, null, null)) {
      List<ApplicationRule> applicationRules = toApplicationRules(cursor);
      return applicationRules.isEmpty() ? null : applicationRules.get(0);
    }
  }

  public List<ApplicationRule> all() {
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(
        DbHelper.VIEW_APPLICATION_RULE, ALL_COLUMNS,
        null, null, null, null, null)) {
      return toApplicationRules(cursor);
    }
  }

  public Map<String, Set<Application>> byPhoneNumbers(Set<String> phoneNumbers) {
    Map<Long, Application> applications = new HashMap<>();
    Map<String, Set<Application>> applicationsByPhoneNumber = new HashMap<>();
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    String rawQuery = "SELECT " + TextUtils.join(", ", ALL_COLUMNS) + " FROM " + VIEW_APPLICATION_RULE + " WHERE " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER +
        " IN (" + TextUtils.join(",", phoneNumbers.stream().map(s -> "'" + s + "'").collect(Collectors.toList())) + ")";
    try (Cursor cursor = db.rawQuery(rawQuery, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          Application application = applications.getOrDefault(id, new Application(id, cursor.getString(1), cursor.getString(2), cursor.getString(3)));
          applications.put(application.getId(), application);
          String phoneNumber = cursor.getString(4);
          Set<Application> set = applicationsByPhoneNumber.getOrDefault(phoneNumber, new HashSet<>());
          set.add(application);
          applicationsByPhoneNumber.put(phoneNumber, set);
        } while (cursor.moveToNext());
      }
    }
    return applicationsByPhoneNumber;
  }

  private List<ApplicationRule> toApplicationRules(Cursor cursor) {
    Map<Long, Application> applications = new HashMap<>();
    Map<Application, Set<String>> applicationPhoneNumbers = new HashMap<>();
    if (cursor != null && cursor.moveToFirst()) {
      do {
        long id = cursor.getLong(0);
        Application application = applications.getOrDefault(id, new Application(id, cursor.getString(1), cursor.getString(2), cursor.getString(3)));
        applications.put(application.getId(), application);
        Set<String> phoneNumbers = applicationPhoneNumbers.getOrDefault(application, new HashSet<>());
        phoneNumbers.add(cursor.getString(4));
        applicationPhoneNumbers.put(application, phoneNumbers);
      } while (cursor.moveToNext());
      return applications.values()
          .stream()
          .map(application -> new ApplicationRule(application, applicationPhoneNumbers.get(application)))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public void delete(Application application) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    db.delete(TABLE_APPLICATION, TABLE_APPLICATION_COLUMN_ID + "=?", new String[]{String.valueOf(application.getId())});
  }
}
