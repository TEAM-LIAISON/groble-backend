package liaison.groble.api.model.order.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderResponse {
  @Schema(
      description = "주문 식별 ID",
      example = "20251020349820",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "결제를 진행하는 사용자 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "사용자 전화번호",
      example = "010-1234-5678",
      type = "string",
      pattern = "^\\d{3}-\\d{4}-\\d{4}$",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "콘텐츠 제목",
      example = "콘텐츠 제목입니다.",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(
      description = "최종 가격",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal totalPrice;

  @Schema(
      description = "구매한 콘텐츠 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean isPurchasedContent;

  @Schema(description = "Payple 결제 옵션 정보")
  private PaypleOptionsResponse paypleOptions;

  @Getter
  @Builder
  @Schema(description = "정기 결제를 위한 Payple 옵션")
  public static class PaypleOptionsResponse {
    @Schema(
        description = "빌링키 처리 동작(REUSE|REGISTER|REGISTER_AND_CHARGE)",
        example = "REGISTER_AND_CHARGE")
    private String billingKeyAction;

    @Schema(description = "Payple 작업 코드", example = "CERT")
    private String payWork;

    @Schema(description = "카드 버전", example = "02")
    private String cardVer;

    @Schema(description = "월 중복 방지 플래그", example = "Y")
    private String regularFlag;

    @Schema(description = "기본 결제 수단", example = "CARD")
    private String defaultPayMethod;

    @Schema(description = "사용자 식별 키 (PCD_PAYER_NO)", example = "12345")
    private String merchantUserKey;

    @Schema(description = "빌링키 ID (PCD_PAYER_ID)", example = "payple-billing-key")
    private String billingKeyId;

    @Schema(description = "다음 결제 예정일", example = "2024-11-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextPaymentDate;

    @Schema(description = "다음 결제 예정 연도", example = "2024")
    private String payYear;

    @Schema(description = "다음 결제 예정 월", example = "11")
    private String payMonth;

    @Schema(description = "다음 결제 예정 일", example = "15")
    private String payDay;
  }
}
