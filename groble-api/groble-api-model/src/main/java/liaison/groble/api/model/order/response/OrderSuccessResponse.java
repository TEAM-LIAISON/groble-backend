package liaison.groble.api.model.order.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSuccessResponse {
  @Schema(
      description = "주문 식별 ID",
      example = "20251020349820",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "구매 일시",
      example = "2025-04-20 10:15:30",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
  private LocalDateTime purchasedAt;

  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final Long contentId;

  @Schema(
      description = "판매자 이름",
      example = "홍길동 코칭",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String sellerName;

  @Schema(
      description = "콘텐츠 제목",
      example = "UX 디자인 입문 강의",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final String contentTitle;

  @Schema(
      description =
          "구매 상태 (결제대기: PENDING, 결제완료: PAID, 결제취소: CANCEL_REQUEST, 환불완료: CANCELLED, 결제실패: FAILED)",
      example = "PAID",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final String orderStatus;

  @Schema(description = "콘텐츠 설명", example = "기초부터 배우는 UX 디자인 강의입니다.", type = "string")
  private final String contentDescription;

  @Schema(
      description = "콘텐츠 썸네일 이미지 URL",
      example = "https://cdn.example.com/thumbnail.jpg",
      type = "string")
  private final String contentThumbnailUrl;

  @Schema(description = "선택한 옵션 ID", example = "101", type = "number")
  private final Long selectedOptionId;

  @Schema(
      description = "선택한 옵션 타입 (COACHING_OPTION / DOCUMENT_OPTION)",
      example = "COACHING_OPTION",
      type = "string")
  private final String selectedOptionType;

  @Schema(
      description = "옵션 이름",
      example = "기본 옵션",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String selectedOptionName;

  @Schema(
      description = "원래 가격",
      example = "99000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final BigDecimal originalPrice;

  @Schema(description = "할인 금액", example = "10000", type = "number")
  private final BigDecimal discountPrice;

  @Schema(
      description = "최종 결제 금액",
      example = "89000",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final BigDecimal finalPrice;

  @Schema(
      description = "무료 구매 여부",
      example = "false",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private final Boolean isFreePurchase;
}
