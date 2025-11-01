package liaison.groble.mapping.purchase;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.purchase.response.PurchasedContentDetailResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PurchaseMapper extends PageResponseMapper {
  // ====== 📥 Request → DTO 변환 ======

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(target = "contactInfo", ignore = true)
  @Mapping(target = "myReview", ignore = true)
  PurchasedContentDetailResponse toPurchasedContentDetailResponse(
      PurchasedContentDetailDTO purchasedContentDetailDTO);

  // ContactInfo를 포함한 상세 응답 생성
  default PurchasedContentDetailResponse toPurchasedContentDetailResponse(
      PurchasedContentDetailDTO purchasedContentDetailDTO,
      ContactInfoResponse contactInfoResponse,
      ContentReviewDetailResponse contentReviewDetailResponse) {
    if (purchasedContentDetailDTO == null) {
      return null;
    }

    // 기본 매핑 사용
    PurchasedContentDetailResponse response =
        toPurchasedContentDetailResponse(purchasedContentDetailDTO);

    // Builder를 사용하여 contactInfo를 포함한 새로운 응답 생성
    return PurchasedContentDetailResponse.builder()
        .orderStatus(response.getOrderStatus())
        .merchantUid(response.getMerchantUid())
        .purchasedAt(response.getPurchasedAt())
        .cancelRequestedAt(response.getCancelRequestedAt())
        .cancelledAt(response.getCancelledAt())
        .contentId(response.getContentId())
        .sellerName(response.getSellerName())
        .contentTitle(response.getContentTitle())
        .selectedOptionName(response.getSelectedOptionName())
        .selectedOptionQuantity(response.getSelectedOptionQuantity())
        .selectedOptionType(response.getSelectedOptionType())
        .documentOptionActionUrl(response.getDocumentOptionActionUrl())
        .isFreePurchase(response.getIsFreePurchase())
        .originalPrice(response.getOriginalPrice())
        .discountPrice(response.getDiscountPrice())
        .finalPrice(response.getFinalPrice())
        .payType(response.getPayType())
        .payCardName(response.getPayCardName())
        .payCardNum(response.getPayCardNum())
        .paymentType(response.getPaymentType())
        .nextPaymentDate(response.getNextPaymentDate())
        .thumbnailUrl(response.getThumbnailUrl())
        .isRefundable(response.getIsRefundable())
        .isCancelable(response.getIsCancelable())
        .cancelReason(response.getCancelReason())
        .contactInfo(contactInfoResponse)
        .myReview(contentReviewDetailResponse)
        .build();
  }

  PurchaserContentPreviewCardResponse toPurchaserContentPreviewCardResponse(
      PurchaseContentCardDTO purchaseContentCardDTO);

  default PageResponse<PurchaserContentPreviewCardResponse>
      toPurchaserContentPreviewCardResponsePage(
          PageResponse<PurchaseContentCardDTO> DTOPageResponse) {
    return toPageResponse(DTOPageResponse, this::toPurchaserContentPreviewCardResponse);
  }
}
