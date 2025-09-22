package liaison.groble.application.purchase.strategy;

import org.springframework.data.domain.Pageable;

import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.strategy.UserTypeProcessor;

/** 구매 처리 전략 인터페이스 회원/비회원에 따른 구매 콘텐츠 조회 로직을 추상화합니다. */
public interface PurchaseProcessorStrategy extends UserTypeProcessor {

  /** 판매자 연락처 정보 조회 */
  ContactInfoDTO getContactInfo(UserContext userContext, String merchantUid);

  /** 구매한 콘텐츠 목록 조회 */
  PageResponse<PurchaseContentCardDTO> getMyPurchasedContents(
      UserContext userContext, String state, Pageable pageable);

  /** 구매한 콘텐츠 상세 조회 */
  PurchasedContentDetailDTO getMyPurchasedContent(UserContext userContext, String merchantUid);

  /** 구매한 콘텐츠 리뷰 상세 조회 */
  ContentReviewDetailDTO getContentReviewDetail(UserContext userContext, String merchantUid);
}
