package liaison.groble.application.terms.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.terms.dto.TermsAgreementDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.entity.UserOrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;
import liaison.groble.domain.terms.repository.OrderTermsRepository;
import liaison.groble.domain.terms.repository.UserOrderTermsRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTermsService {
  private final OrderTermsRepository orderTermsRepository;
  private final UserOrderTermsRepository userOrderTermsRepository;
  private final UserReader userReader;

  @Transactional
  public void agreeToOrderTerms(TermsAgreementDto dto) {
    User user = userReader.getUserById(dto.getUserId());
    List<OrderTermsType> orderTermsTypes =
        dto.getTermsTypeStrings().stream().map(OrderTermsType::valueOf).toList();

    List<OrderTerms> activeOrderTermsList =
        orderTermsRepository.findActiveOrderTermsByTypes(orderTermsTypes);

    if (activeOrderTermsList.isEmpty()) {
      throw new EntityNotFoundException("요청한 주문 약관 유형에 대한 활성화된 약관이 없습니다.");
    }

    // 첫 번째 주문 약관을 기준으로 DTO를 작성합니다 (여러 약관 처리는 별도 고려 필요)
    OrderTerms firstOrderTerms = activeOrderTermsList.get(0);

    // 약관 동의 처리
    UserOrderTerms agreement =
        processOrderTermsAgreement(
            user, firstOrderTerms, true, dto.getIpAddress(), dto.getUserAgent());

    createOrderTermsAgreementDto(agreement);
  }

  // 약관 동의 또는 철회 처리 헬퍼 메서드
  private UserOrderTerms processOrderTermsAgreement(
      User user, OrderTerms orderTerms, boolean agreed, String ipAddress, String userAgent) {
    UserOrderTerms agreement =
        userOrderTermsRepository
            .findByUserIdAndOrderTermsId(user.getId(), orderTerms.getId())
            .orElse(null);

    if (agreement == null) {
      // 새로운 동의 생성
      agreement =
          UserOrderTerms.builder()
              .user(user)
              .orderTerms(orderTerms)
              .agreed(agreed)
              .agreedAt(Instant.now())
              .agreedIp(ipAddress)
              .agreedUserAgent(userAgent)
              .build();
    } else {
      // 기존 동의 업데이트
      agreement.updateAgreement(agreed, Instant.now(), ipAddress, userAgent);
    }

    return userOrderTermsRepository.save(agreement);
  }

  // 약관 동의 DTO 생성 헬퍼 메서드
  private TermsAgreementDto createOrderTermsAgreementDto(UserOrderTerms agreement) {
    OrderTerms orderTerms = agreement.getOrderTerms();

    return TermsAgreementDto.builder()
        .id(orderTerms.getId())
        .userId(agreement.getUser().getId())
        .typeString(orderTerms.getType().name()) // TermsType을 문자열로 변환
        .title(orderTerms.getTitle())
        .version(orderTerms.getVersion())
        .required(orderTerms.getType().isRequired())
        .contentUrl(orderTerms.getContentUrl())
        .agreed(agreement.isAgreed())
        .agreedAt(agreement.getAgreedAt())
        .ipAddress(agreement.getAgreedIp())
        .userAgent(agreement.getAgreedUserAgent())
        .effectiveFrom(orderTerms.getEffectiveFrom())
        .effectiveTo(orderTerms.getEffectiveTo())
        .build();
  }
}
