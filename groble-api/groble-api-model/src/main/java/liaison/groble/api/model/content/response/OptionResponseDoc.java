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

  @Schema(description = "문서 파일 URL", example = "https://example.com/document.pdf")
  private String documentFileUrl;

  @Schema(description = "문서 링크 URL", example = "https://example.com/document-link")
  private String documentLinkUrl;

  @Schema(description = "문서 원본 파일 이름", example = "document.pdf")
  private String documentOriginalFileName;
}
