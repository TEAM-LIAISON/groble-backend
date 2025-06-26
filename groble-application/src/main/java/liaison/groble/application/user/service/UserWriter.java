package liaison.groble.application.user.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserWriter {
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;

  @Transactional
  public void updateIntegratedAccountEmail(IntegratedAccount account, String newEmail) {
    account.updateEmail(newEmail);
    integratedAccountRepository.save(account);
  }

  @Transactional
  public void updateSocialAccountEmail(SocialAccount account, String newEmail) {
    account.updateEmail(newEmail);
    socialAccountRepository.save(account);
  }

  @Transactional
  public void updateIntegratedAccountPassword(IntegratedAccount account, String encodedPassword) {
    account.updatePassword(encodedPassword);
    integratedAccountRepository.save(account);
  }
}
