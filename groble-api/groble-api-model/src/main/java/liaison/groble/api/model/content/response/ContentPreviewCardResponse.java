package liaison.groble.api.model.content.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "콘텐츠 미리보기 카드 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPreviewCardResponse {
  @Schema(description = "콘텐츠 ID", example = "123")
  private Long contentId;

  @Schema(description = "생성 일시", example = "2025-04-20T10:15:30")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "콘텐츠 제목", example = "Java 프로그래밍 코칭")
  private String title;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail1.jpg")
  private String thumbnailUrl;

  @Schema(description = "판매자 이름", example = "개발자킴")
  private String sellerName;

  @Schema(description = "콘텐츠 최저가 가격 (null인 경우 -> 가격미정)", example = "100000")
  private BigDecimal lowestPrice;

  @Schema(description = "가격 옵션 개수", example = "3")
  private int priceOptionLength;

  @Schema(
      description =
          "콘텐츠 상태 [ACTIVE - 판매중], [DRAFT - 작성중], [PENDING - 심사중], [VALIDATED - 심사완료(승인)], [REJECTED - 심사완료(거절)]",
      example = "DRAFT",
      allowableValues = {"ACTIVE", "DRAFT", "PENDING", "VALIDATED", "REJECTED"})
  private String status;
}
