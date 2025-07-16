package liaison.groble.api.model.content.response.dynamic;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicContentResponse {
  @Schema(
      description = "콘텐츠 ID",
      example = "1",
      type = "number",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long contentId;

  @Schema(
      description = "콘텐츠 제목",
      example = "UX 디자인 입문 강의",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(
      description = "콘텐츠 타입",
      example = "VIDEO",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String contentType;

  @Schema(
      description = "콘텐츠 썸네일 이미지 URL",
      example = "https://cdn.example.com/thumbnail.jpg",
      type = "string")
  private String thumbnailUrl;

  @Schema(
      description = "콘텐츠 업데이트 일시",
      example = "2025-04-20 10:15:30",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;
}
