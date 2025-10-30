package liaison.groble.api.model.content.response.pay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import liaison.groble.api.model.coupon.response.UserCouponResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "콘텐츠 결제 페이지 응답")
public class ContentPayPageResponse {
  @Schema(
      description = "로그인 상태 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean isLoggedIn;

  @Schema(
      description = "콘텐츠 썸네일 이미지 URL",
      example = "https://example.com/thumbnail.jpg",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String thumbnailUrl;

  @Schema(
      description = "콘텐츠 판매자 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String sellerName;

  @Schema(
      description = "콘텐츠 제목",
      example = "프로그래밍 입문 과정",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(
      description = "콘텐츠 유형",
      example = "COACHING",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  @Schema(
      description = "결제 유형",
      example = "ONE_TIME",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String paymentType;

  @JsonFormat(pattern = "yyyy-MM-dd")
  @Schema(description = "다음 결제 예정일 (정기 결제 시 제공)", example = "2024-11-15", type = "string")
  private LocalDate nextPaymentDate;

  @Schema(description = "정기 결제 시 필요한 메타 데이터")
  private SubscriptionMetaResponse subscriptionMeta;

  @Schema(
      description = "옵션 이름",
      example = "기본 옵션",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String optionName;

  @Schema(
      description = "옵션 가격",
      example = "10000.00",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal price;

  @Schema(
      description = "사용자가 보유한 쿠폰 목록",
      implementation = UserCouponResponse.class,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<UserCouponResponse> userCoupons;

  @Getter
  @Builder
  @Schema(description = "정기결제 메타 정보")
  public static class SubscriptionMetaResponse {
    @Schema(description = "활성 빌링키 보유 여부", example = "false")
    private boolean hasActiveBillingKey;

    @Schema(description = "활성 빌링키 ID (PCD_PAYER_ID)", example = "payple-billing-key")
    private String billingKeyId;

    @Schema(description = "사용자 식별 키 (PCD_PAYER_NO)", example = "12345")
    private String merchantUserKey;

    @Schema(description = "기본 결제 수단", example = "CARD")
    private String defaultPayMethod;

    @Schema(description = "Payple 작업 코드", example = "CERT")
    private String payWork;

    @Schema(description = "카드 버전", example = "02")
    private String cardVer;

    @Schema(description = "월 중복 방지 플래그", example = "Y")
    private String regularFlag;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "다음 결제 예정일", example = "2024-11-15")
    private LocalDate nextPaymentDate;

    @Schema(description = "다음 결제 예정 연도", example = "2024")
    private String payYear;

    @Schema(description = "다음 결제 예정 월", example = "11")
    private String payMonth;

    @Schema(description = "다음 결제 예정 일", example = "15")
    private String payDay;

    @Schema(description = "즉시 결제 필요 여부", example = "true")
    private boolean requiresImmediateCharge;
  }
}
