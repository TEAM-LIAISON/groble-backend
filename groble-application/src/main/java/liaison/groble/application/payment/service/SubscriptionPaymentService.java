package liaison.groble.application.payment.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.command.PaymentCommandExecutor;
import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaymentAuthInfo;
import liaison.groble.application.payment.dto.PaypleApprovalResult;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.billing.BillingKeyAction;
import liaison.groble.application.payment.dto.billing.RegisterBillingKeyCommand;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentResult;
import liaison.groble.application.payment.dto.completion.PaymentCompletionResult;
import liaison.groble.application.payment.event.PaymentEventPublisher;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.Purchaser;
import liaison.groble.external.adapter.payment.PaypleSimplePayRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPaymentService {

  private static final String PAYMENT_TYPE_CARD = "card";

  private final PaymentCommandExecutor paymentCommandExecutor;
  private final SubscriptionPaymentMetadataProvider metadataProvider;
  private final BillingKeyService billingKeyService;
  private final OrderReader orderReader;
  private final PaypleApiClient paypleApiClient;
  private final PaymentTransactionService paymentTransactionService;
  private final PaymentEventPublisher paymentEventPublisher;

  @Transactional
  public SubscriptionPaymentResult chargeWithBillingKey(Long userId, String merchantUid) {
    Order order = orderReader.getOrderByMerchantUidAndUserId(merchantUid, userId);

    String billingKey = billingKeyService.getActiveBillingKey(userId).getBillingKey();

    PaypleAuthResultDTO authPayload = buildAuthPayload(order, userId, billingKey);
    PaymentAuthInfo authInfo = paymentTransactionService.saveAuthAndValidate(userId, authPayload);

    PaypleSimplePayRequest request = buildSimplePayRequest(order, userId, billingKey);
    PaypleApprovalResult approvalResult = paypleApiClient.requestSimplePayment(request);

    if (!approvalResult.isSuccess()) {
      paymentTransactionService.handleApprovalFailure(
          authInfo.getOrderId(), approvalResult.getPayCode(), approvalResult.getPayMsg());
      throw new PaypleApiException(
          approvalResult.getErrorMessage() != null
              ? approvalResult.getErrorMessage()
              : "페이플 빌링키 결제에 실패했습니다.");
    }

    PaymentCompletionResult completionResult =
        paymentTransactionService.completePayment(authInfo, approvalResult);
    paymentEventPublisher.publishPaymentCompleted(completionResult);

    Order updatedOrder = orderReader.getOrderById(completionResult.getOrderId());
    return buildResult(
        updatedOrder, true, "PAID", approvalResult.getPayMsg(), completionResult.getAmount());
  }

  @Transactional
  public SubscriptionPaymentResult confirmSubscriptionPayment(
      Long userId, PaypleAuthResultDTO authResult) {
    AppCardPayplePaymentDTO paymentResult =
        paymentCommandExecutor.execute(
            paymentCommandExecutor.createAppCardPaymentCommand(authResult, userId, null));

    if (!"success".equalsIgnoreCase(paymentResult.getPayRst())) {
      throw new PaypleApiException(
          paymentResult.getPayMsg() != null ? paymentResult.getPayMsg() : "페이플 결제 승인에 실패했습니다.");
    }

    RegisterBillingKeyCommand command =
        new RegisterBillingKeyCommand(
            authResult.getPayerId(), paymentResult.getPayCardName(), paymentResult.getPayCardNum());
    billingKeyService.registerBillingKey(userId, command);

    Order order = orderReader.getOrderByMerchantUidAndUserId(authResult.getPayOid(), userId);
    BigDecimal amount = toBigDecimal(paymentResult.getPayTotal());

    return buildResult(order, true, "PAID", paymentResult.getPayMsg(), amount);
  }

  @Transactional
  public SubscriptionPaymentResult confirmBillingKeyRegistration(
      Long userId, PaypleAuthResultDTO authResult) {
    RegisterBillingKeyCommand command =
        new RegisterBillingKeyCommand(
            authResult.getPayerId(), authResult.getPayCardName(), authResult.getPayCardNum());
    billingKeyService.registerBillingKey(userId, command);

    Order order = orderReader.getOrderByMerchantUidAndUserId(authResult.getPayOid(), userId);

    return buildResult(order, true, "BILLING_KEY_REGISTERED", authResult.getPayMsg(), null);
  }

  private SubscriptionPaymentResult buildResult(
      Order order, boolean success, String status, String message, BigDecimal amount) {
    SubscriptionPaymentMetadata metadata = metadataProvider.buildForOrder(order).orElse(null);

    return SubscriptionPaymentResult.builder()
        .merchantUid(order.getMerchantUid())
        .success(success)
        .status(status)
        .message(message)
        .totalAmount(amount)
        .metadata(metadata)
        .build();
  }

  private PaypleAuthResultDTO buildAuthPayload(Order order, Long userId, String billingKey) {
    String totalPrice = order.getFinalPrice().toPlainString();
    var content = order.getOrderItems().get(0).getContent();
    Purchaser purchaser = order.getPurchaser();

    return PaypleAuthResultDTO.builder()
        .payRst("success")
        .pcdPayMethod(PAYMENT_TYPE_CARD)
        .payType(PAYMENT_TYPE_CARD)
        .payWork(BillingKeyAction.REUSE.getPayWork())
        .payerNo(userId.toString())
        .payerName(purchaser != null ? purchaser.getName() : null)
        .payerHp(purchaser != null ? purchaser.getPhone() : null)
        .payerEmail(purchaser != null ? purchaser.getEmail() : null)
        .payerId(billingKey)
        .payOid(order.getMerchantUid())
        .payGoods(content != null ? content.getTitle() : order.getMerchantUid())
        .payTotal(totalPrice)
        .payAmount(totalPrice)
        .payCardQuota("00")
        .build();
  }

  private PaypleSimplePayRequest buildSimplePayRequest(
      Order order, Long userId, String billingKey) {
    Purchaser purchaser = order.getPurchaser();
    var content = order.getOrderItems().get(0).getContent();
    String totalPrice = order.getFinalPrice().toPlainString();

    return PaypleSimplePayRequest.builder()
        .payType(PAYMENT_TYPE_CARD)
        .payerId(billingKey)
        .payGoods(content != null ? content.getTitle() : order.getMerchantUid())
        .payTotal(totalPrice)
        .payOid(order.getMerchantUid())
        .payerNo(userId.toString())
        .payerName(purchaser != null ? purchaser.getName() : null)
        .payerHp(purchaser != null ? purchaser.getPhone() : null)
        .payerEmail(purchaser != null ? purchaser.getEmail() : null)
        .simpleFlag("Y")
        .build();
  }

  private BigDecimal toBigDecimal(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return new BigDecimal(value);
    } catch (NumberFormatException ex) {
      log.warn("금액 변환 실패 - value: {}", value, ex);
      return null;
    }
  }
}
