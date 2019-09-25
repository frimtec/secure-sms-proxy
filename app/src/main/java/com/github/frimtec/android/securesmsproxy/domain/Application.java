package com.github.frimtec.android.securesmsproxy.domain;

import java.util.Objects;

public final class Application {

  private final long id;
  private final String applicationName;
  private final String secret;

  public Application(long id, String applicationName, String secret) {
    this.id = id;
    this.applicationName = applicationName;
    this.secret = secret;
  }

  public long getId() {
    return id;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getSecret() {
    return secret;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Application that = (Application) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ApplicationRule{" +
        "id=" + id +
        ", applicationName='" + applicationName + '\'' +
        ", secret='" + secret + '\'' +
        '}';
  }
}
