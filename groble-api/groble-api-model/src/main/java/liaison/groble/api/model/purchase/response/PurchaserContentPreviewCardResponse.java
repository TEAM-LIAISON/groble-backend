package liaison.groble.api.model.purchase.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구매자 전용 콘텐츠 미리보기 카드 응답 DTO")
public class PurchaserContentPreviewCardResponse {
  @Schema(description = "주문 고유 ID", example = "ORD123456789")
  private String merchantUid;

  @Schema(description = "콘텐츠 ID", example = "123")
  private Long contentId;

  @Schema(description = "콘텐츠 유형", example = "COACHING")
  private String contentType;

  @Schema(description = "구매 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime purchasedAt;

  @Schema(description = "콘텐츠 제목", example = "Java 프로그래밍 코칭")
  private String title;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail1.jpg")
  private String thumbnailUrl;

  @Schema(description = "판매자 이름", example = "개발자킴")
  private String sellerName;

  @Schema(description = "콘텐츠 원래 가격 (null인 경우 -> 가격미정)", example = "100000")
  private BigDecimal originalPrice;

  @Schema(description = "콘텐츠 최종 가격", example = "80000")
  private BigDecimal finalPrice;

  @Schema(description = "가격 옵션 개수", example = "3")
  private int priceOptionLength;

  @Schema(
      description = "콘텐츠 주문 상태 [PAID - 결제완료], [EXPIRED - 기간만료], [CANCELLED - 결제취소]",
      example = "PAID")
  private String orderStatus;

  @Schema(description = "결제 타입 (단건결제: ONE_TIME, 정기결제: SUBSCRIPTION)", example = "SUBSCRIPTION")
  private String paymentType;

  @Schema(description = "정기결제 회차 (정기결제가 아닌 경우 null)", example = "2", nullable = true)
  private Integer subscriptionRound;

  @Schema(
      description = "구독 상태 (정기결제가 아니면 null)",
      example = "ACTIVE",
      nullable = true,
      allowableValues = {"ACTIVE", "CANCELLED", "PAST_DUE"})
  private String subscriptionStatus;

  @Schema(
      description = "구독 완전 해지 여부 - 유예기간 만료됨 (정기결제가 아니면 null)",
      example = "false",
      nullable = true)
  private Boolean isSubscriptionTerminated;

  @Schema(description = "결제 실패 사유 (결제 실패한 경우만)", example = "카드 한도 초과", nullable = true)
  private String billingFailureReason;
}
