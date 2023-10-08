package com.github.frimtec.android.securesmsproxy.service;

import static android.content.Intent.EXTRA_TEXT;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_SEND_SMS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.utility.Permission;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.frimtec.android.securesmsproxyapi.utility.Aes2;
import com.github.frimtec.android.securesmsproxyapi.utility.AesOperations;

import java.util.Collections;
import java.util.function.Function;

public class SmsSender extends BroadcastReceiver {

  private static final String TAG = "SmsSender";

  private final SmsManagerResolver smsManagerResolver;
  private final ApplicationRuleDao dao;
  private final Function<Context, PhoneNumberFormatter> phoneNumberFormatterProvider;


  public SmsSender() {
    this(
        SmsManagerResolver.create(),
        new ApplicationRuleDao(),
        PhoneNumberFormatter::new
    );
  }

  SmsSender(
      SmsManagerResolver smsManagerResolver,
      ApplicationRuleDao dao,
      Function<Context, PhoneNumberFormatter> phoneNumberFormatterProvider
  ) {
    super();
    this.smsManagerResolver = smsManagerResolver;
    this.dao = dao;
    this.phoneNumberFormatterProvider = phoneNumberFormatterProvider;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!ACTION_SEND_SMS.equals(intent.getAction())) {
      Log.w(TAG, "SMS sending blocked because of unrecognized intent.");
      return;
    }
    if (Permission.SMS.isForbidden(context)) {
      Log.w(TAG, "SMS sending blocked because of missing SMS permissions.");
      return;
    }

    Bundle intentExtras = intent.getExtras();
    if (intentExtras != null) {
      String text = intentExtras.getString(EXTRA_TEXT);
      if (text != null) {
        String applicationName = intentExtras.getString(Intent.EXTRA_PACKAGE_NAME);
        Log.v(TAG, "SMS to be send from application: " + applicationName);
        ApplicationRule applicationRule = this.dao.byApplicationName(applicationName);
        if (applicationRule == null) {
          Log.w(TAG, String.format("SMS sending blocked because of unregistered sender application: %s.", applicationName));
          return;
        }
        AesOperations aes = new Aes2(applicationRule.application().secret());
        try {
          Sms sms = Sms.fromJson(aes.decrypt(text));
          if (PHONE_NUMBER_LOOPBACK.equals(sms.getNumber())) {
            SmsListener.broadcastReceivedSms(context, applicationRule.application(), Collections.singletonList(sms));
          } else {
            PhoneNumberFormatter phoneNumberFormatter = this.phoneNumberFormatterProvider.apply(context);
            String number = phoneNumberFormatter.toE164(sms.getNumber());
            if (applicationRule.allowedPhoneNumbers().contains(number)) {
              send(context, sms.getSubscriptionId(), number, sms.getText());
            } else {
              Log.w(TAG, String.format("SMS sending blocked because of not allowed phone number %s of application %s.",
                  number, applicationRule.application().name()));
            }
          }
        } catch (Exception e) {
          Log.w(TAG, "SMS cannot be decrypted, secret must be wrong.");
        }
      }
    }
  }

  void send(Context context, Integer subscriptionId, String number, String text) {
    smsManagerResolver.resolve(context, subscriptionId)
        .sendTextMessage(number, null, text, null, null);
  }

}
