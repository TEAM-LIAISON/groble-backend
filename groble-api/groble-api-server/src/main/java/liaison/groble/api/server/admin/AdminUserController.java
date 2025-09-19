package liaison.groble.api.server.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminUserSummaryInfo;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.common.swagger.SwaggerTags;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminUserService;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminUserMapper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(name = SwaggerTags.Admin.USER, description = SwaggerTags.Admin.USER_DESC)
public class AdminUserController extends BaseController {

  private final AdminUserService adminUserService;
  private final AdminUserMapper adminUserMapper;

  public AdminUserController(
      ResponseHelper responseHelper,
      AdminUserService adminUserService,
      AdminUserMapper adminUserMapper) {
    super(responseHelper);
    this.adminUserService = adminUserService;
    this.adminUserMapper = adminUserMapper;
  }

  @AdminUserSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.ADMIN_USER_SUMMARY_INFO)
  public ResponseEntity<GrobleResponse<PageResponse<AdminUserSummaryInfoResponse>>> getAllUsers(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "페이지당 사용자 수", example = "12")
          @RequestParam(value = "size", defaultValue = "12")
          int size,
      @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
          @RequestParam(value = "sort", defaultValue = "createdAt")
          String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<AdminUserSummaryInfoDTO> response = adminUserService.getAllUsers(pageable);
    PageResponse<AdminUserSummaryInfoResponse> responsePage =
        adminUserMapper.toAdminUserSummaryInfoResponsePage(response);

    return success(responsePage, ResponseMessages.Admin.USER_SUMMARY_INFO_RETRIEVED);
  }
}
