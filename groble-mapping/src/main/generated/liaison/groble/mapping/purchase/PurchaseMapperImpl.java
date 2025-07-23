package liaison.groble.mapping.purchase;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.purchase.response.PurchasedContentDetailResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-23T20:36:40+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class PurchaseMapperImpl implements PurchaseMapper {

  @Override
  public PurchasedContentDetailResponse toPurchasedContentDetailResponse(
      PurchasedContentDetailDTO purchasedContentDetailDTO) {
    if (purchasedContentDetailDTO == null) {
      return null;
    }

    PurchasedContentDetailResponse.PurchasedContentDetailResponseBuilder
        purchasedContentDetailResponse = PurchasedContentDetailResponse.builder();

    if (purchasedContentDetailDTO.getOrderStatus() != null) {
      purchasedContentDetailResponse.orderStatus(purchasedContentDetailDTO.getOrderStatus());
    }
    if (purchasedContentDetailDTO.getMerchantUid() != null) {
      purchasedContentDetailResponse.merchantUid(purchasedContentDetailDTO.getMerchantUid());
    }
    if (purchasedContentDetailDTO.getPurchasedAt() != null) {
      purchasedContentDetailResponse.purchasedAt(purchasedContentDetailDTO.getPurchasedAt());
    }
    if (purchasedContentDetailDTO.getCancelRequestedAt() != null) {
      purchasedContentDetailResponse.cancelRequestedAt(
          purchasedContentDetailDTO.getCancelRequestedAt());
    }
    if (purchasedContentDetailDTO.getCancelledAt() != null) {
      purchasedContentDetailResponse.cancelledAt(purchasedContentDetailDTO.getCancelledAt());
    }
    if (purchasedContentDetailDTO.getContentId() != null) {
      purchasedContentDetailResponse.contentId(purchasedContentDetailDTO.getContentId());
    }
    if (purchasedContentDetailDTO.getSellerName() != null) {
      purchasedContentDetailResponse.sellerName(purchasedContentDetailDTO.getSellerName());
    }
    if (purchasedContentDetailDTO.getContentTitle() != null) {
      purchasedContentDetailResponse.contentTitle(purchasedContentDetailDTO.getContentTitle());
    }
    if (purchasedContentDetailDTO.getSelectedOptionName() != null) {
      purchasedContentDetailResponse.selectedOptionName(
          purchasedContentDetailDTO.getSelectedOptionName());
    }
    if (purchasedContentDetailDTO.getSelectedOptionQuantity() != null) {
      purchasedContentDetailResponse.selectedOptionQuantity(
          purchasedContentDetailDTO.getSelectedOptionQuantity());
    }
    if (purchasedContentDetailDTO.getSelectedOptionType() != null) {
      purchasedContentDetailResponse.selectedOptionType(
          purchasedContentDetailDTO.getSelectedOptionType());
    }
    if (purchasedContentDetailDTO.getDocumentOptionActionUrl() != null) {
      purchasedContentDetailResponse.documentOptionActionUrl(
          purchasedContentDetailDTO.getDocumentOptionActionUrl());
    }
    if (purchasedContentDetailDTO.getIsFreePurchase() != null) {
      purchasedContentDetailResponse.isFreePurchase(purchasedContentDetailDTO.getIsFreePurchase());
    }
    if (purchasedContentDetailDTO.getOriginalPrice() != null) {
      purchasedContentDetailResponse.originalPrice(purchasedContentDetailDTO.getOriginalPrice());
    }
    if (purchasedContentDetailDTO.getDiscountPrice() != null) {
      purchasedContentDetailResponse.discountPrice(purchasedContentDetailDTO.getDiscountPrice());
    }
    if (purchasedContentDetailDTO.getFinalPrice() != null) {
      purchasedContentDetailResponse.finalPrice(purchasedContentDetailDTO.getFinalPrice());
    }
    if (purchasedContentDetailDTO.getPayType() != null) {
      purchasedContentDetailResponse.payType(purchasedContentDetailDTO.getPayType());
    }
    if (purchasedContentDetailDTO.getPayCardName() != null) {
      purchasedContentDetailResponse.payCardName(purchasedContentDetailDTO.getPayCardName());
    }
    if (purchasedContentDetailDTO.getPayCardNum() != null) {
      purchasedContentDetailResponse.payCardNum(purchasedContentDetailDTO.getPayCardNum());
    }
    if (purchasedContentDetailDTO.getThumbnailUrl() != null) {
      purchasedContentDetailResponse.thumbnailUrl(purchasedContentDetailDTO.getThumbnailUrl());
    }
    if (purchasedContentDetailDTO.getIsRefundable() != null) {
      purchasedContentDetailResponse.isRefundable(purchasedContentDetailDTO.getIsRefundable());
    }
    if (purchasedContentDetailDTO.getCancelReason() != null) {
      purchasedContentDetailResponse.cancelReason(purchasedContentDetailDTO.getCancelReason());
    }

    return purchasedContentDetailResponse.build();
  }

  @Override
  public PurchaserContentPreviewCardResponse toPurchaserContentPreviewCardResponse(
      PurchaseContentCardDTO purchaseContentCardDTO) {
    if (purchaseContentCardDTO == null) {
      return null;
    }

    PurchaserContentPreviewCardResponse.PurchaserContentPreviewCardResponseBuilder
        purchaserContentPreviewCardResponse = PurchaserContentPreviewCardResponse.builder();

    if (purchaseContentCardDTO.getMerchantUid() != null) {
      purchaserContentPreviewCardResponse.merchantUid(purchaseContentCardDTO.getMerchantUid());
    }
    if (purchaseContentCardDTO.getContentId() != null) {
      purchaserContentPreviewCardResponse.contentId(purchaseContentCardDTO.getContentId());
    }
    if (purchaseContentCardDTO.getContentType() != null) {
      purchaserContentPreviewCardResponse.contentType(purchaseContentCardDTO.getContentType());
    }
    if (purchaseContentCardDTO.getPurchasedAt() != null) {
      purchaserContentPreviewCardResponse.purchasedAt(purchaseContentCardDTO.getPurchasedAt());
    }
    if (purchaseContentCardDTO.getTitle() != null) {
      purchaserContentPreviewCardResponse.title(purchaseContentCardDTO.getTitle());
    }
    if (purchaseContentCardDTO.getThumbnailUrl() != null) {
      purchaserContentPreviewCardResponse.thumbnailUrl(purchaseContentCardDTO.getThumbnailUrl());
    }
    if (purchaseContentCardDTO.getSellerName() != null) {
      purchaserContentPreviewCardResponse.sellerName(purchaseContentCardDTO.getSellerName());
    }
    if (purchaseContentCardDTO.getOriginalPrice() != null) {
      purchaserContentPreviewCardResponse.originalPrice(purchaseContentCardDTO.getOriginalPrice());
    }
    if (purchaseContentCardDTO.getFinalPrice() != null) {
      purchaserContentPreviewCardResponse.finalPrice(purchaseContentCardDTO.getFinalPrice());
    }
    purchaserContentPreviewCardResponse.priceOptionLength(
        purchaseContentCardDTO.getPriceOptionLength());
    if (purchaseContentCardDTO.getOrderStatus() != null) {
      purchaserContentPreviewCardResponse.orderStatus(purchaseContentCardDTO.getOrderStatus());
    }

    return purchaserContentPreviewCardResponse.build();
  }
}
