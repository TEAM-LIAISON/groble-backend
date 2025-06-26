package liaison.groble.api.model.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppCardPayplePaymentResponse {

  @Schema(
      description = "결제 결과 (success OR error)",
      example = "success",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payRst;

  @Schema(
      description = "결제 응답 코드",
      example = "0000",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCode; // 결제 결과 코드

  @Schema(
      description = "결제 응답 메시지",
      example = "카드 인증 완료",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payMsg;

  // 결제 기본 정보
  @Schema(
      description = "주문 번호 (merchantUid)",
      example = "20250611161125129",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payOid;

  @Schema(
      description = "결제수단(카드/계좌)입니다.",
      example = "card",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payType; // 결제 방법 (card)

  @Schema(
      description = "결제 완료 시간입니다.",
      example = "20250609013218",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payTime;

  @Schema(
      description = "결제 금액입니다.",
      example = "101",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payTotal;

  // 카드 정보
  @Schema(
      description = "카드사명",
      example = "카드사명",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardName;

  @Schema(
      description = "카드번호입니다. (마스킹 처리된 번호)",
      example = "****-****-****-1234",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardNum;

  @Schema(
      description = "할부 개월 수입니다.",
      example = "00",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardQuota;

  @Schema(
      description = "카드 거래 번호입니다.",
      example = "202506090131457067403400",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardTradeNum;

  @Schema(
      description = "승인번호입니다.",
      example = "22717503",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardAuthNo;

  @Schema(
      description = "카드 매출 전표(영수증) 출력 URL입니다.",
      example = "https://example.com/receipt/1234567890",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String payCardReceipt;
}
