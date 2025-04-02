package liaison.grobleauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하로 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Size(max = 50, message = "사용자 이름은 50자 이하로 입력해주세요.")
    private String userName;

    @Builder.Default private boolean marketingConsent = false;
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

  /** 이메일 인증 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EmailVerificationRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;
  }

  /** 비밀번호 변경 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ChangePasswordRequest {
    @NotBlank(message = "현재 비밀번호는 필수 입력값입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하로 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;
  }

  /** 비밀번호 재설정 요청 DTO */
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResetPasswordRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하로 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;
  }
}
