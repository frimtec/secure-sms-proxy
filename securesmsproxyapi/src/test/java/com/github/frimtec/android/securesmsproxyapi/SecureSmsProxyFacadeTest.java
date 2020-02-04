package com.github.frimtec.android.securesmsproxyapi;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SecureSmsProxyFacadeTest {

  @Test
  void instance() {
    Context context = mock(Context.class);
    when(context.getPackageManager()).thenReturn(mock(PackageManager.class));
    SecureSmsProxyFacade instance = SecureSmsProxyFacade.instance(context);
    assertThat(instance).isNotNull();
  }

}