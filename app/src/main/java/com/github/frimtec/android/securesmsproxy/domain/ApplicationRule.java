package com.github.frimtec.android.securesmsproxy.domain;

import java.util.Map;
import java.util.Objects;

public record ApplicationRule(Application application, Map<String, RuleStatistics> allowedPhoneNumbers) {

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
