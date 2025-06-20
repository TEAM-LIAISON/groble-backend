package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.UserWithdrawalDTO;

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

  void withdrawUser(Long userId, UserWithdrawalDTO userWithdrawalDto);
}
