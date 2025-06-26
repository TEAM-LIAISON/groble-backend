package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInAuthResultDTO {
  private String accessToken; // 엑세스 토큰
  private String refreshToken; // 리프레시 토큰
  private boolean hasAgreedToTerms; // 서비스 이용약관 동의 여부
  private boolean hasNickname; // 닉네임 설정 여부
  private boolean hasVerifiedPhoneNumber; // 전화번호 인증 여부
}
