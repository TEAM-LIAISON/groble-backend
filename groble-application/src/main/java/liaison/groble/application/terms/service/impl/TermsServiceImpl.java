package liaison.groble.application.terms.service.impl;

import static liaison.groble.domain.terms.enums.TermsType.MARKETING_POLICY;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.terms.dto.MakerTermsAgreementDto;
import liaison.groble.application.terms.dto.TermsAgreementDto;
import liaison.groble.application.terms.service.TermsService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.exception.ForbiddenException;
import liaison.groble.domain.terms.entity.Terms;
import liaison.groble.domain.terms.entity.UserTerms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;
import liaison.groble.domain.terms.repository.UserTermsRepository;
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
  private final UserTermsRepository userTermsRepository;
  private final UserReader userReader;

  @Transactional
  public TermsAgreementDto agreeToTerms(TermsAgreementDto dto) {
    User user = userReader.getUserById(dto.getUserId());
    // 문자열 타입을 domain TermsType으로 변환
    List<TermsType> termsTypes =
        dto.getTermsTypeStrings().stream().map(TermsType::valueOf).collect(Collectors.toList());

    List<Terms> activeTermsList = termsRepository.findActiveTermsByTypes(termsTypes);

    if (activeTermsList.isEmpty()) {
      throw new EntityNotFoundException("요청한 약관 유형에 대한 활성화된 약관이 없습니다.");
    }

    // 첫 번째 약관을 기준으로 DTO를 작성합니다 (여러 약관 처리는 별도 고려 필요)
    Terms firstTerms = activeTermsList.get(0);

    // 약관 동의 처리
    UserTerms agreement =
        processTermsAgreement(user, firstTerms, true, dto.getIpAddress(), dto.getUserAgent());

    // 응답 DTO 구성
    return createTermsAgreementDto(agreement);
  }

  @Transactional
  public TermsAgreementDto withdrawTermsAgreement(TermsAgreementDto dto) {
    User user =
        userRepository
            .findById(dto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + dto.getUserId()));

    // 문자열 타입을 domain TermsType으로 변환
    List<TermsType> termsTypes =
        dto.getTermsTypeStrings().stream().map(TermsType::valueOf).collect(Collectors.toList());

    // 필수 약관 철회 시도 체크
    for (TermsType type : termsTypes) {
      if (type.isRequired()) {
        throw new ForbiddenException("필수 약관은 철회할 수 없습니다: " + type.name());
      }
    }

    List<Terms> termsList = termsRepository.findByTypesIn(termsTypes);

    if (termsList.isEmpty()) {
      throw new EntityNotFoundException("요청한 약관 유형에 대한 약관이 없습니다.");
    }

    // 첫 번째 약관을 기준으로 DTO를 작성
    Terms firstTerms = termsList.get(0);

    // 약관 철회 처리
    UserTerms agreement =
        processTermsAgreement(user, firstTerms, false, dto.getIpAddress(), dto.getUserAgent());

    // 응답 DTO 구성
    return createTermsAgreementDto(agreement);
  }

  @Transactional(readOnly = true)
  public List<TermsAgreementDto> getUserTermsAgreements(Long userId) {
    List<UserTerms> agreements = userTermsRepository.findByUserId(userId);

    return agreements.stream().map(this::createTermsAgreementDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<TermsAgreementDto> getActiveTerms() {
    List<Terms> activeTerms = termsRepository.findActiveTerms();

    return activeTerms.stream().map(this::createTermsDto).collect(Collectors.toList());
  }

  // 약관 동의 또는 철회 처리 헬퍼 메서드
  private UserTerms processTermsAgreement(
      User user, Terms terms, boolean agreed, String ipAddress, String userAgent) {
    UserTerms agreement =
        userTermsRepository.findByUserIdAndTermsId(user.getId(), terms.getId()).orElse(null);

    if (agreement == null) {
      // 새로운 동의 생성
      agreement =
          UserTerms.builder()
              .user(user)
              .terms(terms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ipAddress)
              .agreedUserAgent(userAgent)
              .build();
    } else {
      // 기존 동의 업데이트
      agreement.updateAgreement(agreed, Instant.now(), ipAddress, userAgent);
    }

    return userTermsRepository.save(agreement);
  }

  // 약관 동의 DTO 생성 헬퍼 메서드
  private TermsAgreementDto createTermsAgreementDto(UserTerms agreement) {
    Terms terms = agreement.getTerms();

    return TermsAgreementDto.builder()
        .id(terms.getId())
        .userId(agreement.getUser().getId())
        .typeString(terms.getType().name()) // TermsType을 문자열로 변환
        .title(terms.getTitle())
        .version(terms.getVersion())
        .required(terms.getType().isRequired())
        .contentUrl(terms.getContentUrl())
        .agreed(agreement.isAgreed())
        .agreedAt(agreement.getAgreedAt())
        .ipAddress(agreement.getAgreedIp())
        .userAgent(agreement.getAgreedUserAgent())
        .effectiveFrom(terms.getEffectiveFrom())
        .effectiveTo(terms.getEffectiveTo())
        .build();
  }

  // 약관 DTO 생성 헬퍼 메서드 (동의 정보 없는 순수 약관 정보)
  private TermsAgreementDto createTermsDto(Terms terms) {
    return TermsAgreementDto.builder()
        .id(terms.getId())
        .typeString(terms.getType().name()) // TermsType을 문자열로 변환
        .title(terms.getTitle())
        .version(terms.getVersion())
        .required(terms.getType().isRequired())
        .contentUrl(terms.getContentUrl())
        .agreed(false) // 기본값
        .effectiveFrom(terms.getEffectiveFrom())
        .effectiveTo(terms.getEffectiveTo())
        .build();
  }

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
  public MakerTermsAgreementDto agreeMakerTerms(
      MakerTermsAgreementDto agreementDto, String clientIp, String userAgent) {
    log.info("메이커 이용약관 동의 처리: userId={}", agreementDto.getUserId());

    // 1. 사용자 조회
    User user = userReader.getUserById(agreementDto.getUserId());

    // 2. 현재 유효한 메이커 약관 조회
    Terms currentMakerTerms =
        termsRepository
            .findLatestByTypeAndEffectiveAt(TermsType.SELLER_TERMS_POLICY, LocalDateTime.now())
            .orElseThrow(() -> new IllegalStateException("현재 유효한 메이커 약관을 찾을 수 없습니다."));

    // 3. 이미 동의한 사용자인지 확인
    if (user.isMakerTermsAgreed()) {
      log.info("이미 메이커 약관에 동의한 사용자: userId={}", agreementDto.getUserId());

      return MakerTermsAgreementDto.builder()
          .userId(user.getId())
          .makerTermsAgreement(true)
          .build();
    }

    // 4. 메이커 약관 동의 처리
    user.updateMakerTermsAgreement(currentMakerTerms, true, clientIp, userAgent);
    user.setSeller(true);
    user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.PENDING));

    // 5. 사용자 저장
    User savedUser = userRepository.save(user);

    log.info("메이커 이용약관 동의 완료: userId={}", savedUser.getId());

    // 6. 결과 반환
    return MakerTermsAgreementDto.builder()
        .userId(savedUser.getId())
        .makerTermsAgreement(true)
        .build();
  }
}
