package com.github.frimtec.android.securesmsproxyapi.utility;

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

public final class Aes {

  public static final String SEPARATOR = ";";
  private static final String ALGORITHM_AES = "AES/CBC/PKCS5Padding";
  private static final String HEX = "0123456789ABCDEF";

  private final SecretKeySpec secretKey;

  public Aes(String secret24Bytes) {
    try {
      byte[] bytes = secret24Bytes.getBytes(StandardCharsets.UTF_8);
      this.secretKey = new SecretKeySpec(bytes, "AES");
    } catch (Exception e) {
      throw new RuntimeException("Cannot create AES key", e);
    }
  }

  public String encrypt(String cleartext) {
    byte[] initVector = RandomString.nextString(16).getBytes(StandardCharsets.UTF_8);
    IvParameterSpec spec = new IvParameterSpec(initVector);
    return toHex(encrypt(spec, cleartext.getBytes())) + SEPARATOR + toHex(initVector);
  }

  public String decrypt(String encrypted) {
    String[] split = encrypted.split(SEPARATOR);
    if (split.length != 2) {
      throw new RuntimeException("Encrypted value in wrong format");
    }
    return new String(decrypt(new IvParameterSpec(toByte(split[1])), toByte(split[0])));
  }

  private byte[] encrypt(IvParameterSpec spec, byte[] clear) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
      return cipher.doFinal(clear);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("Cannot create encrypt", e);
    }
  }

  private byte[] decrypt(IvParameterSpec spec, byte[] encrypted) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
      return cipher.doFinal(encrypted);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("Cannot decrypt", e);
    }
  }

  private static byte[] toByte(String hexString) {
    int len = hexString.length() / 2;
    byte[] result = new byte[len];
    for (int i = 0; i < len; i++) {
      result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
    }
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