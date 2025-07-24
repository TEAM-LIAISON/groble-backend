package liaison.groble.application.auth.helper;

import org.springframework.stereotype.Component;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserHelper {

  private final RoleRepository roleRepository;

  /** 기본 역할 추가 */
  public void addDefaultRole(User user) {
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new EntityNotFoundException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);
  }

  public void addSellerRole(User user) {
    Role roleSeller =
        roleRepository
            .findByName("ROLE_SELLER")
            .orElseThrow(() -> new EntityNotFoundException("메이커 역할(ROLE_SELLER)을 찾을 수 없습니다."));

    boolean hasRole =
        user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().equals(roleSeller));

    if (!hasRole) {
      user.addRole(roleSeller);
    }
  }
}
