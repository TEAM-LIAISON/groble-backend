package liaison.groble.api.server.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.dashboard.response.AdminDashboardOverviewResponse;
import liaison.groble.api.server.admin.docs.AdminDashboardApiResponses;
import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dashboard.dto.AdminDashboardOverviewDTO;
import liaison.groble.application.admin.dashboard.service.AdminDashboardService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminDashboardMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Admin.DASHBOARD_BASE)
@Tag(
    name = AdminSettlementSwaggerDocs.TAG_NAME,
    description = AdminSettlementSwaggerDocs.TAG_DESCRIPTION)
public class AdminDashboardController extends BaseController {

  // Service

  // Mapper
  private final AdminDashboardMapper adminDashboardMapper;
  private final AdminDashboardService adminDashboardService;

  public AdminDashboardController(
      ResponseHelper responseHelper,
      AdminDashboardMapper adminDashboardMapper,
      AdminDashboardService adminDashboardService) {
    super(responseHelper);
    this.adminDashboardMapper = adminDashboardMapper;
    this.adminDashboardService = adminDashboardService;
  }

  // 모든 전체 데이터 조회
  @Operation(
      summary = AdminSettlementSwaggerDocs.ALL_USERS_SETTLEMENTS_SUMMARY,
      description = AdminSettlementSwaggerDocs.ALL_USERS_SETTLEMENTS_DESCRIPTION)
  @AdminDashboardApiResponses.GetAdminDashboardOverviewApiResponses
  @Logging(
      item = "AdminDashboard",
      action = "getAdminDashboardOverview",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Admin.DASHBOARD_OVERVIEW)
  public ResponseEntity<GrobleResponse<AdminDashboardOverviewResponse>> getAdminDashboardOverview(
      @Auth Accessor accessor) {
    AdminDashboardOverviewDTO adminDashboardOverviewDTO =
        adminDashboardService.getAdminDashboardOverview();
    AdminDashboardOverviewResponse response =
        adminDashboardMapper.toAdminDashboardOverviewResponse(adminDashboardOverviewDTO);
    return success(response, ResponseMessages.Admin.DASHBOARD_OVERVIEW_RETRIEVED);
  }
}
