package com.github.frimtec.android.securesmsproxy.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PhoneNumberFormatterTest {

  private final PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter("ch");

  @Test
  void toE164ForNonNumber() {
    // act
    String e164 = phoneNumberFormatter.toE164("text");

    // assert
    assertThat(e164).isEqualTo("text");
  }

  @Test
  void toE164ForE164Number() {
    // act
    String e164 = phoneNumberFormatter.toE164("+41791231212");

    // assert
    assertThat(e164).isEqualTo("+41791231212");
  }

  @Test
  void toE164ForLocalNumberFormatted() {
    // act
    String e164 = phoneNumberFormatter.toE164("079 123 12 12");

    // assert
    assertThat(e164).isEqualTo("+41791231212");
  }

  @Test
  void toE164ForPriorityNumber() {
    // act
    String e164 = phoneNumberFormatter.toE164("117");

    // assert
    assertThat(e164).isEqualTo("117");
  }

  @Test
  void toE164ForInternationalNumberWithoutPlus() {
    // act
    String e164 = phoneNumberFormatter.toE164("0041791231212");

    // assert
    assertThat(e164).isEqualTo("+41791231212");
  }

  @Test
  void toE164ForE164NumberMissingPlus() {
    // act
    String e164 = phoneNumberFormatter.toE164("41791231212");

    // assert
    assertThat(e164).isEqualTo("+41791231212");
  }

  @Test
  void toE164ForLocalUsNumber() {
    // arrange
    PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter("us");

    // act
    String e164 = phoneNumberFormatter.toE164("0791231212");

    // assert
    assertThat(e164).isEqualTo("+10791231212");
  }

  @Test
  void toE164ForAlphanumericShortCode() {
    // arrange
    PhoneNumberFormatter phoneNumberFormatter = this.phoneNumberFormatter;

    // act
    String e164 = phoneNumberFormatter.toE164("123On");

    // assert
    assertThat(e164).isEqualTo("123On");
  }

  @Test
  void getFormattedNumberForAlphanumericShortCodeUppercase() {
    // act
    String formatNumber = PhoneNumberFormatter.getFormattedNumber("1MSG2", "ch");

    // assert
    assertThat(formatNumber).isEqualTo("1MSG2");
  }

  @Test
  void getFormattedNumberForAlphanumericShortCodeLowercase() {
    // act
    String formatNumber = PhoneNumberFormatter.getFormattedNumber("1msg2", "ch");

    // assert
    assertThat(formatNumber).isEqualTo("1msg2");
  }

  @Test
  void getFormattedNumberForNull() {
    // act
    String formatNumber = PhoneNumberFormatter.getFormattedNumber(null, null);

    // assert
    assertThat(formatNumber).isNull();
  }
}