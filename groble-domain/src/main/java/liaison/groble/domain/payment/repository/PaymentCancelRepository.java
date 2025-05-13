package liaison.groble.domain.payment.repository;

import liaison.groble.domain.payment.entity.PaymentCancel;

public interface PaymentCancelRepository {
  PaymentCancel save(PaymentCancel paymentCancel);
}
