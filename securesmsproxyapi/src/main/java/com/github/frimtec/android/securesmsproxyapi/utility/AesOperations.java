package com.github.frimtec.android.securesmsproxyapi.utility;

public interface AesOperations {
  String encrypt(String cleartext);
  String decrypt(String encrypted);
}