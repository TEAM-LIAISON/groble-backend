package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;

public interface AuthService {
  /**
   * 회원가입 처리 및 토큰 발급
   *
   * @param signUpDto 회원가입 정보
   * @return 발급된 토큰 정보
   */
  TokenDto signUp(SignUpDto signUpDto);

  /**
   * 로그인 처리 및 토큰 발급
   *
   * @param signInDto 로그인 정보
   * @return 발급된 토큰 정보
   */
  TokenDto signIn(SignInDto signInDto);

  /**
   * 이메일 인증 메일 발송
   *
   * @param emailVerificationDto 이메일 정보
   */
  void sendEmailVerification(EmailVerificationDto emailVerificationDto);

  /**
   * 로그아웃 처리
   *
   * @param userId 사용자 식별 PK
   */
  void logout(Long userId);
}
