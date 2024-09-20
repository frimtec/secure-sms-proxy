package com.github.frimtec.android.securesmsproxyapi.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumberTypeTest {

  @Test
  void typeEmptyAttributes() {
    PhoneNumberType type = PhoneNumberType.EMPTY;
    assertFalse(type.isValid());
    assertFalse(type.isShortCode());
    assertFalse(type.isSendSupport());
  }

  @Test
  void typeStandardAttributes() {
    PhoneNumberType type = PhoneNumberType.STANDARD;
    assertTrue(type.isValid());
    assertFalse(type.isShortCode());
    assertTrue(type.isSendSupport());
  }

  @Test
  void typeNumericShortCodeAttributes() {
    PhoneNumberType type = PhoneNumberType.NUMERIC_SHORT_CODE;
    assertTrue(type.isValid());
    assertTrue(type.isShortCode());
    assertTrue(type.isSendSupport());
  }

  @Test
  void typeAlphanumericShortCodeAttributes() {
    PhoneNumberType type = PhoneNumberType.ALPHANUMERIC_SHORT_CODE;
    assertTrue(type.isValid());
    assertTrue(type.isShortCode());
    assertFalse(type.isSendSupport());
  }

  @ParameterizedTest
  @CsvSource({
      "123,,STANDARD",
      "12,ch,STANDARD",
      "123,CH,NUMERIC_SHORT_CODE",
      "123,ch,NUMERIC_SHORT_CODE",
      "1234,ch,NUMERIC_SHORT_CODE",
      "12345,ch,NUMERIC_SHORT_CODE",
      "123456,ch,STANDARD",
      "12345,us,STANDARD",
      "2234,us,STANDARD",
      "22345,us,NUMERIC_SHORT_CODE",
      "223456,us,NUMERIC_SHORT_CODE",
      "2234567,us,STANDARD",
      "1234,fr,STANDARD",
      "12345,fr,NUMERIC_SHORT_CODE",
      "123456,fr,STANDARD",
      "123,de,STANDARD",
      "1234,de,NUMERIC_SHORT_CODE",
      "12345,de,NUMERIC_SHORT_CODE",
      "123456,de,STANDARD",
      "12345,at,STANDARD",
      "HELLO,,ALPHANUMERIC_SHORT_CODE",
      "123C,,ALPHANUMERIC_SHORT_CODE",
      "123c,,ALPHANUMERIC_SHORT_CODE"
  })
  void fromNumberForNumericShortCodesNumbers(String number, String country, String expected) {
    assertEquals(PhoneNumberType.valueOf(expected), PhoneNumberType.fromNumber(number, country));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "  ", "null"})
  void fromNumberForEmptyNumbers(String number) {
    if ("null".equals(number)) {
      number = null;
    }
    assertEquals(PhoneNumberType.EMPTY, PhoneNumberType.fromNumber(number, (String) null));
  }

}