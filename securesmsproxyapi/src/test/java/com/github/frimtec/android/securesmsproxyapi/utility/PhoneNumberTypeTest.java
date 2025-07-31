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
      "123,IT,NUMERIC_SHORT_CODE",
      "123,IT,NUMERIC_SHORT_CODE",
      "1234,IT,NUMERIC_SHORT_CODE",
      "12345,IT,NUMERIC_SHORT_CODE",
      "123456,IT,STANDARD",
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
      "123c,,ALPHANUMERIC_SHORT_CODE",
      "12,BW,STANDARD",
      "123,BW,NUMERIC_SHORT_CODE",
      "1234,BW,STANDARD",
      "12,CL,STANDARD",
      "123,CL,NUMERIC_SHORT_CODE",
      "1234,CL,NUMERIC_SHORT_CODE",
      "12345,CL,STANDARD",
      "12,DK,STANDARD",
      "123,DK,NUMERIC_SHORT_CODE",
      "1234,DK,NUMERIC_SHORT_CODE",
      "12345,DK,STANDARD",
      "12,NP,STANDARD",
      "123,NP,NUMERIC_SHORT_CODE",
      "1234,NP,NUMERIC_SHORT_CODE",
      "12345,NP,STANDARD",
      "12,NZ,STANDARD",
      "123,NZ,NUMERIC_SHORT_CODE",
      "1234,NZ,NUMERIC_SHORT_CODE",
      "12345,NZ,STANDARD",
      "123,BE,STANDARD",
      "1234,BE,NUMERIC_SHORT_CODE",
      "12345,BE,STANDARD",
      "123,ID,STANDARD",
      "1234,ID,NUMERIC_SHORT_CODE",
      "12345,ID,STANDARD",
      "123,ES,STANDARD",
      "1234,ES,NUMERIC_SHORT_CODE",
      "12345,ES,STANDARD",
      "123,MA,STANDARD",
      "1234,MA,NUMERIC_SHORT_CODE",
      "12345,MA,STANDARD",
      "123,NL,STANDARD",
      "1234,NL,NUMERIC_SHORT_CODE",
      "12345,NL,STANDARD",
      "123,PA,STANDARD",
      "1234,PA,NUMERIC_SHORT_CODE",
      "12345,PA,STANDARD",
      "123,TR,STANDARD",
      "1234,TR,NUMERIC_SHORT_CODE",
      "12345,TR,STANDARD",
      "123,DO,STANDARD",
      "1234,DO,NUMERIC_SHORT_CODE",
      "12345,DO,NUMERIC_SHORT_CODE",
      "123456,DO,STANDARD",
      "123,HU,STANDARD",
      "1234,HU,NUMERIC_SHORT_CODE",
      "12345,HU,NUMERIC_SHORT_CODE",
      "123456,HU,STANDARD",
      "123,NG,STANDARD",
      "1234,NG,NUMERIC_SHORT_CODE",
      "12345,NG,NUMERIC_SHORT_CODE",
      "123456,NG,STANDARD",
      "123,NO,STANDARD",
      "1234,NO,NUMERIC_SHORT_CODE",
      "12345,NO,NUMERIC_SHORT_CODE",
      "123456,NO,STANDARD",
      "1234,BR,STANDARD",
      "12345,BR,NUMERIC_SHORT_CODE",
      "123456,BR,STANDARD",
      "1234,GB,STANDARD",
      "12345,GB,NUMERIC_SHORT_CODE",
      "123456,GB,STANDARD",
      "1234,GR,STANDARD",
      "12345,GR,NUMERIC_SHORT_CODE",
      "123456,GR,STANDARD",
      "1234,SG,STANDARD",
      "12345,SG,NUMERIC_SHORT_CODE",
      "123456,SG,STANDARD",
      "1234,SE,STANDARD",
      "12345,SE,NUMERIC_SHORT_CODE",
      "123456,SE,STANDARD",
      "1234,CA,STANDARD",
      "12345,CA,NUMERIC_SHORT_CODE",
      "123456,CA,NUMERIC_SHORT_CODE",
      "1234567,CA,STANDARD",
      "1234,FI,STANDARD",
      "12345,FI,NUMERIC_SHORT_CODE",
      "123456,FI,NUMERIC_SHORT_CODE",
      "1234567,FI,STANDARD",
      "5234,IE,STANDARD",
      "12345,IE,STANDARD",
      "52345,IE,NUMERIC_SHORT_CODE",
      "523456,IE,STANDARD",
      "5234,IN,STANDARD",
      "12345,IN,STANDARD",
      "52345,IN,NUMERIC_SHORT_CODE",
      "523456,IN,STANDARD",
      "191234,AU,NUMERIC_SHORT_CODE",
      "19123456,AU,NUMERIC_SHORT_CODE",
      "1912345,AU,STANDARD",
      "191234567,AU,STANDARD",
      "19123,AU,STANDARD",
      "18123456,AU,STANDARD",
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