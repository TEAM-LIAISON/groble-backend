package liaison.groble.mapping.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import liaison.groble.api.model.content.response.pay.ContentPayPageResponse;
import liaison.groble.api.model.order.response.CreateOrderResponse;
import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.model.payment.request.PaypleBillingRegistrationRequest;
import liaison.groble.api.model.payment.request.SubscriptionCancelRequest;
import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.api.model.payment.response.BillingKeyResponse;
import liaison.groble.api.model.payment.response.PaymentCancelInfoResponse;
import liaison.groble.api.model.payment.response.PaypleBillingAuthResponse;
import liaison.groble.api.model.payment.response.PaypleSubscriptionResultResponse;
import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.billing.BillingKeyInfoDTO;
import liaison.groble.application.payment.dto.billing.RegisterBillingKeyCommand;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentMetadata;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentResult;
import liaison.groble.application.payment.dto.cancel.PaymentCancelDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelInfoDTO;
import liaison.groble.application.subscription.dto.SubscriptionCancelDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface PaymentMapper {
  // ====== 📥 Request → DTO 변환 ======
  PaypleAuthResultDTO toPaypleAuthResultDTO(PaypleAuthResultRequest paypleAuthResultRequest);

  RegisterBillingKeyCommand toRegisterBillingKeyCommand(
      PaypleBillingRegistrationRequest paypleBillingRegistrationRequest);

  BillingKeyResponse toBillingKeyResponse(BillingKeyInfoDTO dto);

  PaymentCancelDTO toPaymentCancelDTO(PaymentCancelRequest paymentCancelRequest);

  // ====== 📤 DTO → Response 변환 ======
  PaymentCancelInfoResponse toPaymentCancelInfoResponse(PaymentCancelInfoDTO paymentCancelInfoDTO);

  AppCardPayplePaymentResponse toAppCardPayplePaymentResponse(
      AppCardPayplePaymentDTO appCardPayplePaymentDTO);

  PaypleBillingAuthResponse toPaypleBillingAuthResponse(
      PaypleAuthResponseDTO paypleAuthResponseDTO);

  @Mapping(target = "subscriptionMeta", source = "metadata")
  @Mapping(target = "paypleOptions", source = "metadata")
  PaypleSubscriptionResultResponse toPaypleSubscriptionResultResponse(
      SubscriptionPaymentResult subscriptionPaymentResult);

  default ContentPayPageResponse.SubscriptionMetaResponse toSubscriptionMetaResponse(
      SubscriptionPaymentMetadata metadata) {
    if (metadata == null) {
      return null;
    }
    return ContentPayPageResponse.SubscriptionMetaResponse.builder()
        .hasActiveBillingKey(metadata.isHasActiveBillingKey())
        .billingKeyId(metadata.getBillingKeyId())
        .merchantUserKey(metadata.getMerchantUserKey())
        .defaultPayMethod(metadata.getDefaultPayMethod())
        .payWork(metadata.getPayWork())
        .cardVer(metadata.getCardVer())
        .regularFlag(metadata.getRegularFlag())
        .nextPaymentDate(metadata.getNextPaymentDate())
        .payYear(metadata.getPayYear())
        .payMonth(metadata.getPayMonth())
        .payDay(metadata.getPayDay())
        .requiresImmediateCharge(metadata.isRequiresImmediateCharge())
        .build();
  }

  default CreateOrderResponse.PaypleOptionsResponse toPaypleOptionsResponse(
      SubscriptionPaymentMetadata metadata) {
    if (metadata == null) {
      return null;
    }
    return CreateOrderResponse.PaypleOptionsResponse.builder()
        .billingKeyAction(
            metadata.getBillingKeyAction() != null ? metadata.getBillingKeyAction().name() : null)
        .payWork(metadata.getPayWork())
        .cardVer(metadata.getCardVer())
        .regularFlag(metadata.getRegularFlag())
        .defaultPayMethod(metadata.getDefaultPayMethod())
        .merchantUserKey(metadata.getMerchantUserKey())
        .billingKeyId(metadata.getBillingKeyId())
        .nextPaymentDate(metadata.getNextPaymentDate())
        .payYear(metadata.getPayYear())
        .payMonth(metadata.getPayMonth())
        .payDay(metadata.getPayDay())
        .build();
  }

  SubscriptionCancelDTO toSubscriptionCancelDTO(
      SubscriptionCancelRequest subscriptionCancelRequest);
}
