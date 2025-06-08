package liaison.groble.api.model.admin.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "로그인 응답 DTO")
public class AdminSignInResponse {
  @Schema(description = "로그인된 관리자 이메일", example = "admin@groble.com")
  private String email;

  @Schema(description = "관리자 역할 (User Type)", example = "ADMIN")
  private String role;

  public static AdminSignInResponse of(String email, String role) {
    return new AdminSignInResponse(email, role);
  }
}
