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
  private String categoryId;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Schema(description = "검색 엔진 노출 여부", example = "true")
  @Builder.Default
  private Boolean isSearchExposed = Boolean.FALSE;

  @NotBlank(message = "결제 유형은 필수 입력 항목입니다")
  @Pattern(
      regexp = "^(ONE_TIME|SUBSCRIPTION)$",
      message = "결제 유형은 ONE_TIME 또는 SUBSCRIPTION만 가능합니다.")
  @Schema(description = "결제 유형 [ONE_TIME - 단건 결제], [SUBSCRIPTION - 정기 결제]", example = "ONE_TIME")
  private String paymentType;

  @Valid
  @Schema(description = "코칭 옵션 목록 (contentType이 COACHING인 경우)")
  private List<BaseOptionRegisterRequest> coachingOptions;

  @Valid
  @Schema(description = "문서 옵션 목록 (contentType이 DOCUMENT인 경우)")
  private List<DocumentOptionRegisterRequest> documentOptions;

  @Schema(description = "콘텐츠 소개", example = "사업계획서 컨설팅")
  private String contentIntroduction;

  @Schema(description = "서비스 타겟", example = "초창패, 창중, 예창패, 청창사 등을 준비하는 분")
  @Size(max = 1000, message = "서비스 타겟은 1000자 이내로 입력해야 합니다")
  private String serviceTarget;

  @Schema(description = "제공 절차", example = "STANDARD/DELUXE/PREMIUM")
  @Size(max = 1000, message = "제공 절차는 1000자 이내로 입력해야 합니다")
  private String serviceProcess;

  @Schema(description = "메이커 소개", example = "- 동국대학교 철학과 졸업")
  @Size(max = 1000, message = "메이커 소개는 1000자 이내로 입력해야 합니다")
  private String makerIntro;
}
