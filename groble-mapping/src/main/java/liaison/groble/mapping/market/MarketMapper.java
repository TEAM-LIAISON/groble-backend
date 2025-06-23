package liaison.groble.mapping.market;

import java.util.List;

import org.mapstruct.Mapper;

import liaison.groble.api.model.content.response.ContentPreviewCardResponse;
import liaison.groble.api.model.maker.request.ContactInfoRequest;
import liaison.groble.api.model.maker.request.MarketEditRequest;
import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketEditDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface MarketMapper {
  // ====== 📥 Request → DTO 변환 ======
  MarketEditDTO toMarketEditDTO(MarketEditRequest marketEditRequest);

  ContactInfoDTO toContactInfoDTO(ContactInfoRequest contactInfoRequest);

  // ====== 📤 DTO → Response 변환 ======
  MakerIntroSectionResponse toMakerIntroSectionResponse(
      MarketIntroSectionDTO marketIntroSectionDTO);

  ContactInfoResponse toContactInfoResponse(ContactInfoDTO contactInfoDTO);

  /** ContentCardDTO를 ContentPreviewCardResponse로 변환 */
  ContentPreviewCardResponse toContentPreviewCardResponse(ContentCardDTO contentCardDTO);

  List<ContentPreviewCardResponse> toContentPreviewCardResponseList(
      List<ContentCardDTO> contentCardDTOList);

  // 필드명이 동일하다면 자동 매핑
  PageResponse<ContentPreviewCardResponse> toContentPreviewCardResponsePage(
      PageResponse<ContentCardDTO> dtoPageResponse);
}
