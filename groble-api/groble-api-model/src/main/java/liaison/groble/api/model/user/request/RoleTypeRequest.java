package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleTypeRequest {
  @NotBlank(message = "사용자 유형은 필수 입력값입니다.")
  @Pattern(regexp = "^(SELLER|BUYER)$", message = "사용자 유형은 SELLER 또는 BUYER만 가능합니다.")
  private String userType;
}
