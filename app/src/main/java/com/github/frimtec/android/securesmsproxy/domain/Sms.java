package com.github.frimtec.android.securesmsproxy.domain;

import androidx.annotation.NonNull;

public class Sms {

  private final String number;
  private final String text;

  public Sms(String number, String text) {
    this.number = number;
    this.text = text;
  }

  public String getNumber() {
    return number;
  }

  public String getText() {
    return text;
  }

  @NonNull
  @Override
  public String toString() {
    return "Sms{" +
        "number='" + number + '\'' +
        ", text='" + text + '\'' +
        '}';
  }
}
