package com.github.frimtec.android.securesmsproxy.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    readLogcat();
  }

  private void readLogcat() {
    try {
      Process process = Runtime.getRuntime().exec("logcat -t 500");
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
      Log.e(TAG, "Logcat read failed", e);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.logcat_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.logcat_clear) {
      try {
        Runtime.getRuntime().exec("logcat -c");
      } catch (IOException e) {
        Log.e(TAG, "Logcat clear failed", e);
      }
      Log.i(TAG, "Logcat cleared manually");
      readLogcat();
      return true;
    } else if (item.getItemId() == R.id.logcat_refresh) {
      readLogcat();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
