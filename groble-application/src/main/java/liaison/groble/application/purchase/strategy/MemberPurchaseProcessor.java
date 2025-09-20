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

/** 회원 구매 처리 전략 구현체 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPurchaseProcessor implements PurchaseProcessorStrategy {

  private final PurchaseService purchaseService;
  private final SellContentService sellContentService;

  @Override
  public String getSupportedUserType() {
    return "MEMBER";
  }

  @Override
  public ContactInfoDTO getContactInfo(UserContext userContext, String merchantUid) {
    Long userId = userContext.getId();
    log.info("회원 판매자 연락처 조회 - userId: {}, merchantUid: {}", userId, merchantUid);
    return purchaseService.getContactInfo(userId, merchantUid);
  }

  @Override
  public PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      UserContext userContext, String state, Pageable pageable) {
    Long userId = userContext.getId();
    log.info("회원 구매 목록 조회 - userId: {}, state: {}", userId, state);
    return purchaseService.getMyPurchasedContents(userId, state, pageable);
  }

  @Override
  public PurchasedContentDetailDTO getMyPurchasedContent(
      UserContext userContext, String merchantUid) {
    Long userId = userContext.getId();
    log.info("회원 구매 콘텐츠 상세 조회 - userId: {}, merchantUid: {}", userId, merchantUid);
    return purchaseService.getMyPurchasedContent(userId, merchantUid);
  }

  @Override
  public ContentReviewDetailDTO getContentReviewDetail(
      UserContext userContext, String merchantUid) {
    Long userId = userContext.getId();
    log.info("회원 콘텐츠 리뷰 상세 조회 - userId: {}, merchantUid: {}", userId, merchantUid);
    return sellContentService.getContentReviewDetail(userId, merchantUid);
  }
}
