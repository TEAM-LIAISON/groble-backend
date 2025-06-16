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
@Schema(description = "관리자 페이지에서 콘텐츠 요약 정보에 대한 응답 DTO")
public class AdminContentSummaryInfoResponse {

  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(description = "콘텐츠 생성 시간", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(
      description = "콘텐츠 유형 (예: 'DOCUMENT', 'COACHING')",
      example = "DOCUMENT",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  @Schema(
      description = "메이커 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String sellerName;

  @Schema(
      description = "콘텐츠 제목",
      example = "자바 프로그래밍 입문",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentTitle;

  @Schema(description = "콘텐츠 최저가 가격 (null인 경우 -> 가격미정)", example = "100000")
  private BigDecimal minPrice;

  @Schema(
      description = "가격 옵션 개수",
      example = "3",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private int priceOptionLength;

  @Schema(
      description = "콘텐츠 상태 (예: 'DRAFT(작성중)', 'ACTIVE(판매중)'",
      example = "DRAFT",
      type = "string",
      allowableValues = {"ACTIVE", "DRAFT"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentStatus;

  @Schema(
      description =
          "관리자의 콘텐츠 심사 상태 (예: 'PENDING(심사 필요)', 'VALIDATED(심사 완료 - 판매승인)', 'DISCONTINUED(심사 완료 - 판매중단)'",
      example = "PENDING",
      type = "string",
      allowableValues = {"PENDING", "VALIDATED", "DISCONTINUED"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String adminContentCheckingStatus;
}
