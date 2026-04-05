package com.bankcore.accounts.services.complements;

import com.bankcore.accounts.utils.IbanUtils;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating valid Spanish IBAN numbers.
 *
 * <p>The generated IBAN follows the ISO 13616 standard and Spanish format:
 *
 * <pre>
 * ES + 2 check digits + 20-digit CCC
 * </pre>
 *
 * <p>The check digits are calculated using the MOD 97 algorithm:
 *
 * <ol>
 *   <li>Generate a 20-digit domestic account number (CCC).
 *   <li>Append country code and temporary check digits (00) to the end.
 *   <li>Convert letters to numbers (A=10, B=11, ..., Z=35).
 *   <li>Compute MOD 97.
 *   <li>Final check digits = 98 - (mod result).
 * </ol>
 *
 * <p>Note: This generator produces structurally valid IBANs for testing or internal systems. It
 * does not guarantee existence in a real banking network.
 *
 * @author BankCoreTeam
 * @author Sebastian Orjuela
 * @version 0.1.0
 */
@Service
public class IbanGeneratorService {

  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Generates a structurally valid Spanish IBAN (ES).
   *
   * @return a valid Spanish IBAN string (24 characters)
   */
  public String generateSpanishIban() {
    String countryCode = "ES";

    // Step 1: Generate random 20-digit domestic account number (CCC)
    String ccc = generateRandomDigits(20);

    // Step 2: Rearrange IBAN for MOD 97 calculation
    String tempIban = ccc + countryCode + "00";

    // Step 3: Convert letters to numeric representation
    String numericIban = IbanUtils.convertLettersToNumbers(tempIban);

    // Step 4: Compute MOD 97
    BigInteger bigInt = new BigInteger(numericIban);
    int mod = bigInt.mod(BigInteger.valueOf(97)).intValue();

    // Step 5: Calculate check digits
    int checkDigits = 98 - mod;

    return countryCode + String.format("%02d", checkDigits) + ccc;
  }

  /**
   * Generates a random numeric string of the specified length.
   *
   * @param length number of digits to generate
   * @return numeric string of given length
   */
  private String generateRandomDigits(int length) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append(RANDOM.nextInt(10));
    }
    return sb.toString();
  }
}
