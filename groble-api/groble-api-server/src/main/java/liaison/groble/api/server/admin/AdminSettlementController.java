package liaison.groble.api.server.admin;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminSettlementMapper;

import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Admin.SETTLEMENT_BASE)
@Tag(
    name = AdminSettlementSwaggerDocs.TAG_NAME,
    description = AdminSettlementSwaggerDocs.TAG_DESCRIPTION)
public class AdminSettlementController extends BaseController {
  // Factory

  // Mapper
  private final AdminSettlementMapper adminSettlementMapper;

  public AdminSettlementController(
      ResponseHelper responseHelper, AdminSettlementMapper adminSettlementMapper) {
    super(responseHelper);
    this.adminSettlementMapper = adminSettlementMapper;
  }

  // TODO: 특정 사용자 정산 완료 처리 진행
}
