package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementDetailResponse;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementsOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.PerTransactionAdminSettlementOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.api.server.admin.docs.AdminSettlementExampleResponses;
import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.settlement.dto.AdminSettlementDetailDTO;
import liaison.groble.application.admin.settlement.dto.AdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PerTransactionAdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.application.admin.settlement.service.AdminSettlementService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminSettlementMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
   * 모든 사용자의 정산 내역 조회
   *
   * <p>관리자가 모든 사용자의 정산 내역을 페이징 처리하여 조회합니다.
   *
   * @param accessor 인증된 관리자 정보
   * @param page 페이지 번호 (기본값: 0)
   * @param size 페이지 크기 (기본값: 20)
   * @param sort 정렬 기준 (기본값: createdAt)
   * @return 페이징된 모든 사용자의 정산 내역
   */
  @Operation(
      summary = AdminSettlementSwaggerDocs.ALL_USERS_SETTLEMENTS_SUMMARY,
      description = AdminSettlementSwaggerDocs.ALL_USERS_SETTLEMENTS_DESCRIPTION)
  @AdminSettlementExampleResponses.AllUsersSettlementsPageSuccess
  @Logging(
      item = "AdminSettlement",
      action = "getAllUsersSettlements",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Admin.ALL_USERS_SETTLEMENTS)
  public ResponseEntity<GrobleResponse<PageResponse<AdminSettlementsOverviewResponse>>>
      getAllUsersSettlements(
          @Auth Accessor accessor,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<AdminSettlementOverviewDTO> dtoPage =
        adminSettlementService.getAllUsersSettlements(accessor.getUserId(), pageable);

    PageResponse<AdminSettlementsOverviewResponse> responsePage =
        adminSettlementMapper.toAdminSettlementsOverviewResponsePage(dtoPage);

    return success(responsePage, ResponseMessages.Admin.ALL_USERS_SETTLEMENTS_RETRIEVED);
  }

  @Operation(
      summary = AdminSettlementSwaggerDocs.SETTLEMENT_DETAIL_SUMMARY,
      description = AdminSettlementSwaggerDocs.SETTLEMENT_DETAIL_DESCRIPTION)
  @AdminSettlementExampleResponses.AdminSettlementDetailSuccess
  @RequireRole("ROLE_ADMIN")
  @Logging(
      item = "AdminSettlement",
      action = "getSettlementsDetail",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Admin.SETTLEMENT_DETAIL)
  public ResponseEntity<GrobleResponse<AdminSettlementDetailResponse>> getSettlementsDetail(
      @Auth Accessor accessor,
      @Parameter(
              name = "settlementId",
              description = "숫자 형식",
              example = "265",
              schema = @Schema(type = "number"))
          @PathVariable("settlementId")
          Long settlementId) {
    AdminSettlementDetailDTO adminSettlementDetailDTO =
        adminSettlementService.getSettlementDetail(settlementId);

    AdminSettlementDetailResponse response =
        adminSettlementMapper.toAdminSettlementDetailResponse(adminSettlementDetailDTO);

    return success(response, ResponseMessages.Admin.SETTLEMENT_DETAIL_RETRIEVED);
  }

  @Operation(
      summary = AdminSettlementSwaggerDocs.SETTLEMENT_DETAIL_SALES_SUMMARY,
      description = AdminSettlementSwaggerDocs.SETTLEMENT_DETAIL_SALES_DESCRIPTION)
  @AdminSettlementExampleResponses.AdminSettlementSalesListSuccess
  @RequireRole("ROLE_ADMIN")
  @Logging(
      item = "AdminSettlement",
      action = "getSalesList",
      includeParam = true,
      includeResult = true)
  @GetMapping(ApiPaths.Admin.SALES_LIST)
  public ResponseEntity<GrobleResponse<PageResponse<PerTransactionAdminSettlementOverviewResponse>>>
      getSalesList(
          @Auth Accessor accessor,
          @Parameter(
                  name = "settlementId",
                  description = "숫자 형식",
                  example = "265",
                  schema = @Schema(type = "number"))
              @PathVariable("settlementId")
              Long settlementId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<PerTransactionAdminSettlementOverviewDTO> dtoPage =
        adminSettlementService.getSalesList(settlementId, pageable);

    PageResponse<PerTransactionAdminSettlementOverviewResponse> responsePage =
        adminSettlementMapper.toPerTransactionAdminSettlementOverviewResponsePage(dtoPage);

    return success(responsePage, ResponseMessages.Admin.SALES_LIST_RETRIEVED);
  }

  /**
   * 정산 승인 및 실행 처리
   *
   * <p>관리자가 선택된 정산 항목들을 일괄 승인하고, 페이플 정산을 바로 실행합니다.
   *
   * @param request 정산 승인 요청
   * @return 정산 승인 및 실행 결과
   */
  @PostMapping(ApiPaths.Admin.APPROVE_SETTLEMENTS)
  @AdminSettlementSwaggerDocs.ApproveSettlements
  public ResponseEntity<GrobleResponse<SettlementApprovalResponse>> approveSettlements(
      @Valid @RequestBody SettlementApprovalRequest request) {

    log.info("정산 승인 및 실행 요청 - 정산 수: {}", request.getSettlementIds().size());

    // 1. API 요청을 Application DTO로 변환
    SettlementApprovalRequestDTO requestDTO = adminSettlementMapper.toRequestDTO(request);

    // 2. Service 호출 - 승인과 실행을 한번에 처리
    SettlementApprovalDTO serviceResult =
        adminSettlementService.approveAndExecuteSettlements(requestDTO);

    // 3. Application DTO를 API 응답으로 변환
    SettlementApprovalResponse response = adminSettlementMapper.toResponse(serviceResult);

    return success(response, ResponseMessages.Admin.SETTLEMENT_APPROVAL_SUCCESS);
  }
}
