package liaison.groble.api.model.sell.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[내 스토어 - 상품 관리 - 판매 관리- 판매리스트 상세] 정보 응답")
public class ContentSellDetailResponse {

  @Schema(
      description = "구매 ID",
      example = "200",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long purchaseId;

  @Schema(
      description = "콘텐츠 제목",
      example = "자바 프로그래밍 코칭",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(description = "구매 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime purchasedAt;

  @Schema(
      description = "콘텐츠 제목",
      example = "자바 프로그래밍 코칭",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String purchaserNickname;

  @Schema(
      description = "구매자 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String purchaserEmail;

  @Schema(
      description = "구매자 핸드폰 번호",
      example = "010-3661-4067",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String purchaserPhoneNumber;

  @Schema(
      description = "구매한 콘텐츠 옵션 이름",
      example = "옵션 이름",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String selectedOptionName;

  @Schema(
      description = "콘텐츠 결제 가격",
      example = "29900",
      type = "number",
      format = "decimal",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal finalPrice;

  @Schema(description = "결제 타입 (ONE_TIME / SUBSCRIPTION)", example = "SUBSCRIPTION")
  private String paymentType;

  @Schema(description = "정기결제 회차 (정기결제가 아니면 null)", example = "2", nullable = true)
  private Integer subscriptionRound;
}
