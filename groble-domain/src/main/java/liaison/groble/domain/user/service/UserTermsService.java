package liaison.groble.domain.user.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.UserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Service // 스프링 빈으로 등록
@Slf4j // 로깅 추가
public class UserTermsService {
  public void agreeToTerms(User user, Terms terms, String agreedIp, String agreedUserAgent) {
    log.info("약관 동의 처리 시작 - 사용자: {}, 약관 유형: {}", user.getId(), terms.getType());

    // agreedAt 필드 추가 (누락된 것으로 보임)
    UserTerms agreement =
        UserTerms.builder()
            .user(user)
            .terms(terms)
            .agreed(true)
            .agreedAt(Instant.now()) // 동의 시간 추가
            .agreedIp(agreedIp)
            .agreedUserAgent(agreedUserAgent)
            .build();

    user.getTermsAgreements().add(agreement);
    log.info("약관 동의 정보 생성 완료 - 사용자: {}, 약관 ID: {}", user.getId(), terms.getId());
  }

  public boolean hasAgreedTo(User user, TermsType termsType) {
    return user.getTermsAgreements().stream()
        .anyMatch(
            agreement ->
                agreement.getTerms().getType() == termsType
                    && agreement.isAgreed()
                    && agreement.getTerms().getEffectiveTo() == null);
  }

  public boolean hasAgreedToAllRequiredTerms(User user) {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .allMatch(type -> hasAgreedTo(user, type));
  }

  public List<TermsType> getMissingRequiredTerms(User user) {
    return Arrays.stream(TermsType.values())
        .filter(TermsType::isRequired)
        .filter(type -> !hasAgreedTo(user, type))
        .collect(Collectors.toList());
  }

  public boolean hasAgreedToAdvertising(User user) {
    return hasAgreedTo(user, TermsType.MARKETING_POLICY);
  }

  public void updateAdvertisingAgreement(
      User user, Terms advertisingTerms, boolean agreed, String ip, String userAgent) {

    UserTerms existingAgreement =
        user.getTermsAgreements().stream()
            .filter(a -> a.getTerms().equals(advertisingTerms))
            .findFirst()
            .orElse(null);

    if (existingAgreement != null) {
      existingAgreement.updateAgreement(agreed, Instant.now(), ip, userAgent);
    } else {
      UserTerms newAgreement =
          UserTerms.builder()
              .user(user)
              .terms(advertisingTerms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ip)
              .agreedUserAgent(userAgent)
              .build();

      user.getTermsAgreements().add(newAgreement);
    }
  }
}
