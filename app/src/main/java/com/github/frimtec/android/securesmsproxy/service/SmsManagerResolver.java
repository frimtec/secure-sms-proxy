package com.github.frimtec.android.securesmsproxy.service;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.RequiresApi;

@FunctionalInterface
interface SmsManagerResolver {

  SmsManager resolve(Context context, Integer subscriptionId);

  static SmsManagerResolver create() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
        resolverForAndroidFromS() : resolverForAndroidBeforeS();

  }

  @RequiresApi(api = Build.VERSION_CODES.S)
  static SmsManagerResolver resolverForAndroidFromS() {
    return (context, subscriptionId) -> {
      SmsManager defaultSmSManager = context.getSystemService(SmsManager.class);
      return subscriptionId == null ? defaultSmSManager :
          defaultSmSManager.createForSubscriptionId(subscriptionId);
    };
  }

  static SmsManagerResolver resolverForAndroidBeforeS() {
    return (context, subscriptionId) ->
        subscriptionId == null ? context.getSystemService(SmsManager.class) :
            // there is no alternative for this deprecated API before Android S
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
  }
}
