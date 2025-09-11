package liaison.groble.common.utils;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * 생년월일 관련 유틸리티 클래스
 *
 * <p>생년월일 형식 변환 및 검증 기능을 제공합니다.
 */
@Slf4j
public class BirthDateUtil {

  private static final Pattern YYYYMMDD_PATTERN = Pattern.compile("^\\d{8}$");
  private static final Pattern YYMMDD_PATTERN = Pattern.compile("^\\d{6}$");

  private BirthDateUtil() {
    // Utility class
  }

  /**
   * YYYYMMDD 형태의 생년월일을 YYMMDD 6자리로 변환
   *
   * @param birthDate 8자리 생년월일 (YYYYMMDD)
   * @return 6자리 생년월일 (YYMMDD)
   * @throws IllegalArgumentException 잘못된 형식이거나 null인 경우
   */
  public static String convertToSixDigit(String birthDate) {
    if (birthDate == null || birthDate.trim().isEmpty()) {
      throw new IllegalArgumentException("생년월일은 필수 입력값입니다.");
    }

    String trimmedBirthDate = birthDate.trim();

    // 이미 6자리인 경우 그대로 반환
    if (trimmedBirthDate.length() == 6 && YYMMDD_PATTERN.matcher(trimmedBirthDate).matches()) {
      log.debug("생년월일이 이미 6자리 형태입니다: {}", maskBirthDate(trimmedBirthDate));
      return trimmedBirthDate;
    }

    // 8자리 형식 검증
    if (trimmedBirthDate.length() != 8 || !YYYYMMDD_PATTERN.matcher(trimmedBirthDate).matches()) {
      throw new IllegalArgumentException(
          "생년월일은 YYYYMMDD 8자리 또는 YYMMDD 6자리 숫자 형태여야 합니다. 입력값: " + trimmedBirthDate);
    }

    // YYYYMMDD -> YYMMDD 변환
    String converted = trimmedBirthDate.substring(2);

    log.debug("생년월일 변환 완료: {} -> {}", maskBirthDate(trimmedBirthDate), maskBirthDate(converted));

    return converted;
  }

  /**
   * 생년월일 형식 검증
   *
   * @param birthDate 검증할 생년월일
   * @return 유효한 형식인지 여부 (YYYYMMDD 또는 YYMMDD)
   */
  public static boolean isValidFormat(String birthDate) {
    if (birthDate == null || birthDate.trim().isEmpty()) {
      return false;
    }

    String trimmed = birthDate.trim();

    // 6자리 또는 8자리 숫자 형식 확인
    return (trimmed.length() == 8 && YYYYMMDD_PATTERN.matcher(trimmed).matches())
        || (trimmed.length() == 6 && YYMMDD_PATTERN.matcher(trimmed).matches());
  }

  /**
   * 생년월일 기본 논리적 검증
   *
   * @param birthDate 검증할 생년월일 (YYYYMMDD 또는 YYMMDD)
   * @return 논리적으로 유효한지 여부
   */
  public static boolean isLogicallyValid(String birthDate) {
    if (!isValidFormat(birthDate)) {
      return false;
    }

    String trimmed = birthDate.trim();
    String month;
    String day;

    if (trimmed.length() == 8) {
      month = trimmed.substring(4, 6);
      day = trimmed.substring(6, 8);
    } else {
      month = trimmed.substring(2, 4);
      day = trimmed.substring(4, 6);
    }

    int monthInt = Integer.parseInt(month);
    int dayInt = Integer.parseInt(day);

    // 월 검증 (01~12)
    if (monthInt < 1 || monthInt > 12) {
      return false;
    }

    // 일 검증 (01~31, 간단한 범위 체크)
    if (dayInt < 1 || dayInt > 31) {
      return false;
    }

    return true;
  }

  /**
   * 로그 출력용 생년월일 마스킹
   *
   * @param birthDate 마스킹할 생년월일
   * @return 마스킹된 생년월일
   */
  private static String maskBirthDate(String birthDate) {
    if (birthDate == null || birthDate.length() < 4) {
      return "****";
    }

    if (birthDate.length() == 6) {
      return birthDate.substring(0, 2) + "****";
    } else if (birthDate.length() == 8) {
      return birthDate.substring(0, 4) + "****";
    }

    return "****";
  }
}
