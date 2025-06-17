package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;

public interface AuthService {

  /**
   * 로그인 처리 및 토큰 발급
   *
   * @param signInDto 로그인 정보
   * @return 발급된 토큰 정보
   */
  SignInAuthResultDTO signIn(SignInDTO signInDto);

  /**
   * 로그아웃 처리
   *
   * @param userId 사용자 식별 PK
   */
  void logout(Long userId);

  /**
   * Refresh tokens
   *
   * @param refreshToken refresh token
   * @return TokenDto
   */
  TokenDto refreshTokens(String refreshToken);

  void withdrawUser(Long userId, UserWithdrawalDto userWithdrawalDto);

  void resetPhoneNumber(Long userId, PhoneNumberVerifyRequestDto phoneNumberVerifyRequestDto);
}
