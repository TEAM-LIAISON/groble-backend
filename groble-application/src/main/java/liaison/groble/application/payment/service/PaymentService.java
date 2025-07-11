package liaison.groble.application.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.cancel.PaymentCancelDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelInfoDTO;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.purchase.enums.CancelReason;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  // Reader
  private final OrderReader orderReader;
  private final PaymentReader paymentReader;

  @Transactional
  public void requestPaymentCancel(
      Long userId, String merchantUid, PaymentCancelDTO paymentCancelDTO) {
    CancelReason cancelReason = parseCancelReason(paymentCancelDTO.getCancelReason());

    log.info("결제 취소 요청 처리 시작 - 주문번호: {}, 사유: {}", merchantUid, cancelReason.getDescription());
    try {
      Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);
      PayplePayment payplePayment = paymentReader.getPayplePaymentByOid(merchantUid);

      validateRequestCancellableStatus(order, payplePayment);
      log.info("환불 요청 가능 상태 검증 완료 - 주문번호: {}", merchantUid);

      // 환불 요청
      handlePaymentCancelSuccess(order, cancelReason, paymentCancelDTO.getDetailReason());
    } catch (IllegalArgumentException | IllegalStateException e) {
      log.warn("결제 취소 처리 실패 - 주문번호: {}, 사유: {}", merchantUid, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("결제 취소 처리 중 예상치 못한 오류 발생 - 주문번호: {}", merchantUid, e);
      throw e;
    }
  }

  @Transactional(readOnly = true)
  public PaymentCancelInfoDTO getPaymentCancelInfo(Long userId, String merchantUid) {
    PayplePayment payplePayment = paymentReader.getPayplePaymentByOid(merchantUid);
    Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);

    if (order.getStatus() != Order.OrderStatus.CANCEL_REQUEST) {
      throw new IllegalStateException("취소 요청 상태가 아닌 주문입니다.");
    }

    return buildPaymentCancelInfoDTO(merchantUid, order, payplePayment);
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
  }

  private void handlePaymentCancelSuccess(
      Order order, CancelReason cancelReason, String detailReason) {
    order.cancelRequestOrder(cancelReason, detailReason);
  }

  private CancelReason parseCancelReason(String cancelReason) {
    try {
      return CancelReason.valueOf(cancelReason.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 취소 사유 유형입니다: " + cancelReason);
    }
  }

  private PaymentCancelInfoDTO buildPaymentCancelInfoDTO(
      String merchantUid, Order order, PayplePayment payplePayment) {
    return PaymentCancelInfoDTO.builder()
        .merchantUid(merchantUid)
        .originalPrice(order.getOriginalPrice())
        .discountPrice(order.getDiscountPrice())
        .finalPrice(order.getFinalPrice())
        .payType(payplePayment.getPcdPayType())
        .payCardName(payplePayment.getPcdPayCardName())
        .payCardNum(payplePayment.getPcdPayCardNum())
        .build();
  }
}
