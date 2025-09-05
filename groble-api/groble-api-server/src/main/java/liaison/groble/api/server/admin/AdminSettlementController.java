package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.settlement.request.SettlementApprovalRequest;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementDetailResponse;
import liaison.groble.api.model.admin.settlement.response.AdminSettlementsOverviewResponse;
import liaison.groble.api.model.admin.settlement.response.SettlementApprovalResponse;
import liaison.groble.api.server.admin.docs.AdminSettlementExampleResponses;
import liaison.groble.api.server.admin.docs.AdminSettlementSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.settlement.dto.AdminSettlementDetailDTO;
import liaison.groble.application.admin.settlement.dto.AdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.PaypleSettlementResultDTO;
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
      @Auth Accessor accessor, @RequestParam Long settlementId) {
    AdminSettlementDetailDTO adminSettlementDetailDTO =
        adminSettlementService.getSettlementDetail(settlementId);

    AdminSettlementDetailResponse response =
        adminSettlementMapper.toAdminSettlementDetailResponse(adminSettlementDetailDTO);

    return success(response, ResponseMessages.Admin.SETTLEMENT_DETAIL_RETRIEVED);
  }

  /**
   * 정산 승인 처리
   *
   * <p>관리자가 선택된 정산 항목들을 일괄 승인하고, 페이플 정산을 실행합니다.
   *
   * @param request 정산 승인 요청
   * @return 정산 승인 결과
   */
  @PostMapping(ApiPaths.Admin.APPROVE_SETTLEMENTS)
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

  /**
   * 이체 대기 상태인 정산을 실제 실행
   *
   * <p>관리자가 검토 후 이체 대기 상태의 정산을 실제 실행합니다.
   *
   * @param groupKey 이체 대기 시 받은 그룹키
   * @param billingTranId 실행할 빌링키 (기본값: "ALL")
   * @return 이체 실행 결과
   */
  @PostMapping("/execute-transfer")
  public ResponseEntity<GrobleResponse<PaypleSettlementResultDTO>> executeTransfer(
      @RequestParam String groupKey, @RequestParam(defaultValue = "ALL") String billingTranId) {

    log.info("이체 실행 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    PaypleSettlementResultDTO result =
        adminSettlementService.executeTransfer(groupKey, billingTranId);

    if (result.isSuccess()) {
      return success(result, "이체 실행이 완료되었습니다");
    } else {
      return success(result, result.getResponseMessage());
    }
  }

  /**
   * 이체 대기 상태인 정산을 취소
   *
   * <p>관리자가 검토 후 이체 대기 상태의 정산을 취소합니다.
   *
   * @param groupKey 이체 대기 시 받은 그룹키
   * @param billingTranId 취소할 빌링키 (기본값: "ALL")
   * @param cancelReason 취소 사유
   * @return 이체 취소 결과
   */
  @PostMapping("/cancel-transfer")
  public ResponseEntity<GrobleResponse<PaypleSettlementResultDTO>> cancelTransfer(
      @RequestParam String groupKey,
      @RequestParam(defaultValue = "ALL") String billingTranId,
      @RequestParam(required = false) String cancelReason) {

    log.info("이체 취소 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    PaypleSettlementResultDTO result =
        adminSettlementService.cancelTransfer(groupKey, billingTranId, cancelReason);

    if (result.isSuccess()) {
      return success(result, "이체 취소가 완료되었습니다");
    } else {
      return success(result, result.getResponseMessage());
    }
  }

  /** 민감한 데이터 마스킹 */
  private String maskSensitiveData(String sensitiveData) {
    if (sensitiveData == null || sensitiveData.length() <= 8) {
      return "****";
    }
    return sensitiveData.substring(0, 4)
        + "****"
        + sensitiveData.substring(sensitiveData.length() - 4);
  }
}
