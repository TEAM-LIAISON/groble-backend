package liaison.groble.application.terms.service.impl;

import static liaison.groble.domain.terms.enums.TermsType.MARKETING_POLICY;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.helper.UserHelper;
import liaison.groble.application.terms.dto.MakerTermsAgreementDTO;
import liaison.groble.application.terms.service.TermsService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {
  private final TermsRepository termsRepository;
  private final UserRepository userRepository;

  private final UserReader userReader;
  private final UserHelper userHelper;

  @Override
  public boolean getAdvertisingAgreementStatus(Long userId) {
    User user = userReader.getUserById(userId);
    return user.hasAgreedToAdvertising();
  }

  @Override
  public void updateAdvertisingAgreementStatus(
      Long userId, boolean agreed, String ipAddress, String userAgent) {
    User user = userReader.getUserById(userId);

    Terms advertisingTerms =
        termsRepository
            .findLatestByTypeAndEffectiveAt(MARKETING_POLICY, LocalDateTime.now())
            .orElseThrow(() -> new IllegalStateException("현재 유효한 광고성 정보 약관이 없습니다."));

    user.updateAdvertisingAgreement(advertisingTerms, agreed, ipAddress, userAgent);

    userRepository.save(user);
  }

  @Override
  @Transactional
  public MakerTermsAgreementDTO agreeMakerTerms(
      Long userId, MakerTermsAgreementDTO agreementDTO, String clientIp, String userAgent) {
    log.info("메이커 이용약관 동의 처리: userId={}", userId);

    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 현재 유효한 메이커 약관 조회
    Terms currentMakerTerms =
        termsRepository
            .findLatestByTypeAndEffectiveAt(TermsType.SELLER_TERMS_POLICY, LocalDateTime.now())
            .orElseThrow(() -> new IllegalStateException("현재 유효한 메이커 약관을 찾을 수 없습니다."));

    // 3. 이미 동의한 사용자인지 확인
    if (user.isMakerTermsAgreed()) {
      log.info("이미 메이커 약관에 동의한 사용자: userId={}", userId);

      return MakerTermsAgreementDTO.builder().makerTermsAgreement(true).build();
    }

    // 4. 메이커 약관 동의 처리
    user.updateMakerTermsAgreement(currentMakerTerms, true, clientIp, userAgent);
    user.setSeller(true);
    user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.PENDING));

    userHelper.addSellerRole(user);

    // 5. 사용자 저장
    User savedUser = userRepository.save(user);

    log.info("메이커 이용약관 동의 완료: userId={}", savedUser.getId());

    // 6. 결과 반환
    return MakerTermsAgreementDTO.builder().makerTermsAgreement(true).build();
  }
}
