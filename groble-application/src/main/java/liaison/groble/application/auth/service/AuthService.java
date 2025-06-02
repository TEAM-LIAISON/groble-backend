package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.SocialSignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;

public interface AuthService {

  TokenDto signUp(SignUpDto signUpDto);

  TokenDto socialSignUp(Long userId, SocialSignUpDto socialSignUpDto);

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
  void sendEmailVerificationForSignUp(EmailVerificationDto emailVerificationDto);

  /**
   * 이메일 인증 메일 발송
   *
   * @param emailVerificationDto 이메일 정보
   */
  void sendEmailVerificationForChangeEmail(Long userId, EmailVerificationDto emailVerificationDto);

  /**
   * 로그아웃 처리
   *
   * @param userId 사용자 식별 PK
   */
  void logout(Long userId);

  /**
   * 비밀번호 재설정 이메일 발송
   *
   * @param email 비밀번호를 재설정할 이메일
   */
  void sendPasswordResetEmail(String email);

  void resetPassword(String token, String newPassword);

  /**
   * 이메일 인증 코드 검증
   *
   * @param verifyEmailCodeDto 이메일 인증 코드 정보
   */
  void verifyEmailCode(VerifyEmailCodeDto verifyEmailCodeDto);

  /**
   * 이메일 인증 코드 검증
   *
   * @param userId 사용자 식별 PK
   * @param verifyEmailCodeDto 이메일 인증 코드 정보
   */
  void verifyEmailCodeForChangeEmail(Long userId, VerifyEmailCodeDto verifyEmailCodeDto);

  /**
   * Refresh tokens
   *
   * @param refreshToken refresh token
   * @return TokenDto
   */
  TokenDto refreshTokens(String refreshToken);

  /**
   * 닉네임 중복 확인
   *
   * @param nickname 확인할 닉네임
   * @return 중복 여부
   */
  boolean isNicknameTaken(String nickname);

  /**
   * 닉네임 설정 또는 업데이트
   *
   * @param nickname 설정할 닉네임
   * @return 설정된 닉네임
   */
  String updateNickname(Long userId, String nickname);

  /**
   * 사용자 탈퇴 처리
   *
   * @param userId 사용자 식별 PK
   * @param userWithdrawalDto 탈퇴 사유 및 기타 정보
   */
  void withdrawUser(Long userId, UserWithdrawalDto userWithdrawalDto);

  void resetPhoneNumber(Long userId, PhoneNumberVerifyRequestDto phoneNumberVerifyRequestDto);
}
