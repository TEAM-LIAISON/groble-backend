package liaison.groble.application.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.command.PaymentCommandExecutor;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.billing.BillingKeyAction;
import liaison.groble.application.payment.dto.billing.BillingKeyInfoDTO;
import liaison.groble.application.payment.dto.billing.RegisterBillingKeyCommand;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentResult;
import liaison.groble.application.payment.event.PaymentEventPublisher;
import liaison.groble.application.subscription.service.SubscriptionRecurringOrderFactory;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionPaymentServiceTest {

  @Mock private PaymentCommandExecutor paymentCommandExecutor;
  @Mock private SubscriptionPaymentMetadataProvider metadataProvider;
  @Mock private BillingKeyService billingKeyService;
  @Mock private OrderReader orderReader;
  @Mock private PaypleApiClient paypleApiClient;
  @Mock private PaymentTransactionService paymentTransactionService;
  @Mock private PaymentEventPublisher paymentEventPublisher;
  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private SubscriptionRecurringOrderFactory subscriptionRecurringOrderFactory;

  private SubscriptionPaymentService service;

  @BeforeEach
  void setUp() {
    service =
        new SubscriptionPaymentService(
            paymentCommandExecutor,
            metadataProvider,
            billingKeyService,
            orderReader,
            paypleApiClient,
            paymentTransactionService,
            paymentEventPublisher,
            subscriptionRepository,
            subscriptionRecurringOrderFactory);
  }

  @Test
  void confirmBillingKeyRegistrationWithoutMerchantUidReturnsMetadataFromUser() {
    // given
    Long userId = 1L;
    PaypleAuthResultDTO authResult =
        PaypleAuthResultDTO.builder()
            .payerId("billing-key")
            .payCardName("Groble Card")
            .payCardNum("1111-****-****-2222")
            .payMsg("카드등록이 완료되었습니다.")
            .build();

    when(billingKeyService.registerBillingKey(eq(userId), any(RegisterBillingKeyCommand.class)))
        .thenReturn(
            BillingKeyInfoDTO.builder()
                .billingKey("billing-key")
                .cardName("Groble Card")
                .cardNumberMasked("1111-****-****-2222")
                .build());

    SubscriptionPaymentMetadata metadata =
        SubscriptionPaymentMetadata.builder()
            .billingKeyAction(BillingKeyAction.REGISTER)
            .hasActiveBillingKey(true)
            .billingKeyId("billing-key")
            .merchantUserKey("1")
            .defaultPayMethod("CARD")
            .payWork("AUTH")
            .cardVer("02")
            .regularFlag("Y")
            .nextPaymentDate(LocalDate.of(2024, 1, 1))
            .payYear("2024")
            .payMonth("01")
            .payDay("01")
            .requiresImmediateCharge(false)
            .build();

    when(metadataProvider.buildForUser(userId)).thenReturn(Optional.of(metadata));

    // when
    SubscriptionPaymentResult result = service.confirmBillingKeyRegistration(userId, authResult);

    // then
    verify(orderReader, never()).getOrderByMerchantUidAndUserId(anyString(), anyLong());
    assertThat(result.getMerchantUid()).isNull();
    assertThat(result.getStatus()).isEqualTo("BILLING_KEY_REGISTERED");
    assertThat(result.getMetadata()).isEqualTo(metadata);
  }

  @Test
  void confirmBillingKeyRegistrationWithMerchantUidUsesOrderMetadata() {
    // given
    Long userId = 2L;
    String merchantUid = "ORD-001";
    PaypleAuthResultDTO authResult =
        PaypleAuthResultDTO.builder()
            .payOid(merchantUid)
            .payerId("billing-key")
            .payCardName("Groble Card")
            .payCardNum("1111-****-****-2222")
            .payMsg("카드등록이 완료되었습니다.")
            .build();

    when(billingKeyService.registerBillingKey(eq(userId), any(RegisterBillingKeyCommand.class)))
        .thenReturn(
            BillingKeyInfoDTO.builder()
                .billingKey("billing-key")
                .cardName("Groble Card")
                .cardNumberMasked("1111-****-****-2222")
                .build());

    Order order = mock(Order.class);
    when(order.getMerchantUid()).thenReturn(merchantUid);

    SubscriptionPaymentMetadata metadata =
        SubscriptionPaymentMetadata.builder()
            .billingKeyAction(BillingKeyAction.REUSE)
            .hasActiveBillingKey(true)
            .billingKeyId("billing-key")
            .merchantUserKey("2")
            .defaultPayMethod("CARD")
            .payWork("AUTH")
            .cardVer("02")
            .regularFlag("Y")
            .nextPaymentDate(LocalDate.of(2024, 2, 1))
            .payYear("2024")
            .payMonth("02")
            .payDay("01")
            .requiresImmediateCharge(true)
            .build();

    when(orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId)).thenReturn(order);
    when(metadataProvider.buildForOrder(order)).thenReturn(Optional.of(metadata));

    // when
    SubscriptionPaymentResult result = service.confirmBillingKeyRegistration(userId, authResult);

    // then
    verify(orderReader).getOrderByMerchantUidAndUserId(merchantUid, userId);
    assertThat(result.getMerchantUid()).isEqualTo(merchantUid);
    assertThat(result.getMetadata()).isEqualTo(metadata);
  }
}
