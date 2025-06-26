package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDto;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDto;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;
import liaison.groble.domain.order.entity.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
  private final OrderReader orderReader;

  // 모든 주문 목록 전체 조회 메서드
  public PageResponse<AdminOrderSummaryInfoDto> getAllOrders(Pageable pageable) {
    Page<FlatAdminOrderSummaryInfoDTO> orderPage = orderReader.getAllOrders(pageable);

    List<AdminOrderSummaryInfoDto> items =
        orderPage.getContent().stream().map(this::convertFlatDtoToInfoResponse).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(orderPage, items, meta);
  }

  // 주문 최소 사유 조회 메서드
  public AdminOrderCancellationReasonDto getOrderCancellationReason(String merchantUid) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Order.OrderStatus status = order.getStatus();
    if (status != Order.OrderStatus.CANCELLED && status != Order.OrderStatus.CANCEL_REQUEST) {
      throw new IllegalArgumentException("취소 또는 취소 요청 상태가 아닙니다.");
    }

    return AdminOrderCancellationReasonDto.builder().cancelReason(order.getOrderNote()).build();
  }

  private AdminOrderSummaryInfoDto convertFlatDtoToInfoResponse(FlatAdminOrderSummaryInfoDTO flat) {
    return AdminOrderSummaryInfoDto.builder()
        .contentId(flat.getContentId())
        .merchantUid(flat.getMerchantUid())
        .createdAt(flat.getCreatedAt())
        .contentType(flat.getContentType())
        .contentStatus(flat.getContentStatus())
        .purchaserName(flat.getPurchaserName())
        .contentTitle(flat.getContentTitle())
        .finalPrice(flat.getFinalPrice())
        .orderStatus(flat.getOrderStatus())
        .build();
  }
}
