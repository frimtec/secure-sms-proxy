package com.github.frimtec.android.securesmsproxy.helper;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Aes {

  private static final String ALGORITHM_AES = "AES/CBC/PKCS5Padding";
  private final static String HEX = "0123456789ABCDEF";

  private final SecretKeySpec secretKey;
  private final IvParameterSpec parameterSpec;

  public Aes(String secret24Bytes) {
    try {
      byte[] bytes = secret24Bytes.getBytes(StandardCharsets.UTF_8);
      this.secretKey = new SecretKeySpec(bytes, "AES");
      this.parameterSpec = new IvParameterSpec("3855219456285914".getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("Cannot create AES key", e);
    }
  }

  public String encrypt(String cleartext) {
    return toHex(encrypt(cleartext.getBytes()));
  }

  public String decrypt(String encrypted) {
    return new String(decrypt(toByte(encrypted)));
  }

  private byte[] encrypt(byte[] clear) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
      return cipher.doFinal(clear);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("Cannot create encrypt", e);
    }
  }

  private byte[] decrypt(byte[] encrypted) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
      return cipher.doFinal(encrypted);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("Cannot decrypt", e);
    }
  }

  private static byte[] toByte(String hexString) {
    int len = hexString.length() / 2;
    byte[] result = new byte[len];
    for (int i = 0; i < len; i++)
      result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
          16).byteValue();
    return result;
  }

  private static String toHex(byte[] buf) {
    if (buf == null) {
      return "";
    }
    StringBuffer result = new StringBuffer(2 * buf.length);
    for (byte b : buf) {
      appendHex(result, b);
    }
    return result.toString();
  }

  private static void appendHex(StringBuffer sb, byte b) {
    sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
  }
}