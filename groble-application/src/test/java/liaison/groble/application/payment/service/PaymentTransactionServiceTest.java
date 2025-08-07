package liaison.groble.application.payment.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.validator.PaymentValidator;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceTest {
  @Mock private OrderReader orderReader;
  @Mock private PurchaseReader purchaseReader;
  @Mock private PaymentReader paymentReader;
  @Mock private PaymentValidator paymentValidator;
  @Mock private PayplePaymentRepository payplePaymentRepository;
  @Mock private PaymentRepository paymentRepository;
  @Mock private PurchaseRepository purchaseRepository;

  @InjectMocks private PaymentTransactionService paymentTransactionService;
}
