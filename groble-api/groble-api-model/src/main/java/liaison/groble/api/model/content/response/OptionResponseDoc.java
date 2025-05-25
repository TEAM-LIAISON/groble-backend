package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 스웨거 문서 전용: 코칭 옵션과 문서 옵션의 모든 필드를 한 번에 보여주기 위한 플랫 DTO */
@Getter
@AllArgsConstructor
@Schema(name = "OptionResponseDoc", description = "코칭/문서 옵션의 모든 필드를 포함한 응답 스펙 (문서용)")
public class OptionResponseDoc {
  @Schema(description = "옵션 ID", example = "1")
  private Long optionId;

  @Schema(description = "옵션 유형", example = "COACHING_OPTION")
  private String optionType;

  @Schema(description = "옵션 이름", example = "1시간 코칭")
  private String name;

  @Schema(description = "옵션 설명", example = "1:1 전문가 코칭 1시간")
  private String description;

  @Schema(description = "옵션 가격", example = "50000")
  private Integer price;

  // ─── CoachingOptionResponse 전용 필드 ────────────────────
  @Schema(
      description = "코칭 기간",
      example = "ONE_DAY = [1일], TWO_TO_SIX_DAYS = [2-6일], MORE_THAN_ONE_WEEK = [일주일 이상]")
  private String coachingPeriod;

  @Schema(description = "자료 제공 여부", example = "PROVIDED - [자료 제공], NOT_PROVIDED - [자료 미제공]")
  private String documentProvision;

  @Schema(description = "코칭 방식", example = "ONLINE - [온라인], OFFLINE - [오프라인]")
  private String coachingType;

  @Schema(description = "코칭 방식 설명", example = "줌을 통한 온라인 미팅으로 진행됩니다.")
  private String coachingTypeDescription;

  // ─── DocumentOptionResponse 전용 필드 ───────────────────
  @Schema(
      description = "컨텐츠 제공 방식",
      example = "IMMEDIATE_DOWNLOAD - [즉시 다운로드], FUTURE_UPLOAD - [추후 업로드]")
  private String contentDeliveryMethod;

  @Schema(description = "문서 파일 URL", example = "https://example.com/document.pdf")
  private String documentFileUrl;

  @Schema(description = "문서 링크 URL", example = "https://example.com/document-link")
  private String documentLinkUrl;
}
