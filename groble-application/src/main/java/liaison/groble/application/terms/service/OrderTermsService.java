package liaison.groble.application.terms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.terms.dto.TermsAgreementDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.terms.entity.GuestUserOrderTerms;
import liaison.groble.domain.terms.entity.OrderTerms;
import liaison.groble.domain.terms.entity.UserOrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;
import liaison.groble.domain.terms.repository.GuestUserOrderTermsRepository;
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
  private final GuestUserOrderTermsRepository guestUserOrderTermsRepository;
  private final UserReader userReader;
  private final GuestUserReader guestUserReader;

  /**
   * 회원 주문 약관 동의 처리
   *
   * @param dto 약관 동의 정보
   */
  @Transactional
  public void agreeToOrderTerms(TermsAgreementDTO dto) {
    User user = userReader.getUserById(dto.getUserId());
    processOrderTermsAgreementInternal(dto, user, null);
  }

  /**
   * 비회원 주문 약관 동의 처리
   *
   * @param dto 약관 동의 정보 (userId는 무시됨)
   * @param guestUserId 게스트 사용자 ID
   */
  @Transactional
  public void agreeToOrderTermsForGuest(TermsAgreementDTO dto, Long guestUserId) {
    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);
    processOrderTermsAgreementInternal(dto, null, guestUser);
  }

  /**
   * 주문 약관 동의 처리 공통 로직
   *
   * @param dto 약관 동의 정보
   * @param user 회원 (null 가능)
   * @param guestUser 비회원 (null 가능)
   */
  private void processOrderTermsAgreementInternal(
      TermsAgreementDTO dto, User user, GuestUser guestUser) {
    List<OrderTermsType> orderTermsTypes =
        dto.getTermsTypeStrings().stream().map(OrderTermsType::valueOf).toList();

    String userInfo = (user != null) ? "User " + user.getId() : "GuestUser " + guestUser.getId();
    log.info(
        "{} is agreeing to order terms: {}",
        userInfo,
        orderTermsTypes.stream().map(Enum::name).toList());

    List<OrderTerms> activeOrderTermsList =
        orderTermsRepository.findActiveOrderTermsByTypes(orderTermsTypes);

    if (activeOrderTermsList.isEmpty()) {
      throw new EntityNotFoundException("요청한 주문 약관 유형에 대한 활성화된 약관이 없습니다.");
    }

    // 모든 약관에 대해 동의 처리
    for (OrderTerms orderTerms : activeOrderTermsList) {
      if (user != null) {
        // 회원 약관 동의 처리
        UserOrderTerms agreement =
            processUserOrderTermsAgreement(
                user, orderTerms, true, dto.getIpAddress(), dto.getUserAgent());
        createOrderTermsAgreementDTO(agreement);
      } else {
        // 비회원 약관 동의 처리
        GuestUserOrderTerms agreement =
            processGuestUserOrderTermsAgreement(
                guestUser, orderTerms, true, dto.getIpAddress(), dto.getUserAgent());
        createGuestOrderTermsAgreementDTO(agreement);
      }
    }
  }

  /** 회원 약관 동의 또는 철회 처리 헬퍼 메서드 */
  private UserOrderTerms processUserOrderTermsAgreement(
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
              .agreedAt(LocalDateTime.now())
              .agreedIp(ipAddress)
              .agreedUserAgent(userAgent)
              .build();
    } else {
      // 기존 동의 업데이트
      agreement.updateAgreement(agreed, LocalDateTime.now(), ipAddress, userAgent);
    }

    return userOrderTermsRepository.save(agreement);
  }

  /** 비회원 약관 동의 또는 철회 처리 헬퍼 메서드 */
  private GuestUserOrderTerms processGuestUserOrderTermsAgreement(
      GuestUser guestUser,
      OrderTerms orderTerms,
      boolean agreed,
      String ipAddress,
      String userAgent) {
    GuestUserOrderTerms agreement =
        guestUserOrderTermsRepository
            .findByGuestUserIdAndOrderTermsId(guestUser.getId(), orderTerms.getId())
            .orElse(null);

    if (agreement == null) {
      // 새로운 동의 생성
      agreement =
          GuestUserOrderTerms.builder()
              .guestUser(guestUser)
              .orderTerms(orderTerms)
              .agreed(agreed)
              .agreedAt(LocalDateTime.now())
              .agreedIp(ipAddress)
              .agreedUserAgent(userAgent)
              .build();
    } else {
      // 기존 동의 업데이트
      agreement.updateAgreement(agreed, LocalDateTime.now(), ipAddress, userAgent);
    }

    return guestUserOrderTermsRepository.save(agreement);
  }

  /** 회원 약관 동의 DTO 생성 헬퍼 메서드 */
  private TermsAgreementDTO createOrderTermsAgreementDTO(UserOrderTerms agreement) {
    OrderTerms orderTerms = agreement.getOrderTerms();

    return TermsAgreementDTO.builder()
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

  /** 비회원 약관 동의 DTO 생성 헬퍼 메서드 */
  private TermsAgreementDTO createGuestOrderTermsAgreementDTO(GuestUserOrderTerms agreement) {
    OrderTerms orderTerms = agreement.getOrderTerms();

    return TermsAgreementDTO.builder()
        .id(orderTerms.getId())
        .userId(null) // 비회원은 userId 없음
        .typeString(orderTerms.getType().name())
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
