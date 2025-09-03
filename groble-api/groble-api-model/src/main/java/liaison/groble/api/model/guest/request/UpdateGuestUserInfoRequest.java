package liaison.groble.api.model.guest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비회원 사용자 정보 업데이트 요청")
public class UpdateGuestUserInfoRequest {

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Schema(description = "비회원 사용자 이메일", example = "example@example.com")
  private String email;

  @NotBlank(message = "이름은 필수입니다.")
  @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하로 입력해주세요.")
  @Schema(description = "비회원 사용자 이름", example = "홍길동")
  private String username;
}
