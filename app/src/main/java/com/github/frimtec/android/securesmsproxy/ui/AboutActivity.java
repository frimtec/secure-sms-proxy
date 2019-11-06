package com.github.frimtec.android.securesmsproxy.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.securesmsproxy.BuildConfig;
import com.github.frimtec.android.securesmsproxy.R;

public class AboutActivity extends AppCompatActivity {

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
    String version = BuildConfig.VERSION_NAME;
    int build = BuildConfig.VERSION_CODE;

    textView.setText(Html.fromHtml(
        "<h2><a href='https://github.com/frimtec/secure-sms-proxy'>Secure SMS Proxy</a></h2>" +
            "<p>Version: <b>" + version + "</b><br/>" + "Build  " + build + "</p>" +
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
