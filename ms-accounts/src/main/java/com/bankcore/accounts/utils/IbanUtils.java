package com.bankcore.accounts.utils;

/**
 * Utility class for handling International Bank Account Number (IBAN) operations.
 *
 * <p>Provides helper methods to convert alphabetic characters into their numeric representation
 * according to the ISO 13616 standard, which is required for IBAN validation and MOD 97 checksum
 * calculations.
 *
 * <h2>Design Notes</h2>
 *
 * <ul>
 *   <li>This class is declared {@code final} to prevent inheritance.
 *   <li>It has a private constructor to enforce non-instantiability.
 *   <li>All methods are {@code static}, making them suitable for utility usage.
 * </ul>
 *
 * <h2>ISO 13616 Conversion Rule</h2>
 *
 * <p>Alphabetic characters are mapped to numbers as follows: A = 10, B = 11, ..., Z = 35
 *
 * @author BankcoreTeam - Sebastian Orjuela
 * @version 0.1.0
 */
public final class IbanUtils {

  /** Private constructor to prevent instantiation. */
  private IbanUtils() {}

  /**
   * Converts alphabetic characters in an IBAN string to their numeric representation.
   *
   * <p>According to ISO 13616:
   *
   * <ul>
   *   <li>A = 10
   *   <li>B = 11
   *   <li>...
   *   <li>Z = 35
   * </ul>
   *
   * @param input alphanumeric IBAN string
   * @return numeric-only string ready for MOD 97 calculation
   */
  public static String convertLettersToNumbers(String input) {
    StringBuilder result = new StringBuilder();
    for (char ch : input.toCharArray()) {
      if (Character.isLetter(ch)) {
        result.append(ch - 'A' + 10);
      } else {
        result.append(ch);
      }
    }
    return result.toString();
  }
}
