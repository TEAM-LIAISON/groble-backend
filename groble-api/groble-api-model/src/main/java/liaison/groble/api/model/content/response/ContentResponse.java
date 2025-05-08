package liaison.groble.api.model.content.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentResponse {
  @Schema(description = "콘텐츠 ID", example = "1")
  private Long id;

  @Schema(description = "콘텐츠 이름", example = "사업계획서 컨설팅")
  private String title;

  @Schema(description = "콘텐츠 유형 [COACHING - 코칭], [DOCUMENT - 자료]", example = "COACHING")
  private String contentType;

  @Schema(description = "카테고리 ID", example = "1")
  private Long categoryId;

  @Schema(description = "카테고리 이름", example = "비즈니스 컨설팅")
  private String categoryName;

  @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
  private String thumbnailUrl;

  @Schema(description = "콘텐츠 상태", example = "DRAFT")
  private String status;

  @Schema(description = "옵션 목록")
  private List<OptionResponse> options;

  @Getter
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class OptionResponse {
    @Schema(description = "옵션 ID", example = "1")
    private Long id;

    @Schema(description = "옵션 이름", example = "스탠다드 옵션")
    private String name;

    @Schema(description = "옵션 설명", example = "기본적인 컨설팅을 제공합니다.")
    private String description;

    @Schema(description = "가격", example = "50000")
    private BigDecimal price;

    // 코칭 옵션 관련 필드
    @Schema(description = "코칭 기간", example = "ONE_WEEK")
    private String coachingPeriod;

    @Schema(description = "자료 제공 여부", example = "PROVIDED")
    private String documentProvision;

    @Schema(description = "코칭 방식", example = "ONLINE")
    private String coachingType;

    @Schema(description = "코칭 방식 설명", example = "줌을 통한 온라인 미팅으로 진행됩니다.")
    private String coachingTypeDescription;

    // 문서 옵션 관련 필드
    @Schema(description = "콘텐츠 제공 방식", example = "IMMEDIATE_DOWNLOAD")
    private String contentDeliveryMethod;
  }
}
