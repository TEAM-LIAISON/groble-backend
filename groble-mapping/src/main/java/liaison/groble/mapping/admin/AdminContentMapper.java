package liaison.groble.mapping.admin;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.common.PageResponseMapper;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminContentMapper extends PageResponseMapper {
  default PageResponse<AdminContentSummaryInfoResponse> toAdminContentSummaryInfoResponsePage(
      PageResponse<AdminContentSummaryInfoDTO> dtoPage) {

    return toPageResponse(dtoPage, this::toAdminContentSummaryInfoResponse);
  }

  AdminContentSummaryInfoResponse toAdminContentSummaryInfoResponse(
      AdminContentSummaryInfoDTO adminContentSummaryInfoDTO);
}
