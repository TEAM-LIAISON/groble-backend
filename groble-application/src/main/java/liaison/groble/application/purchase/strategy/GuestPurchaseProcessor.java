package liaison.groble.application.purchase.strategy;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.application.purchase.service.PurchaseService;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.service.SellContentService;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.response.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 비회원 구매 처리 전략 구현체 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestPurchaseProcessor implements PurchaseProcessorStrategy {

  private final PurchaseService purchaseService;
  private final SellContentService sellContentService;

  @Override
  public String getSupportedUserType() {
    return "GUEST";
  }

  @Override
  public ContactInfoDTO getContactInfo(UserContext userContext, String merchantUid) {
    Long guestUserId = userContext.getId();
    log.info("비회원 판매자 연락처 조회 - guestUserId: {}, merchantUid: {}", guestUserId, merchantUid);
    return purchaseService.getContactInfoForGuest(guestUserId, merchantUid);
  }

  @Override
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      UserContext userContext, String state, Pageable pageable) {
    Long guestUserId = userContext.getId();
    log.info("비회원 구매 목록 조회 - guestUserId: {}, state: {}", guestUserId, state);
    return purchaseService.getMyPurchasedContentsForGuest(guestUserId, state, pageable);
  }

  @Override
  public PurchasedContentDetailDTO getMyPurchasedContent(
      UserContext userContext, String merchantUid) {
    Long guestUserId = userContext.getId();
    log.info("비회원 구매 콘텐츠 상세 조회 - guestUserId: {}, merchantUid: {}", guestUserId, merchantUid);
    return purchaseService.getMyPurchasedContentForGuest(guestUserId, merchantUid);
  }

  @Override
  public ContentReviewDetailDTO getContentReviewDetail(
      UserContext userContext, String merchantUid) {
    Long guestUserId = userContext.getId();
    log.info("비회원 콘텐츠 리뷰 상세 조회 - guestUserId: {}, merchantUid: {}", guestUserId, merchantUid);
    return sellContentService.getContentReviewDetailForGuest(guestUserId, merchantUid);
  }
}
