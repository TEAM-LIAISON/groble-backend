package liaison.groble.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.payment.entity.PayplePayment;

public interface PayplePaymentRepository {

  PayplePayment save(PayplePayment payplePayment);

  Optional<PayplePayment> findByOrderId(String orderId);

  List<PayplePayment> findByUserId(Long userId);

  List<PayplePayment> findByUserIdAndBillingKey(Long userId, String billingKey);

  List<PayplePayment> findByBillingKey(String billingKey);
}
