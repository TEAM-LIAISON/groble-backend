package liaison.groble.api.model.content.request.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CoachingOptionRegisterRequest extends BaseOptionRegisterRequest {
  @NotBlank(message = "코칭 기간은 필수 입력 항목입니다")
  @Pattern(regexp = "^(ONE_DAY|TWO_TO_SIX_DAYS|MORE_THAN_ONE_WEEK)$", message = "유효한 코칭 기간이 아닙니다")
  @Schema(
      description = "코칭 기간 [ONE_DAY - 1일], [TWO_TO_SIX_DAYS - 2-6일], [MORE_THAN_ONE_WEEK - 일주일 이상]",
      example = "ONE_DAY")
  private String coachingPeriod;

  @NotBlank(message = "코칭 방식 설명은 필수 입력 항목입니다")
  @Pattern(regexp = "^(PROVIDED|NOT_PROVIDED)$", message = "유효한 자료 제공 옵션이 아닙니다")
  @Schema(description = "자료 제공 여부 [PROVIDED - 제공], [NOT_PROVIDED - 미제공]", example = "PROVIDED")
  private String documentProvision;

  @NotBlank(message = "코칭 방식은 필수 입력 항목입니다")
  @Pattern(regexp = "^(ONLINE|OFFLINE)$", message = "유효한 코칭 방식이 아닙니다")
  @Schema(description = "코칭 방식 [ONLINE - 온라인], [OFFLINE - 오프라인]", example = "ONLINE")
  private String coachingType;

  @NotBlank(message = "코칭 방식 설명은 필수 입력 항목입니다")
  @Size(max = 20, message = "코칭 방식 설명은 20자 이하여야 합니다")
  @Schema(description = "코칭 방식 설명", example = "줌을 통한 온라인 미팅으로 진행됩니다.")
  private String coachingTypeDescription;
}
