package liaison.groble.persistence.role.initializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    log.info("Initializing role data...");
    initializeRoleData();
    log.info("Role data initialization completed");
  }

  @Transactional
  protected void initializeRoleData() {
    try {
      // 기존 데이터 조회
      List<Role> existingRoles = roleRepository.findAll();
      Map<String, Role> existingRoleMap =
          existingRoles.stream().collect(Collectors.toMap(Role::getName, role -> role));

      List<Role> newRoles = new ArrayList<>();

      // 필수 역할 정의
      String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN"};

      for (String roleName : requiredRoles) {
        if (!existingRoleMap.containsKey(roleName)) {
          // 새로운 역할 추가
          newRoles.add(Role.builder().name(roleName).build());
        }
      }

      // 새 역할 저장
      if (!newRoles.isEmpty()) {
        roleRepository.saveAll(newRoles);
        log.info("Added {} new roles", newRoles.size());
      } else {
        log.info("Role data is already up to date");
      }
    } catch (Exception e) {
      log.error("Error initializing role data", e);
    }
  }
}
