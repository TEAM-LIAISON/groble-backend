package liaison.groble.api.server.purchase.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PurchaseDtoMapper {

  public PurchaserContentPreviewCardResponse toPurchaseContentPreviewCardFromCardDto(
      PurchaseContentCardDto cardDto) {
    return PurchaserContentPreviewCardResponse.builder()
        .contentId(cardDto.getContentId())
        .purchasedAt(cardDto.getPurchasedAt())
        .title(cardDto.getTitle())
        .thumbnailUrl(cardDto.getThumbnailUrl())
        .sellerName(cardDto.getSellerName())
        .originalPrice(cardDto.getOriginalPrice())
        .priceOptionLength(cardDto.getPriceOptionLength())
        .status(cardDto.getStatus())
        .build();
  }
}
