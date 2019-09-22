package com.github.frimtec.android.securesmsproxy.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.securesmsproxy.R;

public class AboutActivity extends AppCompatActivity {

  private static final String TAG = "AboutActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    setupAppInfo();
    setupDocumentation();
    setupDisclaimer();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupAppInfo() {
    TextView textView = findViewById(R.id.app_info);

    String version = "N/A";
    int build = 0;
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      version = packageInfo.versionName;
      build = packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Can not read version info", e);
    }

    textView.setText(Html.fromHtml(
        "<h2><a href='https://github.com/frimtec/pikett-assist'>SecureSmsProxy</a></h2>" +
            "<p>Version: " + version + " (Build  " + build + ")</p>" +
            "<p>&copy; 2019 <a href='https://github.com/frimtec'>frimTEC</a></p>" +
            ""
        , Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void setupDocumentation() {
    TextView textView = findViewById(R.id.documentation);
    textView.setText(Html.fromHtml(getString(R.string.about_documentation), Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void setupDisclaimer() {
    TextView textView = findViewById(R.id.disclaimer);
    textView.setText(Html.fromHtml(getString(R.string.about_disclaimer), Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

}
