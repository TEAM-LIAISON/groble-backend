package liaison.groble.mapping.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import liaison.groble.api.model.admin.response.AdminContentSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminContentSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface AdminContentMapper {
  /* 페이지 변환 – default 메서드는 수동 구현 + 재사용 */
  default PageResponse<AdminContentSummaryInfoResponse> toAdminContentSummaryInfoResponsePage(
      PageResponse<AdminContentSummaryInfoDTO> dtoPage) {

    List<AdminContentSummaryInfoResponse> items =
        dtoPage.getItems().stream()
            .map(this::toAdminContentSummaryInfoResponse) // 위 단건 매핑 재사용
            .collect(Collectors.toList());

    return PageResponse.<AdminContentSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }
  ;

  AdminContentSummaryInfoResponse toAdminContentSummaryInfoResponse(
      AdminContentSummaryInfoDTO adminContentSummaryInfoDTO);
}
