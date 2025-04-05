package liaison.groble.application.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.Role;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.RoleRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final RoleRepository roleRepository;
  private final IntegratedAccountRepository integratedAccountRepository;

  @Override
  @Transactional
  public TokenDto signUp(SignUpDto signUpDto) {
    // 통합 계정 이메일 중복 검사
    if (integratedAccountRepository.existsByIntegratedAccountEmail(signUpDto.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    // 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // IntegratedAccount 생성 (내부적으로 User 객체 생성 및 연결)
    IntegratedAccount integratedAccount =
        IntegratedAccount.createAccount(signUpDto.getEmail(), encodedPassword);

    User user = integratedAccount.getUser();

    // 기본 사용자 역할 추가
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);

    // 사용자 상태 활성화 설정
    user.updateStatus(UserStatus.ACTIVE);

    // 사용자 저장 (CascadeType.ALL로 IntegratedAccount도 함께 저장됨)
    User savedUser = userRepository.save(user);

    log.info("회원가입 완료: {}", savedUser.getEmail());

    // 토큰 생성
    String accessToken = securityPort.createAccessToken(savedUser.getId(), savedUser.getEmail());
    String refreshToken = securityPort.createRefreshToken(savedUser.getId(), savedUser.getEmail());

    // 리프레시 토큰 저장
    savedUser.updateRefreshToken(refreshToken);
    userRepository.save(savedUser);

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional
  public TokenDto signIn(SignInDto signInDto) {
    // 이메일로 IntegratedAccount 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(signInDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(signInDto.getPassword(), account.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = account.getUser();
    // 사용자 상태 확인 (로그인 가능 상태인지)
    if (!user.isLoginable()) {
      throw new IllegalArgumentException("로그인할 수 없는 계정 상태입니다: " + user.getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    log.info("로그인 성공: {}", user.getEmail());

    // 토큰 생성
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }
}
