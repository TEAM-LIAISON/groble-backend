package liaison.groble.api.model.content.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품 미리보기 카드 응답 DTO 목록 조회 시 카드 형태로 표시되는 상품 정보 */
@Schema(description = "상품 미리보기 카드 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPreviewCardResponse {
  @Schema(description = "상품 ID", example = "123")
  private Long contentId;

  @Schema(description = "생성 일시", example = "2025-04-20T10:15:30")
  private LocalDateTime createdAt;

  @Schema(description = "상품 제목", example = "Java 프로그래밍 코칭")
  private String title;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail1.jpg")
  private String thumbnailUrl;

  @Schema(description = "판매자 이름", example = "개발자킴")
  private String sellerName;

  @Schema(
      description = "상품 상태",
      example = "DRAFT",
      allowableValues = {"DRAFT", "PENDING", "ACTIVE", "REJECTED", "SUSPENDED"})
  private String status;
}
