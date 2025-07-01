package liaison.groble.api.server.purchase.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PurchaseDtoMapper {

  public PurchaserContentPreviewCardResponse toPurchaseContentPreviewCardFromCardDto(
      PurchaseContentCardDTO cardDto) {
    return PurchaserContentPreviewCardResponse.builder()
        .merchantUid(cardDto.getMerchantUid())
        .contentId(cardDto.getContentId())
        .contentType(cardDto.getContentType())
        .purchasedAt(cardDto.getPurchasedAt())
        .title(cardDto.getTitle())
        .thumbnailUrl(cardDto.getThumbnailUrl())
        .sellerName(cardDto.getSellerName())
        .originalPrice(cardDto.getOriginalPrice())
        .finalPrice(cardDto.getFinalPrice())
        .priceOptionLength(cardDto.getPriceOptionLength())
        .orderStatus(cardDto.getOrderStatus())
        .status(cardDto.getStatus())
        .build();
  }
}
