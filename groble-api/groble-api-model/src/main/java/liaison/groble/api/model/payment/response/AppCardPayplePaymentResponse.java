package liaison.groble.api.model.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "앱카드 페이플 결제 응답")
public class AppCardPayplePaymentResponse {
  @Schema(description = "결제 결과", example = "success")
  private String payRst; // 결제 결과 (success/error)

  @Schema(description = "결제 결과 코드", example = "0000")
  private String payCode; // 결제 결과 코드

  @Schema(description = "결제 결과 메시지", example = "결제 승인 완료")
  private String payMsg; // 결제 결과 메시지

  @Schema(description = "주문번호", example = "ORD20250831123456")
  private String payOid; // 주문번호

  @Schema(description = "결제 방법", example = "card")
  private String payType; // 결제 방법 (card)

  @Schema(description = "결제 완료 시간", example = "2025-08-31T12:34:56")
  private String payTime; // 결제 완료 시간

  @Schema(description = "결제 금액", example = "10000")
  private String payTotal; // 결제 금액

  @Schema(description = "카드사명", example = "신한카드")
  private String payCardName; // 카드사명

  @Schema(description = "카드번호 (마스킹)", example = "1234-****-****-5678")
  private String payCardNum; // 카드번호 (마스킹)

  @Schema(description = "할부개월수", example = "00")
  private String payCardQuota; // 할부개월수

  @Schema(description = "거래번호", example = "TXN202508311234")
  private String payCardTradeNum; // 거래번호

  @Schema(description = "승인번호", example = "12345678")
  private String payCardAuthNo; // 승인번호

  @Schema(description = "카드 매출전표 URL", example = "https://receipt.example.com/12345")
  private String payCardReceipt; // 카드 매출전표 URL
}
