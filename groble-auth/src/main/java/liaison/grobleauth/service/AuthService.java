package liaison.grobleauth.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.grobleauth.dto.AuthDto;
import liaison.groblecore.domain.Role;
import liaison.groblecore.domain.RoleType;
import liaison.groblecore.domain.User;
import liaison.groblecore.repository.RoleRepository;
import liaison.groblecore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 회원가입, 로그인, 토큰 갱신, 로그아웃 등의 기능 제공 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  /**
   * 회원가입 처리
   *
   * @param request 회원가입 요청 정보
   * @return 회원가입 성공 여부
   */
  @Transactional
  public boolean signup(AuthDto.SignupRequest request) {
    // 이메일 중복 검사
    if (userRepository.existsByEmail(request.getEmail())) {
      log.warn("이메일 중복: {}", request.getEmail());
      return false;
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 사용자 생성
    User user = User.createUser(request.getEmail(), encodedPassword);

    // 기본 역할 설정 (ROLE_USER)
    Role userRole =
        roleRepository
            .findByName(RoleType.ROLE_USER.name())
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);

    user.addRole(userRole);

    // 사용자 저장
    userRepository.save(user);
    log.info("회원가입 완료: {}", request.getEmail());

    return true;
  }
}
