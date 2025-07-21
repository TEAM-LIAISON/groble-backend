package liaison.groble.domain.user.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.entity.UserTerms;
import liaison.groble.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
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
}
