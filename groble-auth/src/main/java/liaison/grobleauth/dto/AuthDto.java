package liaison.grobleauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 인증 관련 DTO */
public class AuthDto {
  /** 회원가입 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SignupRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    // @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String name;
  }

  /** 로그인 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LoginRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
  }

  /** 토큰 응답 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
  }

  /** 토큰 갱신 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TokenRefreshRequest {
    @NotBlank(message = "리프레시 토큰은 필수 입력값입니다.")
    private String refreshToken;
  }

  /** 사용자 정보 응답 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String providerType;
    private String providerId;
  }
}
