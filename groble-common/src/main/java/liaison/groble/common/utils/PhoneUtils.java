package liaison.groble.common.utils;

import java.util.regex.Pattern;

public class PhoneUtils {
  private static final Pattern NON_DIGITS = Pattern.compile("\\D+");

  /**
   * 문자열에서 모든 숫자가 아닌 문자를 제거합니다.
   *
   * @param phoneNumber 입력 전화번호( null 허용 )
   * @return 숫자만 남긴 문자열( null 입력 시 null 반환 )
   */
  public static String sanitizePhoneNumber(String phoneNumber) {
    if (phoneNumber == null) return null;
    return NON_DIGITS.matcher(phoneNumber).replaceAll("");
  }
}
