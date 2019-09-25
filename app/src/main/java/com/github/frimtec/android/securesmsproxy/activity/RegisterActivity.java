package com.github.frimtec.android.securesmsproxy.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.securesmsproxy.R;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

  public static final String ACTION_REGISTER = "com.github.frimtec.android.securesmsproxy.intent.action.REGISTER";
  public static final String EXTRA_PHONE_NUMBERS = "com.github.frimtec.android.securesmsproxy.intent.extra.PHONE_NUMBERS";
  public static final String EXTRA_SECRET = "com.github.frimtec.android.securesmsproxy.intent.extra.SECRET";

  private static final String TAG = "MainActivity";

  private List<String> phoneNumbers;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    Button allow = findViewById(R.id.button_allow);
    allow.setOnClickListener(v -> {
      Intent resultIntent = new Intent();
      resultIntent.putExtra(EXTRA_SECRET, "hossa");
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
    Uri caller = getReferrer();
    Log.v(TAG, String.format("Action register [application: %s; phoneNumbers: %s", caller, phoneNumbers));
  }
}
