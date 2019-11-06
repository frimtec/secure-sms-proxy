package com.github.frimtec.android.securesmsproxy.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.securesmsproxy.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class LogcatActivity extends AppCompatActivity {

  private static final String TAG = "LogcatActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logcat);
    try {
      Process process = Runtime.getRuntime().exec("logcat -d");
      BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));

      List<String> lines = new LinkedList<>();
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(0, line);
      }
      TextView tv = findViewById(R.id.log_text);
      tv.setText(TextUtils.join("\n", lines));
    } catch (IOException e) {
      Log.e(TAG, "onCreate: ", e);
    }
  }
}
