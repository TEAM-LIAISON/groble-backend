package liaison.groble.mapping.sell;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.sell.request.ReplyContentRequest;
import liaison.groble.api.model.sell.response.ContentReviewDetailResponse;
import liaison.groble.api.model.sell.response.ContentSellDetailResponse;
import liaison.groble.api.model.sell.response.ReplyContentResponse;
import liaison.groble.api.model.sell.response.SellManageDetailResponse;
import liaison.groble.api.model.sell.response.SellManagePageResponse;
import liaison.groble.application.sell.dto.ContentReviewDetailDTO;
import liaison.groble.application.sell.dto.ContentSellDetailDTO;
import liaison.groble.application.sell.dto.ReplyContentDTO;
import liaison.groble.application.sell.dto.SellManageDetailDTO;
import liaison.groble.application.sell.dto.SellManagePageDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;
import liaison.groble.mapping.content.ContentReplyMapper;

@Mapper(
    config = GrobleMapperConfig.class,
    uses = {ContentReplyMapper.class})
public interface SellMapper extends PageResponseMapper {
  // ====== 📤 Request → DTO 변환 ======
  ReplyContentDTO toReplyContentDTO(ReplyContentRequest replyContentRequest);

  // ====== 📤 DTO → Response 변환 ======
  @Mapping(source = "title", target = "title")
  @Mapping(source = "sellManageDetail", target = "contentSellDetail")
  @Mapping(source = "contentSellDetailList", target = "contentSellList")
  @Mapping(source = "contentReviewDetailList", target = "contentReviewList")
  SellManagePageResponse toSellManagePageResponse(SellManagePageDTO sellManagePageDTO);

  // SellManageDetailDTO → SellManageDetailResponse 변환 메서드 추가
  SellManageDetailResponse toSellManageDetailResponse(SellManageDetailDTO sellManageDetailDTO);

  ContentSellDetailResponse toContentSellDetailResponse(ContentSellDetailDTO contentSellDetailDTO);

  ContentReviewDetailResponse toContentReviewDetailResponse(
      ContentReviewDetailDTO contentReviewDetailDTO);

  ReplyContentResponse toReplyContentResponse(ReplyContentDTO replyContentDTO);

  default PageResponse<ContentSellDetailResponse> toContentSellResponsePage(
      PageResponse<ContentSellDetailDTO> dtoPageResponse) {
    return toPageResponse(dtoPageResponse, this::toContentSellDetailResponse);
  }

  default PageResponse<ContentReviewDetailResponse> toContentReviewResponsePage(
      PageResponse<ContentReviewDetailDTO> dtoPageResponse) {
    return toPageResponse(dtoPageResponse, this::toContentReviewDetailResponse);
  }
}
