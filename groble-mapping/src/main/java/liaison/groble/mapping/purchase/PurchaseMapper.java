package liaison.groble.mapping.purchase;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.purchase.response.PurchasedContentDetailResponse;
import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.application.purchase.dto.PurchasedContentDetailDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PurchaseMapper {
  // ====== 📥 Request → DTO 변환 ======

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(target = "contactInfo", ignore = true)
  PurchasedContentDetailResponse toPurchasedContentDetailResponse(
      PurchasedContentDetailDTO purchasedContentDetailDTO);

  // ContactInfo를 포함한 상세 응답 생성
  default PurchasedContentDetailResponse toPurchasedContentDetailResponse(
      PurchasedContentDetailDTO purchasedContentDetailDTO,
      ContactInfoResponse contactInfoResponse) {
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
        .thumbnailUrl(response.getThumbnailUrl())
        .isRefundable(response.getIsRefundable())
        .cancelReason(response.getCancelReason())
        .contactInfo(contactInfoResponse)
        .build();
  }

  PurchaserContentPreviewCardResponse toPurchaserContentPreviewCardResponse(
      PurchaseContentCardDTO purchaseContentCardDTO);

  default PageResponse<PurchaserContentPreviewCardResponse>
      toPurchaserContentPreviewCardResponsePage(
          PageResponse<PurchaseContentCardDTO> dtoPageResponse) {
    if (dtoPageResponse == null) {
      return null;
    }

    // items 리스트 변환
    List<PurchaserContentPreviewCardResponse> convertedItems =
        dtoPageResponse.getItems().stream()
            .map(this::toPurchaserContentPreviewCardResponse)
            .collect(Collectors.toList());

    // PageInfo 복사
    PageResponse.PageInfo pageInfo =
        PageResponse.PageInfo.builder()
            .currentPage(dtoPageResponse.getPageInfo().getCurrentPage())
            .totalPages(dtoPageResponse.getPageInfo().getTotalPages())
            .pageSize(dtoPageResponse.getPageInfo().getPageSize())
            .totalElements(dtoPageResponse.getPageInfo().getTotalElements())
            .first(dtoPageResponse.getPageInfo().isFirst())
            .last(dtoPageResponse.getPageInfo().isLast())
            .empty(dtoPageResponse.getPageInfo().isEmpty())
            .build();

    // MetaData 복사 (있는 경우)
    PageResponse.MetaData meta = null;
    if (dtoPageResponse.getMeta() != null) {
      meta =
          PageResponse.MetaData.builder()
              .searchTerm(dtoPageResponse.getMeta().getSearchTerm())
              .filter(dtoPageResponse.getMeta().getFilter())
              .sortBy(dtoPageResponse.getMeta().getSortBy())
              .sortDirection(dtoPageResponse.getMeta().getSortDirection())
              .categoryIds(dtoPageResponse.getMeta().getCategoryIds())
              .build();
    }

    // PageResponse 생성
    return PageResponse.<PurchaserContentPreviewCardResponse>builder()
        .items(convertedItems)
        .pageInfo(pageInfo)
        .meta(meta)
        .build();
  }
}
