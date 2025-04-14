package liaison.groble.common.utils;

import java.security.SecureRandom;

public class CodeGenerator {
  private static final SecureRandom secureRandom = new SecureRandom();

  public static String generateVerificationCode(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("길이는 양수여야 합니다.");
    }

    int max = (int) Math.pow(10, length);
    return String.format("%0" + length + "d", secureRandom.nextInt(max));
  }
}
