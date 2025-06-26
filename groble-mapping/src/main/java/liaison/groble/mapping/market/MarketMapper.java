package liaison.groble.mapping.market;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.request.ContactInfoRequest;
import liaison.groble.api.model.maker.request.MarketEditRequest;
import liaison.groble.api.model.maker.request.MarketLinkCheckRequest;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketEditDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.application.market.dto.MarketLinkCheckDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface MarketMapper {
  // ====== 📥 Request → DTO 변환 ======
  MarketEditDTO toMarketEditDTO(MarketEditRequest marketEditRequest);

  ContactInfoDTO toContactInfoDTO(ContactInfoRequest contactInfoRequest);

  MarketLinkCheckDTO toMarketLinkCheckDTO(MarketLinkCheckRequest marketLinkCheckRequest);

  // ====== 📤 DTO → Response 변환 ======
  MakerIntroSectionResponse toMakerIntroSectionResponse(
      MarketIntroSectionDTO marketIntroSectionDTO);

  ContactInfoResponse toContactInfoResponse(ContactInfoDTO contactInfoDTO);

  /** ContentCardDTO를 ContentPreviewCardResponse로 변환 */
  ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDTO);

  List<ContentPreviewCardResponse> toContentPreviewCardResponseList(
      List<ContentCardDTO> contentCardDTOList);

  // MarketMapper 인터페이스에 추가할 default 메서드
  default PageResponse<ContentPreviewCardResponse> toContentPreviewCardResponsePage(
      PageResponse<ContentCardDTO> dtoPageResponse) {
    if (dtoPageResponse == null) {
      return null;
    }

    // items 리스트 변환
    List<ContentPreviewCardResponse> convertedItems =
        dtoPageResponse.getItems().stream()
            .map(this::toContentPreviewCardResponse)
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
    return PageResponse.<ContentPreviewCardResponse>builder()
        .items(convertedItems)
        .pageInfo(pageInfo)
        .meta(meta)
        .build();
  }
}
