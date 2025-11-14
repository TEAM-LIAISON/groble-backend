package liaison.groble.application.content.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentPayPageDTO;
import liaison.groble.application.coupon.service.CouponService;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.service.SubscriptionPaymentMetadataProvider;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.content.entity.ContentOption;
import liaison.groble.domain.content.enums.ContentPaymentType;
import liaison.groble.domain.content.enums.SubscriptionSellStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentPaymentService {
  private static final ZoneId BILLING_ZONE_ID = ZoneId.of("Asia/Seoul");

  private static final int MONTHLY_BILLING_INTERVAL = 1;
  // Reader
  private final ContentReader contentReader;

  // Service
  private final CouponService couponService;
  private final SubscriptionPaymentMetadataProvider subscriptionPaymentMetadataProvider;

  @Transactional(readOnly = true)
  public ContentPayPageDTO getContentPayPage(Long userId, Long contentId, Long optionId) {
    // 1. 콘텐츠 조회
    Content content = contentReader.getContentById(contentId);

    // 2. 콘텐츠 옵션 조회
    ContentOption contentOption = findContentOptionById(content, optionId);

    if (content.getPaymentType() == ContentPaymentType.SUBSCRIPTION) {
      validateSubscriptionSellStatus(content);
    }

    boolean isLoggedIn = userId != null;
    List<ContentPayPageDTO.UserCouponDTO> userCoupons =
        isLoggedIn ? couponService.getUserCoupons(userId) : Collections.emptyList();

    // 3. 응답 DTO 생성
    return buildContentPayPageDTO(userId, isLoggedIn, content, contentOption, userCoupons);
  }

  private ContentPayPageDTO buildContentPayPageDTO(
      Long userId,
      boolean isLoggedIn,
      Content content,
      ContentOption contentOption,
      List<ContentPayPageDTO.UserCouponDTO> userCoupons) {
    ContentPayPageDTO.SubscriptionMetaDTO subscriptionMeta =
        buildSubscriptionMeta(userId, content, contentOption);
    LocalDate nextPaymentDate =
        subscriptionMeta != null
            ? subscriptionMeta.getNextPaymentDate()
            : determineNextPaymentDate(content);

    return ContentPayPageDTO.builder()
        .isLoggedIn(isLoggedIn)
        .thumbnailUrl(content.getThumbnailUrl())
        .sellerName(content.getUser().getNickname())
        .title(content.getTitle())
        .contentType(content.getContentType().name())
        .paymentType(content.getPaymentType() != null ? content.getPaymentType().name() : null)
        .nextPaymentDate(nextPaymentDate)
        .subscriptionMeta(subscriptionMeta)
        .optionName(contentOption.getName())
        .price(contentOption.getPrice())
        .userCoupons(userCoupons)
        .build();
  }

  /**
   * Content의 옵션 리스트에서 특정 optionId에 해당하는 ContentOption을 찾습니다.
   *
   * @param content 콘텐츠 엔티티
   * @param optionId 찾을 옵션 ID
   * @return 해당하는 ContentOption
   * @throws EntityNotFoundException 옵션을 찾을 수 없는 경우
   */
  private ContentOption findContentOptionById(Content content, Long optionId) {
    return content.getOptions().stream()
        .filter(option -> option.getId().equals(optionId))
        .findFirst()
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "콘텐츠 옵션을 찾을 수 없습니다. ContentId: "
                        + content.getId()
                        + ", OptionId: "
                        + optionId));
  }

  private void validateSubscriptionSellStatus(Content content) {
    SubscriptionSellStatus sellStatus = content.getSubscriptionSellStatus();
    if (sellStatus == SubscriptionSellStatus.PAUSED) {
      throw new IllegalStateException("정기결제 신규 신청이 일시 중단된 콘텐츠입니다.");
    }
    if (sellStatus == SubscriptionSellStatus.TERMINATED) {
      throw new IllegalStateException("정기결제가 종료된 콘텐츠입니다.");
    }
  }

  private LocalDate determineNextPaymentDate(Content content) {
    if (content == null) {
      return null;
    }

    ContentPaymentType paymentType = content.getPaymentType();
    if (paymentType != ContentPaymentType.SUBSCRIPTION) {
      return null;
    }

    LocalDate todayInBillingZone = LocalDate.now(BILLING_ZONE_ID);
    return todayInBillingZone.plusMonths(MONTHLY_BILLING_INTERVAL);
  }

  private ContentPayPageDTO.SubscriptionMetaDTO buildSubscriptionMeta(
      Long userId, Content content, ContentOption option) {
    return subscriptionPaymentMetadataProvider
        .buildForContent(userId, content, option)
        .map(this::toSubscriptionMetaDTO)
        .orElse(null);
  }

  private ContentPayPageDTO.SubscriptionMetaDTO toSubscriptionMetaDTO(
      SubscriptionPaymentMetadata metadata) {
    return ContentPayPageDTO.SubscriptionMetaDTO.builder()
        .hasActiveBillingKey(metadata.isHasActiveBillingKey())
        .billingKeyId(metadata.getBillingKeyId())
        .merchantUserKey(metadata.getMerchantUserKey())
        .defaultPayMethod(metadata.getDefaultPayMethod())
        .payWork(metadata.getPayWork())
        .cardVer(metadata.getCardVer())
        .regularFlag(metadata.getRegularFlag())
        .nextPaymentDate(metadata.getNextPaymentDate())
        .payYear(metadata.getPayYear())
        .payMonth(metadata.getPayMonth())
        .payDay(metadata.getPayDay())
        .requiresImmediateCharge(metadata.isRequiresImmediateCharge())
        .build();
  }
}
