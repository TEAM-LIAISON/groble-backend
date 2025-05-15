package liaison.groble.api.model.content.response;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeName("COACHING_OPTION")
@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(name = "CoachingOptionResponse", description = "코칭 옵션 정보 응답")
public class CoachingOptionResponse extends BaseOptionResponse {
  @Override
  public String getOptionType() {
    return "COACHING_OPTION";
  }

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
}
