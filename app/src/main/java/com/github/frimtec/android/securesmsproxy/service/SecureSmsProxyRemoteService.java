package com.github.frimtec.android.securesmsproxy.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxyapi.ISecureSmsProxyService;

import java.util.List;

public class SecureSmsProxyRemoteService extends Service {

  private ApplicationRuleDao dao;

  private final ISecureSmsProxyService.Stub binder = new ISecureSmsProxyService.Stub() {
    @Override
    public String version() {
      try {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        return packageInfo.versionName;
      } catch (PackageManager.NameNotFoundException e) {
        return "?.?.?";
      }
    }

    @Override
    public boolean isAllowed(String applicationName, List<String> phoneNumbers) {
      ApplicationRule applicationRule = dao.byApplicationName(applicationName);
      if (applicationRule == null) {
        return false;
      }
      return applicationRule.getAllowedPhoneNumbers().containsAll(phoneNumbers);
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    this.dao = new ApplicationRuleDao();

  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

}
