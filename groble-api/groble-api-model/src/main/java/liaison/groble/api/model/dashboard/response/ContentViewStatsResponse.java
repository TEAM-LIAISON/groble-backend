package liaison.groble.api.model.dashboard.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "[✅ 대시보드 - 콘텐츠 상세 조회수 응답] 콘텐츠 상세 조회수 응답")
public class ContentViewStatsResponse {
  @Schema(
      description = "날짜",
      example = "2025-07-08",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate viewDate;

  @Schema(
      description = "요일",
      example = "화",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String dayOfWeek;

  @Schema(
      description = "조회수",
      example = "1500",
      type = "integer",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long viewCount;
}
