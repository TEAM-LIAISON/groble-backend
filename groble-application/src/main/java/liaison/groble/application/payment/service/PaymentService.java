package liaison.groble.application.payment.service;

import org.springframework.stereotype.Service;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaymentCancelDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.enums.CancelReason;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
import liaison.groble.domain.purchase.entity.Purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  // Reader
  private final OrderReader orderReader;
  private final PaymentReader paymentReader;
  private final PurchaseReader purchaseReader;

  public void requestPaymentCancel(
      Long userId, String merchantUid, PaymentCancelDTO paymentCancelDTO) {
    CancelReason cancelReason = parseCancelReason(paymentCancelDTO.getCancelReason());

    log.info("결제 취소 요청 처리 시작 - 주문번호: {}, 사유: {}", merchantUid, cancelReason.getDescription());
    try {
      Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);
      PayplePayment payplePayment = paymentReader.getPayplePaymentByOid(merchantUid);
      Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());

      validateRequestCancellableStatus(order, payplePayment);
      log.info("환불 요청 가능 상태 검증 완료 - 주문번호: {}", merchantUid);

      // 환불 요청
      handlePaymentCancelSuccess(
          order, payplePayment, purchase, cancelReason, paymentCancelDTO.getDetailReason());
    } catch (IllegalArgumentException | IllegalStateException e) {
      log.warn("결제 취소 처리 실패 - 주문번호: {}, 사유: {}", merchantUid, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("결제 취소 처리 중 예상치 못한 오류 발생 - 주문번호: {}", merchantUid, e);
      throw e;
    }
  }

  private void validateRequestCancellableStatus(Order order, PayplePayment payplePayment) {
    // 주문 상태 검증
    if (order.getStatus() != Order.OrderStatus.PAID) {
      throw new IllegalStateException(
          String.format(
              "결제 완료된 주문만 취소 요청할 수 있습니다. orderId: %d, status: %s",
              order.getId(), order.getStatus()));
    }

    // 결제 상태 검증
    if (payplePayment.getStatus() != PayplePaymentStatus.COMPLETED) {
      throw new IllegalStateException(
          String.format(
              "완료된 결제만 취소 요청할 수 있습니다. 주문번호: %s, 결제상태: %s",
              payplePayment.getPcdPayOid(), payplePayment.getStatus()));
    }
  }

  private void handlePaymentCancelSuccess(
      Order order,
      PayplePayment payplePayment,
      Purchase purchase,
      CancelReason cancelReason,
      String detailReason) {
    Payment payment = order.getPayment();
    payment.cancelRequest(cancelReason.getDescription());
    order.cancelRequestOrder(cancelReason, detailReason);
    purchase.cancelRequest(cancelReason.getDescription());
  }

  private CancelReason parseCancelReason(String cancelReason) {
    try {
      return CancelReason.valueOf(cancelReason.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 취소 사유 유형입니다: " + cancelReason);
    }
  }
}
