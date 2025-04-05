package liaison.groble.application.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.service.UserService;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  /**
   * 사용자 역할 전환 (판매자/구매자 모드 전환)
   *
   * @param email 사용자 이메일
   * @param userType 전환할 유형 ("SELLER" 또는 "BUYER")
   * @return 전환 성공 여부
   */
  @Override
  @Transactional
  public boolean switchUserType(String email, String userType) {
    // 유효한 userType 값인지 확인
    if (!"SELLER".equals(userType) && !"BUYER".equals(userType)) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다. SELLER 또는 BUYER만 가능합니다.");
    }

    return true;
  }
}
