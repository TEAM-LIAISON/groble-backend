package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
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
import liaison.groble.api.server.admin.docs.AdminOrderSwaggerDocs;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminOrderService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
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

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(name = AdminOrderSwaggerDocs.TAG_NAME, description = AdminOrderSwaggerDocs.TAG_DESCRIPTION)
public class AdminOrderController extends BaseController {

  private final AdminOrderService adminOrderService;
  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderController(
      ResponseHelper responseHelper,
      AdminOrderService adminOrderService,
      AdminOrderMapper adminOrderMapper) {
    super(responseHelper);
    this.adminOrderService = adminOrderService;
    this.adminOrderMapper = adminOrderMapper;
  }

  @Operation(
      summary = AdminOrderSwaggerDocs.GET_ALL_ORDERS_SUMMARY,
      description = AdminOrderSwaggerDocs.GET_ALL_ORDERS_DESCRIPTION)
  @AdminOrderSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.ORDERS)
  @Logging(item = "AdminOrder", action = "getAllOrders", includeParam = true, includeResult = true)
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
    PageResponse<AdminOrderSummaryInfoDTO> infoDTOPage = adminOrderService.getAllOrders(pageable);
    PageResponse<AdminOrderSummaryInfoResponse> responsePage =
        adminOrderMapper.toAdminOrderSummaryInfoResponsePage(infoDTOPage);

    return success(responsePage, ResponseMessages.Admin.ORDER_SUMMARY_INFO_RETRIEVED);
  }

  @Operation(
      summary = AdminOrderSwaggerDocs.GET_CANCELLATION_REASON_SUMMARY,
      description = AdminOrderSwaggerDocs.GET_CANCELLATION_REASON_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.ORDER_CANCELLATION_REASON)
  @Logging(
      item = "AdminOrder",
      action = "getOrderCancellationReason",
      includeParam = true,
      includeResult = true)
  public ResponseEntity<GrobleResponse<AdminOrderCancellationReasonResponse>>
      getOrderCancellationReason(
          @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {
    AdminOrderCancellationReasonDTO reasonDTO =
        adminOrderService.getOrderCancellationReason(merchantUid);
    AdminOrderCancellationReasonResponse reasonResponse =
        adminOrderMapper.toAdminOrderCancellationReasonResponse(reasonDTO);
    return success(reasonResponse, ResponseMessages.Admin.ORDER_CANCELLATION_REASON_RETRIEVED);
  }

  @Operation(
      summary = AdminOrderSwaggerDocs.HANDLE_CANCEL_REQUEST_SUMMARY,
      description = AdminOrderSwaggerDocs.HANDLE_CANCEL_REQUEST_DESCRIPTION)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.ORDER_CANCEL_REQUEST)
  @Logging(
      item = "AdminOrder",
      action = "cancelRequestOrder",
      includeParam = true,
      includeResult = true)
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
    return success(response, ResponseMessages.Admin.ORDER_CANCEL_REQUEST_PROCESSED);
  }
}
