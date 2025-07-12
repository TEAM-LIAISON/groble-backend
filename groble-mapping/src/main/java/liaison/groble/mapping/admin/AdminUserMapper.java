package liaison.groble.mapping.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
  @Mapping(target = "isSellerTermsAgreed", source = "sellerTermsAgreed")
  @Mapping(target = "isMarketingAgreed", source = "marketingAgreed")
  @Mapping(target = "isSellerInfo", source = "sellerInfo")
  @Mapping(target = "isBusinessSeller", source = "businessSeller")
  AdminUserSummaryInfoResponse toAdminUserSummaryInfoResponse(AdminUserSummaryInfoDTO dto);

  /* 페이지 변환 – default 메서드는 수동 구현 + 재사용 */
  default PageResponse<AdminUserSummaryInfoResponse> toAdminUserSummaryInfoResponsePage(
      PageResponse<AdminUserSummaryInfoDTO> dtoPage) {

    List<AdminUserSummaryInfoResponse> items =
        dtoPage.getItems().stream()
            .map(this::toAdminUserSummaryInfoResponse) // 위 단건 매핑 재사용
            .collect(Collectors.toList());

    return PageResponse.<AdminUserSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }
}
