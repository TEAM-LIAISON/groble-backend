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
}
