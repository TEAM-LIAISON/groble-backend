package liaison.groble.mapping.sell;

import javax.annotation.processing.Generated;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-28T01:45:47+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.10 (Amazon.com Inc.)")
@Component
public class SellMapperImpl implements SellMapper {

  @Override
  public ContentSellDetailResponse toContentSellDetailResponse(
      ContentSellDetailDTO contentSellDetailDTO) {
    if (contentSellDetailDTO == null) {
      return null;
    }

    ContentSellDetailResponse.ContentSellDetailResponseBuilder contentSellDetailResponse =
        ContentSellDetailResponse.builder();

    if (contentSellDetailDTO.getContentTitle() != null) {
      contentSellDetailResponse.contentTitle(contentSellDetailDTO.getContentTitle());
    }
    if (contentSellDetailDTO.getPurchasedAt() != null) {
      contentSellDetailResponse.purchasedAt(contentSellDetailDTO.getPurchasedAt());
    }
    if (contentSellDetailDTO.getPurchaserNickname() != null) {
      contentSellDetailResponse.purchaserNickname(contentSellDetailDTO.getPurchaserNickname());
    }
    if (contentSellDetailDTO.getPurchaserEmail() != null) {
      contentSellDetailResponse.purchaserEmail(contentSellDetailDTO.getPurchaserEmail());
    }
    if (contentSellDetailDTO.getPurchaserPhoneNumber() != null) {
      contentSellDetailResponse.purchaserPhoneNumber(
          contentSellDetailDTO.getPurchaserPhoneNumber());
    }
    if (contentSellDetailDTO.getSelectedOptionName() != null) {
      contentSellDetailResponse.selectedOptionName(contentSellDetailDTO.getSelectedOptionName());
    }
    if (contentSellDetailDTO.getFinalPrice() != null) {
      contentSellDetailResponse.finalPrice(contentSellDetailDTO.getFinalPrice());
    }

    return contentSellDetailResponse.build();
  }
}
