package liaison.groble.api.server.admin;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminOrderCancellationReasonResponse;
import liaison.groble.api.model.admin.response.AdminOrderSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminOrderSummaryInfo;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDto;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDto;
import liaison.groble.application.admin.service.AdminOrderService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자의 주문 기능 관련 API", description = "관리자 주문 관리 기능 API")
public class AdminOrderController {
  private final AdminOrderService adminOrderService;

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
    Pageable pageable = createPageable(page, size, sort);
    PageResponse<AdminOrderSummaryInfoDto> infoDtoPage = adminOrderService.getAllOrders(pageable);
    PageResponse<AdminOrderSummaryInfoResponse> responsePage =
        toAdminOrderSummaryInfoResponsePage(infoDtoPage);

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

  private Pageable createPageable(int page, int size, String sort) {
    // sort 파라미터가 없거나 빈 문자열인 경우 기본값 설정
    if (sort == null || sort.isBlank()) {
      sort = "createdAt";
    }

    // "property,direction" 형태로 분리
    String[] parts = sort.split(",");
    String property = parts[0].trim();
    Sort.Direction direction = Sort.Direction.DESC; // 기본 방향

    // direction 지정이 있으면 파싱 시도
    if (parts.length > 1 && !parts[1].isBlank()) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        log.warn("잘못된 정렬 방향: {}. DESC로 설정합니다.", parts[1].trim());
      }
    }

    return PageRequest.of(page, size, Sort.by(direction, property));
  }

  private PageResponse<AdminOrderSummaryInfoResponse> toAdminOrderSummaryInfoResponsePage(
      PageResponse<AdminOrderSummaryInfoDto> dtoPage) {
    List<AdminOrderSummaryInfoResponse> items =
        dtoPage.getItems().stream()
            .map(this::toAdminOrderSummaryInfoResponseFromDto)
            .collect(Collectors.toList());

    return PageResponse.<AdminOrderSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }

  private AdminOrderSummaryInfoResponse toAdminOrderSummaryInfoResponseFromDto(
      AdminOrderSummaryInfoDto infoDto) {
    return AdminOrderSummaryInfoResponse.builder()
        .contentId(infoDto.getContentId())
        .merchantUid(infoDto.getMerchantUid())
        .createdAt(infoDto.getCreatedAt())
        .contentType(infoDto.getContentType())
        .contentStatus(infoDto.getContentStatus())
        .purchaserName(infoDto.getPurchaserName())
        .contentTitle(infoDto.getContentTitle())
        .finalPrice(infoDto.getFinalPrice())
        .orderStatus(infoDto.getOrderStatus())
        .build();
  }
}
