package com.github.frimtec.android.securesmsproxy.service;

import android.database.Cursor;
import android.text.TextUtils;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.state.DbHelper;
import com.github.frimtec.android.securesmsproxy.state.SecureSmsProxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_ID;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_NAME;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_APPLICATION_COLUMN_SECRET;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER;
import static com.github.frimtec.android.securesmsproxy.state.DbHelper.VIEW_APPLICATION_RULE;

public class ApplicationRuleDao {

  public ApplicationRule byApplicationName(String applicationName) {
    try (Cursor cursor = SecureSmsProxy.getReadableDatabase().query(
        DbHelper.VIEW_APPLICATION_RULE, new String[]{TABLE_APPLICATION_COLUMN_ID, TABLE_APPLICATION_COLUMN_NAME, TABLE_APPLICATION_COLUMN_SECRET, TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER},
        TABLE_APPLICATION_COLUMN_NAME + "=?", new String[]{applicationName}, null, null, null)) {
      List<ApplicationRule> applicationRules = toApplicationRules(cursor);
      return applicationRules.isEmpty() ? null : applicationRules.get(0);
    }
  }

  public List<ApplicationRule> all() {
    try (Cursor cursor = SecureSmsProxy.getReadableDatabase().query(
        DbHelper.VIEW_APPLICATION_RULE, new String[]{TABLE_APPLICATION_COLUMN_ID, TABLE_APPLICATION_COLUMN_NAME, TABLE_APPLICATION_COLUMN_SECRET, TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER},
        null, null, null, null, null)) {
      return toApplicationRules(cursor);
    }
  }

  public Map<String, Set<Application>> byPhoneNumbers(Set<String> phoneNumbers) {
    Map<Long, Application> applications = new HashMap<>();
    Map<String, Set<Application>> applicationsByPhoneNumber = new HashMap<>();
    try (Cursor cursor = SecureSmsProxy.getReadableDatabase().rawQuery("SELECT " + TABLE_APPLICATION_COLUMN_ID + ", " + TABLE_APPLICATION_COLUMN_NAME + ", " + TABLE_APPLICATION_COLUMN_SECRET + ", " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER + " FROM " + VIEW_APPLICATION_RULE + " WHERE " + TABLE_RULE_COLUMN_ALLOWED_PHONE_NUMBER +
        " IN (" + TextUtils.join(",", phoneNumbers.stream().map(s -> "'" + s + "'").collect(Collectors.toList())) + ")", null)) {
      Application application = applications.getOrDefault(cursor.getLong(0), new Application(cursor.getLong(0), cursor.getString(1), cursor.getString(2)));
      applications.put(application.getId(), application);
      String phoneNumber = cursor.getString(3);
      Set<Application> set = applicationsByPhoneNumber.getOrDefault(phoneNumber, new HashSet<>());
      set.add(application);
      applicationsByPhoneNumber.put(phoneNumber, set);
    }
    return applicationsByPhoneNumber;
  }

  private List<ApplicationRule> toApplicationRules(Cursor cursor) {
    Map<Long, Application> applications = new HashMap<>();
    Map<Application, Set<String>> applicationPhoneNumbers = new HashMap<>();
    if (cursor != null && cursor.moveToFirst()) {
      do {
        Application application = applications.getOrDefault(cursor.getLong(0), new Application(cursor.getLong(0), cursor.getString(1), cursor.getString(2)));
        applications.put(application.getId(), application);
        Set<String> phoneNumbers = applicationPhoneNumbers.getOrDefault(application, new HashSet<>());
        phoneNumbers.add(cursor.getString(3));
        applicationPhoneNumbers.put(application, phoneNumbers);
      } while (cursor.moveToNext());
      return applications.values()
          .stream()
          .map(application -> new ApplicationRule(application, applicationPhoneNumbers.get(application)))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

}
