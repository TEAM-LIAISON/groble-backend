package liaison.groble.api.model.order.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 응답")
public class OrderResponse {

  @Schema(description = "주문 ID (merchantUid)", example = "ORD_abc123_1234567890")
  private String orderId;

  @Schema(
      description = "주문 상태",
      example = "PENDING",
      allowableValues = {"PENDING", "PAID", "CANCELLED", "FAILED"})
  private String status;

  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Schema(description = "콘텐츠 제목", example = "Spring Boot 마스터 클래스")
  private String contentTitle;

  @Schema(description = "선택된 옵션 ID", example = "1")
  private Long optionId;

  @Schema(description = "선택된 옵션명", example = "1:1 코칭 1회")
  private String optionName;

  @Schema(description = "원가", example = "100000")
  private BigDecimal originalAmount;

  @Schema(description = "할인 금액", example = "10000")
  private BigDecimal discountAmount;

  @Schema(description = "최종 결제 금액", example = "90000")
  private BigDecimal finalAmount;

  @Schema(description = "쿠폰 적용 여부", example = "true")
  private boolean couponApplied;

  @Schema(description = "쿠폰 코드", example = "WELCOME10")
  private String couponCode;

  @Schema(description = "주문 생성일시")
  private LocalDateTime createdAt;
}
