package liaison.groble.api.model.purchase.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import liaison.groble.api.model.maker.response.ContactInfoResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 콘텐츠 - 구매 관리] 결제완료/결제 취소 요청/환불 완료 콘텐츠 상세 응답 Response")
public class PurchasedContentDetailResponse {

  @Schema(
      description =
          "구매 상태 [COMPLETED - 구매 완료], [CANCELLED - 구매 취소], [REFUND_REQUESTED - 환불 요청], [REFUNDED - 환불완료]",
      example = "PAID - 결제완료",
      type = "String",
      allowableValues = {"COMPLETED", "CANCELLED", "REFUND_REQUESTED", "REFUNDED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderStatus;

  @Schema(
      description = "주문 식별 ID",
      example = "20251020349820",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(description = "구매 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime purchasedAt;

  @Schema(description = "취소 요청 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime cancelRequestedAt;

  @Schema(description = "환불 완료 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime cancelledAt;

  // 콘텐츠 ID
  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  // 메이커 이름
  @Schema(
      description = "메이커(판매자) 이름",
      example = "판매자판매자",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String sellerName;

  // 콘텐츠 제목
  @Schema(
      description = "콘텐츠 제목",
      example = "콘텐츠 제목입니다.",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  // 선택된 옵션 이름
  @Schema(
      description = "구매한 콘텐츠 옵션 이름",
      example = "옵션 이름",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String selectedOptionName;

  // 선택된 옵션 개수
  @Schema(
      description = "구매한 콘텐츠 옵션 개수",
      example = "2",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer selectedOptionQuantity;

  // 선택된 옵션의 유형
  private String selectedOptionType;

  // 파일 객체인 경우 다운로드 URL / 링크 문자열인 경우 링크 URL
  @Schema(
      description = "파일 객체 다운로드 URL 또는 링크 URL",
      example = "true",
      type = "boolean ",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String documentOptionActionUrl;

  // 무료 상품 여부
  @Schema(
      description = "무료 상품 여부",
      example = "true",
      type = "boolean ",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isFreePurchase;

  @Schema(
      description = "콘텐츠 원가",
      example = "30900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal originalPrice;

  // 할인금액
  @Schema(
      description = "할인 가격",
      example = "1000",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal discountPrice;

  // 총 결제 금액
  @Schema(
      description = "최종 가격",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal finalPrice;

  // 결제 수단 상세 정보
  private String payType;
  private String payCardName;
  private String payCardNum;

  // 누락 변수 추가
  private String thumbnailUrl;
  private Boolean isRefundable;
  private String cancelReason;
  // 문의하기 링크 추가
  private ContactInfoResponse contactInfo;
}
