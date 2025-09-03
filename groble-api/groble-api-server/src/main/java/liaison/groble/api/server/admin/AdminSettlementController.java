package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.application.admin.settlement.service.AdminSettlementService;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.admin.AdminSettlementMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiPaths.Admin.SETTLEMENT_BASE)
@Tag(
    name = AdminSettlementSwaggerDocs.TAG_NAME,
    description = AdminSettlementSwaggerDocs.TAG_DESCRIPTION)
public class AdminSettlementController extends BaseController {

  private final AdminSettlementMapper adminSettlementMapper;
  private final AdminSettlementService adminSettlementService;

  public AdminSettlementController(
      ResponseHelper responseHelper,
      AdminSettlementMapper adminSettlementMapper,
      AdminSettlementService adminSettlementService) {
    super(responseHelper);
    this.adminSettlementMapper = adminSettlementMapper;
    this.adminSettlementService = adminSettlementService;
  }

  /**
   * 정산 승인 처리
   *
   * <p>관리자가 선택된 정산 항목들을 일괄 승인하고, 페이플 정산을 실행합니다.
   *
   * @param request 정산 승인 요청
   * @return 정산 승인 결과
   */
  @PostMapping("/approve")
  @AdminSettlementSwaggerDocs.ApproveSettlements
  public ResponseEntity<GrobleResponse<SettlementApprovalResponse>> approveSettlements(
      @Valid @RequestBody SettlementApprovalRequest request) {

    log.info(
        "정산 승인 요청 - 정산 수: {}, 관리자: {}",
        request.getSettlementIds().size(),
        request.getAdminUserId());

    // 1. API 요청을 Application DTO로 변환
    SettlementApprovalRequestDTO requestDTO = adminSettlementMapper.toRequestDTO(request);

    // 2. Service 호출
    SettlementApprovalDTO serviceResult = adminSettlementService.approveSettlements(requestDTO);

    // 3. Application DTO를 API 응답으로 변환
    SettlementApprovalResponse response = adminSettlementMapper.toResponse(serviceResult);

    return success(response, ResponseMessages.Admin.SETTLEMENT_APPROVAL_SUCCESS);
  }
}
