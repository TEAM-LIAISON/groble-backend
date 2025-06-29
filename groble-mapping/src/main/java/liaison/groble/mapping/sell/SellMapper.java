package liaison.groble.mapping.sell;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface SellMapper {
  // ====== 📤 Request → DTO 변환 ======
  ReplyContentDTO toReplyContentDTO(ReplyContentRequest replyContentRequest);

  // ====== 📤 DTO → Response 변환 ======
  ContentSellDetailResponse toContentSellDetailResponse(ContentSellDetailDTO contentSellDetailDTO);

  ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO);

  ReplyContentResponse toReplyContentResponse(ReplyContentDTO replyContentDTO);

  default PageResponse<ContentReviewDetailResponse> toContentReviewResponsePage(
      PageResponse<ContentReviewDetailDTO> dtoPageResponse) {
    if (dtoPageResponse == null) {
      return null;
    }

    // items 리스트 변환
    List<ContentReviewDetailResponse> convertedItems =
        dtoPageResponse.getItems().stream()
            .map(this::toContentReviewDetailResponse)
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
    return PageResponse.<ContentReviewDetailResponse>builder()
        .items(convertedItems)
        .pageInfo(pageInfo)
        .meta(meta)
        .build();
  }
}
