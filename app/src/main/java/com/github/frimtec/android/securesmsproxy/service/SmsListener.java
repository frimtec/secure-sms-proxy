package com.github.frimtec.android.securesmsproxy.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  public static final BiFunction<Application, String, Intent> SMS_BROADCAST_INTENT_SUPPLIER = (application, serializedSmsList) -> {
    Intent intent = new Intent(SecureSmsProxyFacade.ACTION_BROADCAST_SMS_RECEIVED);
    intent.putExtra(Intent.EXTRA_TEXT, serializedSmsList);
    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
    intent.setComponent(new ComponentName(application.getName(), application.getListener()));
    return intent;
  };

  private final SmsDecoder smsDecoder;
  private final ApplicationRuleDao dao;
  private final BiFunction<Application, String, Intent> smsBroadcastIntentFactory;

  public SmsListener() {
    this(new SmsDecoder(), new ApplicationRuleDao(), SMS_BROADCAST_INTENT_SUPPLIER);
  }

  SmsListener(SmsDecoder smsDecoder, ApplicationRuleDao dao, BiFunction<Application, String, Intent> smsBroadcastIntentFactory) {
    this.smsDecoder = smsDecoder;
    this.dao = dao;
    this.smsBroadcastIntentFactory = smsBroadcastIntentFactory;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
      Map<String, List<Sms>> smsByNumber = this.smsDecoder.getSmsFromIntent(intent).stream()
          .collect(Collectors.groupingBy(Sms::getNumber));
      Log.i(TAG, "SMS received from numbers: " + smsByNumber.keySet());
      Map<String, Set<Application>> applicationsByNumber = this.dao.byPhoneNumbers(smsByNumber.keySet());
      smsByNumber.forEach((key, value) -> {
        Set<Application> applications = applicationsByNumber.get(key);
        if (applications == null || applications.isEmpty()) {
          // no registrations for this number
          return;
        }
        applications.forEach(application -> broadcastReceivedSms(context, application, value, smsBroadcastIntentFactory));
      });
    }
  }

  public static void broadcastReceivedSms(Context context, Application application, List<Sms> smsList) {
    broadcastReceivedSms(context, application, smsList, SMS_BROADCAST_INTENT_SUPPLIER);
  }

  private static void broadcastReceivedSms(Context context, Application application, List<Sms> smsList, BiFunction<Application, String, Intent> smsBroadcastIntentFactory) {
    Log.i(TAG, "broadcastReceivedSms,  count: " + smsList.size() + "; to application: " + application.getName());
    Aes aes = new Aes(application.getSecret());
    String serializedEncryptedSmsList = aes.encrypt(Sms.toJsonArray(smsList));
    context.sendOrderedBroadcast(smsBroadcastIntentFactory.apply(application, serializedEncryptedSmsList), null);
  }

}
