package liaison.groble.api.model.content.request.register;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ContentRegisterRequest {

  @Schema(description = "콘텐츠 ID", example = "1")
  private Long contentId;

  @NotBlank(message = "제목은 필수 입력 항목입니다")
  @Size(max = 30, message = "제목은 30자 이내로 입력해주세요")
  @Schema(description = "콘텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @NotBlank(message = "콘텐츠 유형은 필수 입력 항목입니다")
  @Pattern(regexp = "^(COACHING|DOCUMENT)$", message = "콘텐츠 유형은 COACHING과 DOCUMENT만 가능합니다.")
  @Schema(description = "콘텐츠 유형", example = "COACHING")
  private String contentType;

  @NotNull(message = "카테고리 ID는 필수 입력 항목입니다")
  @Schema(description = "카테고리 ID", example = "1")
  private Long categoryId;

  @NotBlank(message = "썸네일 이미지 URL은 필수 입력 항목입니다")
  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Valid
  @Schema(description = "코칭 옵션 목록 (contentType이 COACHING인 경우)")
  private List<CoachingOptionRegisterRequest> coachingOptions;

  @Valid
  @Schema(description = "문서 옵션 목록 (contentType이 DOCUMENT인 경우)")
  private List<DocumentOptionRegisterRequest> documentOptions;

  /** 요청의 유효성을 검증합니다. contentType에 따라 적절한 옵션 목록이 제공되었는지 확인합니다. */
  public void validate() {
    if ("COACHING".equals(contentType)) {
      if (coachingOptions == null || coachingOptions.isEmpty()) {
        throw new IllegalArgumentException("COACHING 유형은 최소 하나 이상의 코칭 옵션이 필요합니다.");
      }
    } else if ("DOCUMENT".equals(contentType)) {
      if (documentOptions == null || documentOptions.isEmpty()) {
        throw new IllegalArgumentException("DOCUMENT 유형은 최소 하나 이상의 문서 옵션이 필요합니다.");
      }
    }
  }
}
