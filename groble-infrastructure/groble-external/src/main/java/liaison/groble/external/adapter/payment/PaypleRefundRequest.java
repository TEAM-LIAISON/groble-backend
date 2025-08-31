package liaison.groble.external.adapter.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 페이플 환불 요청 Value Object
 *
 * <p>페이플 환불 요청에 필요한 모든 정보를 불변 객체로 캡슐화합니다. Value Object 패턴을 통해 데이터의 무결성을 보장하고 의미를 명확히 합니다.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class PaypleRefundRequest {

  private final String url; // 환불 처리 URL
  private final String cstId; // 고객사 ID
  private final String custKey; // 고객사 키
  private final String authKey; // 인증 키
  private final String payOid; // 결제 고유 번호 (주문번호)
  private final String refundTotal; // 환불 총 금액 (결제취소 요청금액)
  private final String refundTaxfree; // 환불 비과세 금액 (결제취소 부가세)
  private final String refundReason; // 환불 사유

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
        && payOid != null
        && !payOid.trim().isEmpty()
        && refundTotal != null
        && !refundTotal.trim().isEmpty()
        && refundReason != null
        && !refundReason.trim().isEmpty();
  }

  /**
   * 전액 환불인지 확인합니다.
   *
   * @return 환불 비과세 금액이 0인 경우 true
   */
  public boolean isFullRefund() {
    return refundTaxfree == null || "0".equals(refundTaxfree.trim());
  }

  /**
   * 민감한 정보를 마스킹한 문자열을 반환합니다.
   *
   * @return 마스킹된 요청 정보
   */
  @Override
  public String toString() {
    return String.format(
        "PaypleRefundRequest{url='%s', cstId='%s', custKey='***', authKey='***', "
            + "payOid='%s', refundTotal='%s', refundReason='%s'}",
        url, cstId, payOid, refundTotal, refundReason);
  }
}
