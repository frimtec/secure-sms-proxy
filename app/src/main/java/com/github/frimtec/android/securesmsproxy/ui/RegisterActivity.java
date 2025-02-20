package com.github.frimtec.android.securesmsproxy.ui;

import static com.github.frimtec.android.securesmsproxy.utility.Permission.SMS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.ACTION_REGISTER;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_LISTENER_CLASS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_PHONE_NUMBERS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.EXTRA_SECRET;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_NO_EXTRAS;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.REGISTRATION_RESULT_CODE_NO_REFERRER;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;
import com.github.frimtec.android.securesmsproxy.service.PhoneNumberFormatter;
import com.github.frimtec.android.securesmsproxy.utility.PackageInfoAccessor;
import com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterActivity extends BaseActivity {

  @Override
  protected void doOnCreate(@Nullable Bundle savedInstanceState) {
    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(this);

    if (!ACTION_REGISTER.equals(getIntent().getAction())) {
      finish();
      return;
    }

    Intent resultIntent = new Intent();
    if (SMS.isForbidden(this)) {
      setResult(REGISTRATION_RESULT_CODE_MISSING_SMS_PERMISSION, resultIntent);
      finish();
      return;
    }

    Uri referrer = getReferrer();
    if (referrer == null) {
      setResult(REGISTRATION_RESULT_CODE_NO_REFERRER, resultIntent);
      finish();
      return;
    }
    Bundle extras = getIntent().getExtras();
    if (extras == null) {
      setResult(REGISTRATION_RESULT_CODE_NO_EXTRAS, resultIntent);
      finish();
      return;
    }

    String listener = extras.getString(EXTRA_LISTENER_CLASS);
    List<String> phoneNumbers = extras.getStringArrayList(EXTRA_PHONE_NUMBERS);
    String packageName = referrer.getHost();

    PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter(this);
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      String randomSecret = new ApplicationRuleDao().insertOrUpdate(
          packageName,
          listener,
          Collections.emptySet(),
          phoneNumberFormatter
      );
      resultIntent.putExtra(EXTRA_SECRET, randomSecret);
      setResult(RESULT_OK, resultIntent);
      finish();
      return;
    }

    setContentView(R.layout.activity_register);
    ImageView applicationIcon = findViewById(R.id.register_application_icon);
    packageInfoAccessor.getIcon(packageName).ifPresent(applicationIcon::setImageDrawable);

    TextView applicationLabel = findViewById(R.id.register_application_label);
    applicationLabel.setText(packageInfoAccessor.getLabel(packageName));

    TextView phoneNumbersToAllow = findViewById(R.id.register_phone_numbers);
    String networkCountryIso = PhoneNumberType.networkCountryIso(this);
    phoneNumbersToAllow.setText(phoneNumbers.stream()
        .map(number -> PhoneNumberFormatter.getFormattedNumber(number, networkCountryIso))
        .collect(Collectors.joining("\n")));
    Button allow = findViewById(R.id.button_allow);
    allow.setOnClickListener(v -> {
      String randomSecret = new ApplicationRuleDao().insertOrUpdate(
          packageName,
          listener,
          new HashSet<>(phoneNumbers),
          phoneNumberFormatter
      );
      resultIntent.putExtra(EXTRA_SECRET, randomSecret);
      setResult(RESULT_OK, resultIntent);
      finish();
    });
    Button reject = findViewById(R.id.button_reject);
    reject.setOnClickListener(v -> {
      setResult(RESULT_CANCELED);
      finish();
    });
  }
}
