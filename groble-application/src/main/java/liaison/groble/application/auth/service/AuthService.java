package liaison.groble.application.auth.service;

import liaison.groble.application.auth.dto.SignupDto;
import liaison.groble.application.auth.dto.TokenDto;

public interface AuthService {
  /**
   * 회원가입 처리 및 토큰 발급
   *
   * @param signupDto 회원가입 정보
   * @return 발급된 토큰 정보
   */
  TokenDto signup(SignupDto signupDto);
}
