package com.github.frimtec.android.securesmsproxy.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    doOnCreate(savedInstanceState);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(android.R.id.content),
        (view, windowInsets) -> {
          Insets insets = windowInsets.getInsets(
              WindowInsetsCompat.Type.systemBars() |
                  WindowInsetsCompat.Type.displayCutout()
          );
          view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        });
  }

  abstract void doOnCreate(Bundle savedInstanceState);
}
