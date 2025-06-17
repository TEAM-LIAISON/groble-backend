package liaison.groble.api.model.admin.response;

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
@Schema(description = "관리자 페이지에서 주문 요약 정보에 대한 응답 DTO")
public class AdminOrderSummaryInfoResponse {

  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(
      description = "주문 식별 ID",
      example = "20251020349820",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(description = "주문 생성 시간", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  // 2. 구매 유형
  @Schema(
      description = "구매 유형 (예: 'DOCUMENT', 'COACHING')",
      example = "DOCUMENT",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  // 3. 구매자 이름
  @Schema(
      description = "구매자 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String purchaserName;

  // 4. 구매한 콘텐츠 제목
  @Schema(
      description = "구매한 콘텐츠 제목",
      example = "자바 프로그래밍 입문",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  // 5. 콘텐츠 가격
  @Schema(
      description = "콘텐츠 가격",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal finalPrice;

  // 6. 구매 완료된 상태
  @Schema(
      description =
          "주문 상태 (예: 'PENDING(결제대기)', 'PAID(결제완료)', 'CANCELLED(결제취소)', 'EXPIRED(기간만료)', 'FAILED(결제실패)')",
      example = "PAID",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderStatus;
}
