package liaison.groble.application.auth.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountAuthService {
  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;

  // Port
  private final SecurityPort securityPort;
  private final VerificationCodePort verificationCodePort;

  //    @Transactional
  //    public SignUpAuthResultDTO socialSignUp(Long userId, SocialBasicInfoDto socialBasicInfoDto)
  // {
  //
  //    }
}
