package com.github.frimtec.android.securesmsproxy.domain;

import java.util.Objects;
import java.util.Set;

public record ApplicationRule(Application application, Set<String> allowedPhoneNumbers) {

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

}
