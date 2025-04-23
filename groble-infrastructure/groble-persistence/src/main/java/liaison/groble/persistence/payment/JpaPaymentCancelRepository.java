package liaison.groble.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.PaymentCancel;

public interface JpaPaymentCancelRepository extends JpaRepository<PaymentCancel, Long> {}
