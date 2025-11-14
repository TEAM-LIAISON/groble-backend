package liaison.groble.application.payment.util;

import org.springframework.util.StringUtils;

/**
 * Payple 할부 개월수 파라미터를 Payple 사양(2자리 문자열)으로 정규화한다.
 *
 * <p>Payple은 일시불을 "00"으로, 2~12개월 할부를 "02"~"12" 형식으로 요구한다. 프론트/모바일에서 "0"과 같이 한 자리 숫자가 넘어오면 승인 API에서
 * 오류 코드 1060(할부개월 입력 오류)가 발생하므로 중앙에서 두 자리 형태로 맞춰서 전달한다.
 */
public final class CardQuotaNormalizer {

  private static final String DEFAULT_QUOTA = "00";

  private CardQuotaNormalizer() {}

  /**
   * 할부 개월수를 Payple이 요구하는 두 자리 문자열로 맞춘다.
   *
   * @param rawQuota 프론트에서 전달된 할부 개월 문자열
   * @return Payple 사양에 맞게 보정된 값 (빈 값은 "00"으로 처리)
   */
  public static String normalize(String rawQuota) {
    if (!StringUtils.hasText(rawQuota)) {
      return DEFAULT_QUOTA;
    }

    String trimmed = rawQuota.trim();

    if (DEFAULT_QUOTA.equals(trimmed) || "0".equals(trimmed)) {
      return DEFAULT_QUOTA;
    }

    if (trimmed.matches("^\\d{1,2}$")) {
      int quota = Integer.parseInt(trimmed);
      if (quota == 0) {
        return DEFAULT_QUOTA;
      }
      return String.format("%02d", quota);
    }

    // Payple이 허용하지 않는 문자열은 그대로 사용하면 승인 단계에서 또 다른 오류를 만들기 때문에
    // 안전하게 기본값으로 치환한다.
    return DEFAULT_QUOTA;
  }
}
