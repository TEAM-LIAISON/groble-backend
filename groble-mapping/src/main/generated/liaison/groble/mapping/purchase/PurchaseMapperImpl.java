package liaison.groble.mapping.purchase;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-01T20:35:29+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class PurchaseMapperImpl implements PurchaseMapper {

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
    if (purchaseContentCardDTO.getStatus() != null) {
      purchaserContentPreviewCardResponse.status(purchaseContentCardDTO.getStatus());
    }

    return purchaserContentPreviewCardResponse.build();
  }
}
