package liaison.groble.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 완료 응답 DTO - 카드 결제 완료 후 클라이언트에 전달되는 응답 데이터 구조
 *
 * @author Groble Team
 */
@Getter
@Builder
public class AppCardPaymentCompleteResponseDto {
  // 주문 식별 값

  // 결제 정보
  // - 주문 금액 (pcdPayTotal)
  // - 할인 금액
  // - 카드사명 (pcdPayCardName)
  // - 카드 번호 (pcdPayCardNum)
}
