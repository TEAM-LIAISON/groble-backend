package liaison.groble.application.payment.validator;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.exception.PaymentValidationException;
import liaison.groble.application.payment.exception.UnauthorizedPaymentException;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.PayplePayment;

import lombok.extern.slf4j.Slf4j;

/**
 * 결제 관련 검증을 담당하는 서비스
 *
 * <p>모든 결제 관련 검증 로직을 중앙 집중화하여 일관성 있는 검증을 제공합니다.
 */
@Slf4j
@Component
public class PaymentValidator {
  /**
   * 주문 소유권 검증
   *
   * @param order 주문
   * @param userId 요청 사용자 ID
   * @throws UnauthorizedPaymentException 권한이 없는 경우
   */
  public void validateOrderOwnership(Order order, Long userId) {
    if (!order.getUser().getId().equals(userId)) {
      log.warn(
          "주문 접근 권한 없음 - orderId: {}, orderUserId: {}, requestUserId: {}",
          order.getId(),
          order.getUser().getId(),
          userId);
      throw new UnauthorizedPaymentException("주문에 대한 접근 권한이 없습니다");
    }
  }

  public void validateOrderOwnershipForGuest(Order order, Long guestUserId) {
    if (!order.getGuestUser().getId().equals(guestUserId)) {
      log.warn(
          "주문 접근 권한 없음 - orderId: {}, orderGuestUserId: {}, requestGuestUserId: {}",
          order.getId(),
          order.getGuestUser().getId(),
          guestUserId);
      throw new UnauthorizedPaymentException("주문에 대한 접근 권한이 없습니다");
    }
  }

  /**
   * 주문 상태 검증
   *
   * @param order 주문
   * @param expectedStatus 예상 상태
   * @throws PaymentValidationException 상태가 일치하지 않는 경우
   */
  public void validateOrderStatus(Order order, Order.OrderStatus expectedStatus) {
    if (order.getStatus() != expectedStatus) {
      log.warn(
          "주문 상태 불일치 - orderId: {}, expected: {}, actual: {}",
          order.getId(),
          expectedStatus,
          order.getStatus());
      throw new PaymentValidationException(
          String.format("주문 상태가 올바르지 않습니다. 현재 상태: %s", order.getStatus()));
    }
  }

  /**
   * 결제 금액 검증
   *
   * @param orderAmount 주문 금액
   * @param paymentAmount 결제 금액 (문자열)
   * @throws PaymentValidationException 금액이 일치하지 않는 경우
   */
  public void validatePaymentAmount(BigDecimal orderAmount, String paymentAmount) {
    try {
      BigDecimal paymentAmountDecimal = new BigDecimal(paymentAmount);

      if (orderAmount.compareTo(paymentAmountDecimal) != 0) {
        log.warn("결제 금액 불일치 - orderAmount: {}, paymentAmount: {}", orderAmount, paymentAmount);
        throw new PaymentValidationException(
            String.format("결제 금액이 일치하지 않습니다. 주문금액: %s원, 결제금액: %s원", orderAmount, paymentAmount));
      }
    } catch (NumberFormatException e) {
      log.error("결제 금액 형식 오류 - paymentAmount: {}", paymentAmount);
      throw new PaymentValidationException("결제 금액 형식이 올바르지 않습니다");
    }
  }

  /**
   * 취소 가능 상태 검증
   *
   * @param order 주문
   * @throws PaymentValidationException 취소 불가능한 상태인 경우
   */
  public void validateCancellableStatus(Order order) {
    if (order.getStatus() != Order.OrderStatus.CANCEL_REQUEST) {
      log.warn("취소 불가능한 주문 상태 - orderId: {}, status: {}", order.getId(), order.getStatus());
      throw new PaymentValidationException("결제 취소가 불가능한 상태입니다. 취소 요청 상태에서만 환불이 가능합니다.");
    }
  }

  /**
   * 결제 정보 일관성 검증
   *
   * @param payment 저장된 결제 정보
   * @param approvalResult 승인 결과
   * @throws PaymentValidationException 정보가 일치하지 않는 경우
   */
  public void validatePaymentConsistency(
      PayplePayment payment, PaypleApprovalResult approvalResult) {

    // 필수 정보 검증
    validateRequiredFields(payment, approvalResult);

    // 구매자 정보 검증
    validateBuyerInfo(payment, approvalResult);

    // 선택적 정보 검증
    validateOptionalFields(payment, approvalResult);

    log.debug("결제 정보 일관성 검증 완료 - merchantUid: {}", payment.getPcdPayOid());
  }

  /** 필수 결제 정보 검증 */
  private void validateRequiredFields(PayplePayment payment, PaypleApprovalResult approvalResult) {

    // 주문번호 검증
    if (!Objects.equals(payment.getPcdPayOid(), approvalResult.getPayOid())) {
      throw new PaymentValidationException(
          String.format(
              "주문번호 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayOid(), approvalResult.getPayOid()));
    }

    // 결제 금액 검증
    if (!Objects.equals(payment.getPcdPayTotal(), approvalResult.getPayTotal())) {
      throw new PaymentValidationException(
          String.format(
              "결제금액 불일치 - DB: %s, 승인결과: %s",
              payment.getPcdPayTotal(), approvalResult.getPayTotal()));
    }

    // 상품명 검증
    if (!Objects.equals(payment.getPcdPayGoods(), approvalResult.getPayGoods())) {
      log.warn(
          "상품명 불일치 - DB: {}, 승인결과: {}", payment.getPcdPayGoods(), approvalResult.getPayGoods());
    }
  }

  /** 구매자 정보 검증 */
  private void validateBuyerInfo(PayplePayment payment, PaypleApprovalResult approvalResult) {

    validateIfNotNull(payment.getPcdPayerName(), approvalResult.getPayerName(), "구매자명");

    validateIfNotNull(payment.getPcdPayerHp(), approvalResult.getPayerHp(), "구매자 연락처");
  }

  /** 선택적 결제 정보 검증 */
  private void validateOptionalFields(PayplePayment payment, PaypleApprovalResult approvalResult) {

    // 과세 여부
    validateIfNotNull(payment.getPcdPayIsTax(), approvalResult.getPayIsTax(), "과세여부");

    // 복합과세 부가세
    if (payment.getPcdPayTaxTotal() != null) {
      if (!Objects.equals(payment.getPcdPayTaxTotal(), approvalResult.getPayTaxTotal())) {
        log.warn(
            "복합과세 부가세 불일치 - DB: {}, 승인결과: {}",
            payment.getPcdPayTaxTotal(),
            approvalResult.getPayTaxTotal());
      }
    }

    // 할부개월수
    if (payment.getPcdPayCardQuota() != null && approvalResult.getPayCardQuota() != null) {
      if (!Objects.equals(payment.getPcdPayCardQuota(), approvalResult.getPayCardQuota())) {
        log.warn(
            "할부개월수 불일치 - DB: {}, 승인결과: {}",
            payment.getPcdPayCardQuota(),
            approvalResult.getPayCardQuota());
      }
    }
  }

  /** null이 아닌 경우에만 값 검증 */
  private void validateIfNotNull(String dbValue, String approvalValue, String fieldName) {
    if (dbValue != null && approvalValue != null && !dbValue.equals(approvalValue)) {
      log.warn("{} 불일치 - DB: {}, 승인결과: {}", fieldName, dbValue, approvalValue);
    }
  }
}
