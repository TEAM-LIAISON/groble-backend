package liaison.groble.application.auth.helper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import liaison.groble.common.request.RequestUtil;
import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.service.UserTermsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TermsHelper {
  private final TermsRepository termsRepository;
  private final UserTermsService userTermsService;
  private final RequestUtil requestUtil;

  /** 문자열 약관 유형 리스트를 TermsType enum 리스트로 변환 */
  public List<TermsType> convertToTermsTypes(List<String> termsTypeStrings) {
    if (termsTypeStrings == null || termsTypeStrings.isEmpty()) {
      return Collections.emptyList();
    }
    return termsTypeStrings.stream().map(this::parseTermsType).collect(Collectors.toList());
  }

  /** 문자열 약관 유형을 TermsType enum으로 변환 */
  private TermsType parseTermsType(String termsTypeString) {
    try {
      return TermsType.valueOf(termsTypeString.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 약관 유형입니다: " + termsTypeString);
    }
  }

  /** 필수 약관 동의 여부 검증 */
  public void validateRequiredTermsAgreement(List<TermsType> agreedTermsTypes, UserType userType) {
    // 1. 유저 타입에 맞는 필수 약관 목록 정의
    List<TermsType> requiredTermsTypes =
        Arrays.stream(TermsType.values())
            .filter(
                termsType ->
                    termsType.isRequired()
                        && (termsType != TermsType.SELLER_TERMS_POLICY
                            || userType == UserType.SELLER))
            .toList();

    // 2. 필수 약관 중 누락된 항목 검증
    List<TermsType> missingRequiredTerms =
        requiredTermsTypes.stream().filter(req -> !agreedTermsTypes.contains(req)).toList();

    if (!missingRequiredTerms.isEmpty()) {
      String missingTerms =
          missingRequiredTerms.stream()
              .map(TermsType::getDescription)
              .collect(Collectors.joining(", "));
      throw new IllegalArgumentException("다음 필수 약관에 동의해주세요: " + missingTerms);
    }

    // 3. Buyer인데 SELLER_TERMS_POLICY에 동의한 경우 에러
    if (userType == UserType.BUYER && agreedTermsTypes.contains(TermsType.SELLER_TERMS_POLICY)) {
      throw new IllegalArgumentException("구매자는 판매자 이용약관에 동의할 수 없습니다.");
    }
  }

  /**
   * 사용자의 약관 동의 정보를 처리합니다.
   *
   * @param user 사용자 엔티티
   * @param agreedTermsTypes 동의한 약관 유형 리스트
   */
  public void processTermsAgreements(User user, List<TermsType> agreedTermsTypes) {
    log.info("약관 동의 처리 시작 - 사용자 ID: {}, 동의한 약관 수: {}", user.getId(), agreedTermsTypes.size());

    // 현재 IP 주소와 User-Agent 정보 가져오기
    String clientIp = requestUtil.getClientIp();
    String userAgent = requestUtil.getUserAgent();
    log.debug("클라이언트 정보 - IP: {}, UserAgent: {}", clientIp, userAgent);

    try {
      // 현재 유효한 최신 약관 조회
      List<Terms> latestTerms = termsRepository.findAllLatestTerms(LocalDateTime.now());
      log.info("최신 약관 조회 완료 - 약관 수: {}", latestTerms.size());

      Map<TermsType, Terms> latestTermsMap =
          latestTerms.stream().collect(Collectors.toMap(Terms::getType, terms -> terms));

      log.debug(
          "약관 유형별 매핑 완료: {}",
          latestTermsMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));

      // 약관 동의 처리
      for (TermsType termsType : TermsType.values()) {
        Terms terms = latestTermsMap.get(termsType);
        if (terms != null) {
          boolean agreed = agreedTermsTypes.contains(termsType);
          log.debug("약관 처리 - 유형: {}, 동의 여부: {}, 약관ID: {}", termsType, agreed, terms.getId());

          // 동의한 약관에만 동의 정보 추가
          if (agreed) {
            userTermsService.agreeToTerms(user, terms, clientIp, userAgent);
            log.debug("약관 {} 동의 정보 추가 완료", termsType);
          }
        } else {
          log.warn("약관 유형 {}에 해당하는 최신 약관을 찾을 수 없습니다", termsType);
        }
      }

      // 약관 동의 정보가 사용자 객체에 제대로 추가되었는지 확인
      log.info("사용자의 약관 동의 정보 수: {}", user.getTermsAgreements().size());

    } catch (Exception e) {
      log.error("약관 동의 처리 중 오류 발생", e);
      throw e;
    }

    log.info("약관 동의 처리 완료");
  }
}
