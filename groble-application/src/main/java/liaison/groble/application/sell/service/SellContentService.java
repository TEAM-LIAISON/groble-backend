package liaison.groble.application.sell.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.domain.purchase.dto.FlatContentSellDetailDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellContentService {

  private final PurchaseReader purchaseReader;

  @Transactional
  public ContentSellDetailDTO getContentSellDetail(Long userId, Long contentId, Long purchaseId) {
    FlatContentSellDetailDTO contentSellDetailDTO =
        purchaseReader.getContentSellDetail(userId, contentId, purchaseId);

    return buildContentSellDetail(contentSellDetailDTO);
  }

  private ContentSellDetailDTO buildContentSellDetail(
      FlatContentSellDetailDTO contentSellDetailDTO) {
    return ContentSellDetailDTO.builder()
        .contentTitle(contentSellDetailDTO.getContentTitle())
        .purchasedAt(contentSellDetailDTO.getPurchasedAt())
        .purchaserNickname(contentSellDetailDTO.getPurchaserNickname())
        .purchaserEmail(contentSellDetailDTO.getPurchaserEmail())
        .purchaserPhoneNumber(contentSellDetailDTO.getPurchaserPhoneNumber())
        .selectedOptionName(contentSellDetailDTO.getSelectedOptionName())
        .finalPrice(contentSellDetailDTO.getFinalPrice())
        .build();
  }
}
