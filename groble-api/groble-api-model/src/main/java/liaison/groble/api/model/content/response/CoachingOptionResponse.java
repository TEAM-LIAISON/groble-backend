package liaison.groble.api.model.content.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "코칭 옵션 정보 응답")
public class CoachingOptionResponse extends BaseOptionResponse {
  @Schema(description = "옵션 유형", example = "COACHING_OPTION")
  private final String optionType = "COACHING_OPTION";

  @Schema(description = "코칭 기간", example = "ONE_DAY")
  private String coachingPeriod;

  @Schema(description = "자료 제공 여부", example = "PROVIDED")
  private String documentProvision;

  @Schema(description = "코칭 방식", example = "ONLINE")
  private String coachingType;

  @Schema(description = "코칭 방식 설명", example = "줌을 통한 온라인 미팅으로 진행됩니다.")
  private String coachingTypeDescription;
}
