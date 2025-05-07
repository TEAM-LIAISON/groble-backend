package liaison.groble.api.model.content.request.draft;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDraftRequest {

  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @Size(max = 30, message = "제목은 30자 이내로 입력해주세요")
  @Schema(description = "컨텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @Pattern(regexp = "^(COACHING|DOCUMENT)$", message = "컨텐츠 유형은 COACHING과 DOCUMENT만 가능합니다.")
  @Schema(description = "컨텐츠 유형", example = "COACHING")
  private String contentType;

  @Schema(description = "카테고리 ID", example = "1")
  private Long categoryId;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Valid
  @Schema(description = "코칭 옵션 목록 (contentType이 COACHING인 경우)")
  private List<CoachingOptionDraftRequest> coachingOptions;

  @Valid
  @Schema(description = "문서 옵션 목록 (contentType이 DOCUMENT인 경우)")
  private List<DocumentOptionDraftRequest> documentOptions;
}
