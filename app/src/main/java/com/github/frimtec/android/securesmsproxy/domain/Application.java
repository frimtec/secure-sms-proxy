package com.github.frimtec.android.securesmsproxy.domain;

import java.util.Objects;

public final class Application {

  private final long id;
  private final String name;
  private final String listener;
  private final String secret;

  public Application(long id, String name, String listener, String secret) {
    this.id = id;
    this.name = name;
    this.listener = listener;
    this.secret = secret;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
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
        ", name='" + name + '\'' +
        ", listener='" + listener + '\'' +
        ", secret='" + secret + '\'' +
        '}';
  }
}
