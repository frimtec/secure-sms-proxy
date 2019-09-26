package com.github.frimtec.android.securesmsproxy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;

import java.util.HashSet;
import java.util.List;

import static com.github.frimtec.android.securesmsproxy.helper.Feature.PERMISSION_SMS;

public class RegisterActivity extends AppCompatActivity {

  public static final String ACTION_REGISTER = "com.github.frimtec.android.securesmsproxy.intent.action.REGISTER";
  public static final String EXTRA_LISTEER_CLASS = "com.github.frimtec.android.securesmsproxy.intent.extra.LISTENER_CLASS";
  public static final String EXTRA_PHONE_NUMBERS = "com.github.frimtec.android.securesmsproxy.intent.extra.PHONE_NUMBERS";
  public static final String EXTRA_SECRET = "com.github.frimtec.android.securesmsproxy.intent.extra.SECRET";

  private static final int MISSING_SMS_PERMISSION = 1;
  private static final int NO_REFEERRER = 2;

  private static final String TAG = "MainActivity";

  private List<String> phoneNumbers;
  private String listenerClass;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent resultIntent = new Intent();
    if (!PERMISSION_SMS.isAllowed(this)) {
      setResult(MISSING_SMS_PERMISSION, resultIntent);
      finish();
      return;
    }

    Uri referrer = getReferrer();
    if (referrer == null) {
      setResult(NO_REFEERRER, resultIntent);
      finish();
      return;
    }

    setContentView(R.layout.activity_register);
    Button allow = findViewById(R.id.button_allow);
    allow.setOnClickListener(v -> {
      String applicationName = referrer.getHost();
      String randomSecret = new ApplicationRuleDao().insertOrUpdate(applicationName, listenerClass, new HashSet<>(phoneNumbers));
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

  @Override
  protected void onStart() {
    super.onStart();
    if (!ACTION_REGISTER.equals(getIntent().getAction())) {
      finish();
    }
    Bundle extras = getIntent().getExtras();
    this.phoneNumbers = extras.getStringArrayList(EXTRA_PHONE_NUMBERS);
    this.listenerClass = extras.getString(EXTRA_LISTEER_CLASS);
    Uri caller = getReferrer();
    Log.v(TAG, String.format("Action register [application: %s; listener; %s; phoneNumbers: %s", caller, listenerClass, phoneNumbers));
  }
}
