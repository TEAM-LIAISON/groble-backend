package liaison.groble.external.adapter.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 페이플 결제 요청 Value Object
 *
 * <p>페이플 앱카드 결제 요청에 필요한 모든 정보를 불변 객체로 캡슐화합니다. Value Object 패턴을 통해 데이터의 무결성을 보장하고 의미를 명확히 합니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PayplePaymentRequest {

  private final String url; // 결제 승인 URL
  private final String cstId; // 고객사 ID
  private final String custKey; // 고객사 키
  private final String authKey; // 인증 키
  private final String payReqKey; // 결제 요청 키
  private final String cardQuota; // 할부 개월수 (00: 일시불)

  /**
   * 요청 데이터의 유효성을 검증합니다.
   *
   * @return 유효한 경우 true
   */
  public boolean isValid() {
    return url != null
        && !url.trim().isEmpty()
        && cstId != null
        && !cstId.trim().isEmpty()
        && custKey != null
        && !custKey.trim().isEmpty()
        && authKey != null
        && !authKey.trim().isEmpty()
        && payReqKey != null
        && !payReqKey.trim().isEmpty()
        && cardQuota != null
        && !cardQuota.trim().isEmpty();
  }

  /**
   * 민감한 정보를 마스킹한 문자열을 반환합니다.
   *
   * @return 마스킹된 요청 정보
   */
  @Override
  public String toString() {
    return String.format(
        "PayplePaymentRequest{url='%s', cstId='%s', custKey='***', authKey='***', payReqKey='%s', cardQuota='%s'}",
        url, cstId, maskKey(payReqKey), cardQuota);
  }

  private String maskKey(String key) {
    if (key == null || key.length() <= 8) {
      return "***";
    }
    return key.substring(0, 4) + "***" + key.substring(key.length() - 4);
  }
}
