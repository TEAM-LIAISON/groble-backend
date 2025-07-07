package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminOrderCancelRequestResponse;
import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminOrderSummaryInfo;
import liaison.groble.api.model.admin.validation.ValidOrderCancelAction;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDto;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminOrderService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminOrderMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(
    name = "[✅ 관리자] 모든 주문을 조회하고 취소 사유 조회 기능 API",
    description = "모든 주문 목록을 조회하고, 취소 요청 및 환불 완료 주문에 대한 사유를 조회하는 API입니다.")
public class AdminOrderController {

  // API 경로 상수화
  private static final String CANCEL_REQUEST_ORDER_PATH = "/order/{merchantUid}/cancel-request";

  // 응답 메시지 상수화
  private static final String CANCEL_REQUEST_ORDER_RESPONSE_MESSAGE =
      "결제 취소 요청 주문에 대한 승인 및 거절 처리에 성공했습니다.";

  // Service
  private final AdminOrderService adminOrderService;

  // Mapper
  private final AdminOrderMapper adminOrderMapper;

  // Helper
  private final ResponseHelper responseHelper;

  // 1. 주문 목록 전체 조회 (결제 완료 이후에 대한 주문만 모두 조회 가능)
  @AdminOrderSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping("/orders")
  public ResponseEntity<GrobleResponse<PageResponse<AdminOrderSummaryInfoResponse>>> getAllOrders(
      @Auth Accessor accessor,
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "페이지당 주문 수", example = "12")
          @RequestParam(value = "size", defaultValue = "12")
          int size,
      @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
          @RequestParam(value = "sort", defaultValue = "createdAt")
          String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<AdminOrderSummaryInfoDTO> infoDtoPage = adminOrderService.getAllOrders(pageable);
    PageResponse<AdminOrderSummaryInfoResponse> responsePage =
        adminOrderMapper.toAdminOrderSummaryInfoResponsePage(infoDtoPage);

    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  // 2. 결제 취소 주문에 대한 사유 조회
  @Operation(summary = "결제 취소 주문에 대한 사유 조회", description = "결제 취소 주문에 대한 사유를 조회합니다.")
  @RequireRole("ROLE_ADMIN")
  @GetMapping("/order/{merchantUid}/cancellation-reason")
  public ResponseEntity<GrobleResponse<AdminOrderCancellationReasonResponse>>
      getOrderCancellationReason(
          @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
    AdminOrderCancellationReasonDto reasonDto =
        adminOrderService.getOrderCancellationReason(merchantUid);
    AdminOrderCancellationReasonResponse reasonResponse =
        AdminOrderCancellationReasonResponse.builder()
            .cancelReason(reasonDto.getCancelReason())
            .build();

    return ResponseEntity.ok(GrobleResponse.success(reasonResponse));
  }

  // 3. 결제 취소 요청 주문 (승인 및 거절)
  @Operation(
      summary = "[✅ 관리자 주문 관리] 결제 취소 요청 주문 승인 및 거절",
      description = "결제 취소 요청 주문을 승인하거나 거절합니다.")
  @RequireRole("ROLE_ADMIN")
  @PostMapping(CANCEL_REQUEST_ORDER_PATH)
  public ResponseEntity<GrobleResponse<AdminOrderCancelRequestResponse>> cancelRequestOrder(
      @Auth Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid,
      @ValidOrderCancelAction
          @Parameter(
              description = "취소 요청 처리 액션",
              required = true,
              schema = @Schema(allowableValues = {"approve", "reject"}))
          @RequestParam(value = "action")
          String action) {

    AdminOrderCancelRequestDTO adminOrderCancelRequestDTO =
        adminOrderService.handleCancelRequestOrder(merchantUid, action);
    AdminOrderCancelRequestResponse response =
        adminOrderMapper.toAdminOrderCancelRequestResponse(adminOrderCancelRequestDTO);
    return responseHelper.success(response, CANCEL_REQUEST_ORDER_RESPONSE_MESSAGE, HttpStatus.OK);
  }
}
