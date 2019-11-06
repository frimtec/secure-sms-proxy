package com.github.frimtec.android.securesmsproxy.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.utility.SmsHelper;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
      Log.d(TAG, "SMS received");
      Map<String, List<Sms>> smsByNumber = SmsHelper.getSmsFromIntent(intent).stream()
          .collect(Collectors.groupingBy(Sms::getNumber));
      ApplicationRuleDao dao = new ApplicationRuleDao();
      Map<String, Set<Application>> applicationsByNumber = dao.byPhoneNumbers(smsByNumber.keySet());
      smsByNumber.entrySet()
          .forEach(entry -> {
            Set<Application> applications = applicationsByNumber.get(entry.getKey());
            if (applications == null || applications.isEmpty()) {
              // no registrations for this number
              return;
            }
            applications.forEach(application -> broadcastReceivedSms(context, application, entry.getValue()));
          });
    }
  }

  public static void broadcastReceivedSms(Context context, Application application, List<Sms> smsList) {
    Log.d(TAG, "broadcastReceivedSms,  count: " + smsList.size() + "; to application: " + application.getName());
    Intent sendSmsIntent = new Intent(SecureSmsProxyFacade.ACTION_BROADCAST_SMS_RECEIVED);
    Aes aes = new Aes(application.getSecret());
    sendSmsIntent.putExtra(Intent.EXTRA_TEXT, aes.encrypt(Sms.toJsonArray(smsList)));
    sendSmsIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    sendSmsIntent.setComponent(new ComponentName(application.getName(), application.getListener()));
    context.sendOrderedBroadcast(sendSmsIntent, null);
  }

}
