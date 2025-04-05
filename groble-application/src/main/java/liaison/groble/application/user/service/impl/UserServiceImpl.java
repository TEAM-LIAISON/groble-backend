package liaison.groble.application.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.service.UserService;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final IntegratedAccountRepository integratedAccountRepository;

  /**
   * 사용자 역할 전환 (판매자/구매자 모드 전환)
   *
   * @param userId 사용자 ID
   * @param userType 전환할 유형 ("SELLER" 또는 "BUYER")
   * @return 전환 성공 여부
   */
  @Override
  @Transactional
  public boolean switchUserType(Long userId, String userType) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // 요청한 역할을 가지고 있는지 확인
    boolean hasSeller =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_SELLER"));
    boolean hasBuyer =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_USER"));

    if (("SELLER".equals(userType) && !hasSeller) || ("BUYER".equals(userType) && !hasBuyer)) {
      log.warn("사용자가 전환하려는 역할({})을 가지고 있지 않습니다: {}", userType, user.getEmail());
      return false;
    }

    user.updateLastUserType(userType);
    userRepository.save(user);

    log.info("사용자 역할 전환 완료: {} -> {}", user.getEmail(), userType);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public String getUserType(String email) {
    // 이메일로 계정 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    User user = account.getUser();

    // 기본 정보가 없는 경우
    if (user.getUserName() == null || user.getUserName().isEmpty()) {
      return "NONE";
    }

    // 사용자 역할 확인
    boolean isSeller =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_SELLER"));
    boolean isBuyer =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_USER"));

    // 둘 다 가진 경우 마지막 사용 역할 반환
    if (isSeller && isBuyer) {
      String lastUserType = user.getLastUserType();
      // 마지막 사용 역할이 없는 경우 기본값으로 BUYER 반환
      return lastUserType != null ? lastUserType : "BUYER";
    } else if (isSeller) {
      return "SELLER";
    } else {
      return "BUYER";
    }
  }
}
