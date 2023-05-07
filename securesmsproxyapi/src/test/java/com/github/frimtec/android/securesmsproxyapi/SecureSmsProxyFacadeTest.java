package com.github.frimtec.android.securesmsproxyapi;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility;


class SecureSmsProxyFacadeTest {

  @Test
  void instance() {
    Context context = mock(Context.class);
    when(context.getPackageManager()).thenReturn(mock(PackageManager.class));
    SecureSmsProxyFacade instance = SecureSmsProxyFacade.instance(context);
    assertThat(instance).isNotNull();
  }

  @ParameterizedTest
  @EnumSource(value = AppCompatibility.class, names = {"SUPPORTED", "UPDATE_RECOMMENDED" })
  void appCompatibilitySupported(AppCompatibility appCompatibility) {
    assertThat(appCompatibility.isSupported()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = AppCompatibility.class, names = {"SUPPORTED", "UPDATE_RECOMMENDED" }, mode = EXCLUDE)
  void appCompatibilityNotSupported(AppCompatibility appCompatibility) {
    assertThat(appCompatibility.isSupported()).isFalse();
  }

}