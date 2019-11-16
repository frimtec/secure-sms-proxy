package com.github.frimtec.android.securesmsproxyapi;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SecureSmsProxyFacadeTest {

  @Test
  public void instance() {
    Context context = mock(Context.class);
    when(context.getPackageManager()).thenReturn(mock(PackageManager.class));
    SecureSmsProxyFacade instance = SecureSmsProxyFacade.instance(context);
    assertThat(instance, notNullValue());
  }

}