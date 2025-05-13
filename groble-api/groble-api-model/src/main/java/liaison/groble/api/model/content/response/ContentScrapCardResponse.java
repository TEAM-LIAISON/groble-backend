package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "콘텐츠 스크랩용 카드 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentScrapCardResponse {
  @Schema(description = "콘텐츠 ID", example = "123")
  private Long contentId;

  @Schema(description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]", example = "COACHING")
  private String contentType;

  @Schema(description = "콘텐츠 제목", example = "Java 프로그래밍 코칭")
  private String title;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail1.jpg")
  private String thumbnailUrl;

  @Schema(description = "판매자 이름", example = "개발자킴")
  private String sellerName;

  @Schema(description = "콘텐츠 스크랩 여부", example = "true")
  private Boolean isContentScrap;
}
