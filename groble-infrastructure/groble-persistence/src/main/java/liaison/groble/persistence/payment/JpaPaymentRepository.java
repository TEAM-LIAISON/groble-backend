package liaison.groble.persistence.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.payment.entity.Payment;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {}
