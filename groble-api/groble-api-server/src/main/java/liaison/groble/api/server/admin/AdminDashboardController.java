package liaison.groble.api.server.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminDashboardMapper;

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

  public AdminDashboardController(
      ResponseHelper responseHelper, AdminDashboardMapper adminDashboardMapper) {
    super(responseHelper);
    this.adminDashboardMapper = adminDashboardMapper;
  }
}
