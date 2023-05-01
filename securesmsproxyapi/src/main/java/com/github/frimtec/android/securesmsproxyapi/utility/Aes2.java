package com.github.frimtec.android.securesmsproxyapi.utility;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * New and stronger encryption based on AES/GCM.
 * @since 3.0.0
 */
public final class Aes2 implements AesOperations {

  public static final int GCM_TAG_LENGTH = 16;
  public static final int GCM_IV_LENGTH = 12;
  public static final String SEPARATOR = ";";
  private static final String ALGORITHM_AES = "AES/GCM/NoPadding";
  private static final String HEX = "0123456789ABCDEF";

  private final SecretKeySpec secretKey;

  public Aes2(String secret24Bytes) {
    try {
      byte[] bytes = secret24Bytes.getBytes(StandardCharsets.UTF_8);
      this.secretKey = new SecretKeySpec(bytes, "AES");
    } catch (Exception e) {
      throw new RuntimeException("Cannot create AES key", e);
    }
  }

  public String encrypt(String cleartext) {
    byte[] initVector = Random.nextBytes(GCM_IV_LENGTH);
    return toHex(
        encrypt(
            new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, initVector),
            cleartext.getBytes())
    ) + SEPARATOR + toHex(initVector);
  }

  public String decrypt(String encrypted) {
    String[] split = encrypted.split(SEPARATOR);
    if (split.length != 2) {
      throw new RuntimeException("Encrypted value in wrong format");
    }
    GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, toByte(split[1]));
    return new String(decrypt(spec, toByte(split[0])));
  }

  private byte[] encrypt(GCMParameterSpec spec, byte[] clear) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
      return cipher.doFinal(clear);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
             NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new RuntimeException("Cannot create encrypt", e);
    }
  }

  private byte[] decrypt(GCMParameterSpec spec, byte[] encrypted) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
      return cipher.doFinal(encrypted);
    } catch (BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
             NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
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