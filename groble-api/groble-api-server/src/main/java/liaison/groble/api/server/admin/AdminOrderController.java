package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDto;
import liaison.groble.application.admin.service.AdminOrderService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/order")
@Tag(name = "관리자의 주문 기능 관련 API", description = "관리자 주문 관리 기능 API")
public class AdminOrderController {
  private final AdminOrderService adminOrderService;

  // 1. 주문 목록 전체 조회 (결제 완료 이후에 대한 주문만 모두 조회 가능)

  // 2. 결제 취소 주문에 대한 사유 조회
  @Operation(summary = "결제 취소 주문에 대한 사유 조회", description = "결제 취소 주문에 대한 사유를 조회합니다.")
  @RequireRole("ROLE_ADMIN")
  @GetMapping("/{merchantUid}/cancellation-reason")
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
}
