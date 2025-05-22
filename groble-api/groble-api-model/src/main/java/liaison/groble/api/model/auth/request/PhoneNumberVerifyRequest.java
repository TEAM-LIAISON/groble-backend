package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberVerifyRequest {

  @NotBlank(message = "전화번호는 필수 입력값입니다.")
  @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식으로 입력해주세요.")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;
}
