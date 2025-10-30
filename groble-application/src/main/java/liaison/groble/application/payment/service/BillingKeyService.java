package liaison.groble.application.payment.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.payment.dto.billing.BillingKeyInfoDTO;
import liaison.groble.application.payment.dto.billing.RegisterBillingKeyCommand;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.payment.entity.BillingKey;
import liaison.groble.domain.payment.enums.BillingKeyStatus;
import liaison.groble.domain.payment.repository.BillingKeyRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingKeyService {

  private final BillingKeyRepository billingKeyRepository;
  private final UserReader userReader;

  @Transactional
  public BillingKeyInfoDTO registerBillingKey(Long userId, RegisterBillingKeyCommand command) {
    User user = userReader.getUserById(userId);

    String billingKeyValue = normalize(command.getBillingKey());
    String cardName = normalize(command.getCardName());
    String cardNumberMasked = normalize(command.getCardNumberMasked());

    if (billingKeyValue == null || billingKeyValue.isBlank()) {
      throw new IllegalArgumentException("빌링키는 필수값입니다.");
    }

    billingKeyRepository
        .findByUserIdAndStatus(userId, BillingKeyStatus.ACTIVE)
        .ifPresent(
            active -> {
              active.deactivate();
              billingKeyRepository.save(active);
            });

    BillingKey billingKey = BillingKey.active(user, billingKeyValue, cardName, cardNumberMasked);
    BillingKey saved = billingKeyRepository.save(billingKey);

    return BillingKeyInfoDTO.builder()
        .billingKey(saved.getBillingKey())
        .cardName(saved.getCardName())
        .cardNumberMasked(saved.getCardNumberMasked())
        .build();
  }

  @Transactional(readOnly = true)
  public BillingKey getActiveBillingKey(Long userId) {
    return billingKeyRepository
        .findByUserIdAndStatus(userId, BillingKeyStatus.ACTIVE)
        .orElseThrow(() -> new IllegalStateException("등록된 정기결제 카드가 없습니다."));
  }

  @Transactional(readOnly = true)
  public Optional<BillingKey> findActiveBillingKey(Long userId) {
    return billingKeyRepository.findByUserIdAndStatus(userId, BillingKeyStatus.ACTIVE);
  }

  @Transactional(readOnly = true)
  public List<BillingKey> getBillingKeys(Long userId) {
    return billingKeyRepository.findByUserId(userId);
  }

  private String normalize(String value) {
    return value == null ? null : value.trim();
  }
}
