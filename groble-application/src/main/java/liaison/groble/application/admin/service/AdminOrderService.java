package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminOrderCancelRequestDTO;
import liaison.groble.application.admin.dto.AdminOrderCancellationReasonDTO;
import liaison.groble.application.admin.dto.AdminOrderSummaryInfoDTO;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacadeV2;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.common.exception.InvalidRequestException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.order.dto.FlatAdminOrderSummaryInfoDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.purchase.entity.Purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService {
  private final OrderReader orderReader;
  private final PayplePaymentFacadeV2 payplePaymentFacadeV2;
  private final OrderRepository orderRepository;
  private final PurchaseReader purchaseReader;
  private final KakaoNotificationService kakaoNotificationService;

  // 모든 주문 목록 전체 조회 메서드
  public PageResponse<AdminOrderSummaryInfoDTO> getAllOrders(Pageable pageable) {
    Page<FlatAdminOrderSummaryInfoDTO> orderPage = orderReader.getAllOrders(pageable);

    List<AdminOrderSummaryInfoDTO> items =
        orderPage.getContent().stream().map(this::convertFlatDTOToInfoResponse).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(orderPage, items, meta);
  }

  // 주문 최소 사유 조회 메서드
  public AdminOrderCancellationReasonDTO getOrderCancellationReason(String merchantUid) {

    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    Order.OrderStatus status = order.getStatus();
    if (status != Order.OrderStatus.CANCELLED && status != Order.OrderStatus.CANCEL_REQUEST) {
      throw new IllegalArgumentException("취소 또는 취소 요청 상태가 아닙니다.");
    }

    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    return AdminOrderCancellationReasonDTO.builder()
        .cancelReason(purchase.getCancelReason().name())
        .build();
  }

  @Transactional
  public AdminOrderCancelRequestDTO handleCancelRequestOrder(String merchantUid, String action) {
    log.info("결제 취소 요청 처리 시작 - merchantUid: {}, action: {}", merchantUid, action);

    // 1. 주문 조회 및 상태 검증
    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    if (order.getStatus() != Order.OrderStatus.CANCEL_REQUEST) {
      throw new IllegalStateException("취소 요청 상태의 주문만 처리할 수 있습니다. 현재 상태: " + order.getStatus());
    }

    Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

    boolean memberOrder = order.isMemberOrder();
    boolean guestOrder = order.isGuestOrder();

    if (!memberOrder && !guestOrder) {
      throw new InvalidRequestException("회원/비회원 정보가 없는 주문입니다. merchantUid: " + merchantUid);
    }

    // 2. action에 따른 처리
    if ("approve".equalsIgnoreCase(action)) {
      // 취소 승인 - 페이플 실결제 취소 API 호출
      try {
        PaymentCancelResponse response =
            memberOrder
                ? payplePaymentFacadeV2.cancelPayment(
                    order.getUser().getId(), merchantUid, order.getOrderNote())
                : payplePaymentFacadeV2.cancelPaymentForGuest(
                    order.getGuestUser().getId(), merchantUid, order.getOrderNote());

        kakaoNotificationService.sendNotification(
            KakaoNotificationDTO.builder()
                .type(KakaoNotificationType.APPROVE_CANCEL)
                .phoneNumber(purchase.getPurchaserPhoneNumber())
                .buyerName(purchase.getPurchaserName())
                .contentTitle(purchase.getContent().getTitle())
                .refundedAmount(order.getFinalPrice())
                .build());

        // 주문 상태는 PayplePaymentService에서 이미 CANCELLED로 변경됨
        return AdminOrderCancelRequestDTO.builder()
            .merchantUid(response.getMerchantUid())
            .action(action)
            .resultStatus(Order.OrderStatus.CANCELLED)
            .message("결제 취소가 승인되었습니다.")
            .processedAt(response.getCanceledAt())
            .build();

      } catch (Exception e) {
        log.error("결제 취소 처리 실패 - merchantUid: {}", merchantUid, e);
        throw new RuntimeException("결제 취소 처리 실패: " + e.getMessage(), e);
      }

    } else if ("reject".equalsIgnoreCase(action)) {
      // 취소 거절 - 주문 상태를 다시 결제 완료로 변경
      order.changeStatus(Order.OrderStatus.PAID);
      orderRepository.save(order);

      return AdminOrderCancelRequestDTO.builder()
          .merchantUid(merchantUid)
          .action(action)
          .resultStatus(Order.OrderStatus.PAID)
          .message("결제 취소 요청이 거절되었습니다.")
          .processedAt(java.time.LocalDateTime.now())
          .build();

    } else {
      throw new IllegalArgumentException("유효하지 않은 action입니다: " + action);
    }
  }

  private AdminOrderSummaryInfoDTO convertFlatDTOToInfoResponse(FlatAdminOrderSummaryInfoDTO flat) {
    return AdminOrderSummaryInfoDTO.builder()
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
