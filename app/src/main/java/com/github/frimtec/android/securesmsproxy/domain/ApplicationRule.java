package com.github.frimtec.android.securesmsproxy.domain;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.Set;

public final class ApplicationRule {

  private final Application application;
  private final Set<String> allowedPhoneNumbers;

  public ApplicationRule(Application application, Set<String> allowedPhoneNumbers) {
    this.application = application;
    this.allowedPhoneNumbers = allowedPhoneNumbers;
  }

  public Application getApplication() {
    return application;
  }

  public Set<String> getAllowedPhoneNumbers() {
    return allowedPhoneNumbers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationRule that = (ApplicationRule) o;
    return application.equals(that.application);
  }

  @Override
  public int hashCode() {
    return Objects.hash(application);
  }

  @Override
  @NonNull
  public String toString() {
    return "ApplicationRule{" +
        "application=" + application +
        ", allowedPhoneNumbers=" + allowedPhoneNumbers +
        '}';
  }
}
