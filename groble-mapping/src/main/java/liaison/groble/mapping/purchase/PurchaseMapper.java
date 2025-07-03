package liaison.groble.mapping.purchase;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import liaison.groble.api.model.purchase.response.PurchaserContentPreviewCardResponse;
import liaison.groble.application.purchase.dto.PurchaseContentCardDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PurchaseMapper {
  // ====== 📥 Request → DTO 변환 ======

  // ====== 📤 DTO → Response 변환 ======
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
