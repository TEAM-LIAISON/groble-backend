package liaison.groble.api.server.payment.mapper;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.payment.request.PaymentRequest;
import liaison.groble.api.model.payment.response.PaymentCancelResponse;
import liaison.groble.api.model.payment.response.PaymentCompleteResponse;
import liaison.groble.api.model.payment.response.PaymentInfo;
import liaison.groble.api.model.payment.response.PaymentRequestResponse;
import liaison.groble.api.model.payment.response.PaypleAuthResponse;
import liaison.groble.application.payment.dto.PaymentCancelResponseDto;
import liaison.groble.application.payment.dto.PaymentCompleteResponseDto;
import liaison.groble.application.payment.dto.PaymentInfoDto;
import liaison.groble.application.payment.dto.PaymentRequestDto;
import liaison.groble.application.payment.dto.PaymentRequestResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PayplePaymentResult;
import liaison.groble.application.payment.dto.PayplePaymentResultDto;
import liaison.groble.application.payment.dto.link.PaypleLinkResponse;
import liaison.groble.application.payment.dto.link.PaypleLinkResponseDto;

@Component
public class PayplePaymentMapper {

  public PaypleAuthResponse toPaypleAuthResponse(PaypleAuthResponseDto paypleAuthResponseDto) {
    return PaypleAuthResponse.builder()
        .clientKey(paypleAuthResponseDto.getClientKey())
        .authKey(paypleAuthResponseDto.getAuthKey())
        .returnUrl(paypleAuthResponseDto.getReturnUrl())
        .build();
  }

  public PaymentRequestDto toPaymentRequestDto(PaymentRequest paymentRequest) {
    return PaymentRequestDto.builder()
        .price(paymentRequest.getPrice())
        .payMethod(paymentRequest.getPayMethod())
        .productName(paymentRequest.getProductName())
        .userName(paymentRequest.getUserName())
        .userPhone(paymentRequest.getUserPhone())
        .userEmail(paymentRequest.getUserEmail())
        .build();
  }

  public PaymentRequestResponse toPaymentRequestResponse(
      PaymentRequestResponseDto paymentRequestResponseDto) {
    return PaymentRequestResponse.builder()
        .orderId(paymentRequestResponseDto.getOrderId())
        .price(paymentRequestResponseDto.getPrice())
        .productName(paymentRequestResponseDto.getProductName())
        .status(paymentRequestResponseDto.getStatus())
        .build();
  }

  public PayplePaymentResultDto toPaymentResultDto(PayplePaymentResult payplePaymentResult) {
    return PayplePaymentResultDto.builder()
        .payRst(payplePaymentResult.getPayRst())
        .payMsg(payplePaymentResult.getPayMsg())
        .payOid(payplePaymentResult.getPayOid())
        .payerId(payplePaymentResult.getPayerId())
        .payTime(payplePaymentResult.getPayTime())
        .payCardName(payplePaymentResult.getPayCardName())
        .payCardNum(payplePaymentResult.getPayCardNum())
        .payBankName(payplePaymentResult.getPayBankName())
        .payBankNum(payplePaymentResult.getPayBankNum())
        .build();
  }

  public PaymentCompleteResponse toPaymentCompleteResponse(
      PaymentCompleteResponseDto paymentCompleteResponseDto) {
    return PaymentCompleteResponse.builder()
        .orderId(paymentCompleteResponseDto.getOrderId())
        .status(paymentCompleteResponseDto.getStatus())
        .price(paymentCompleteResponseDto.getPrice())
        .productName(paymentCompleteResponseDto.getProductName())
        .paymentDate(paymentCompleteResponseDto.getPaymentDate())
        .build();
  }

  public PaymentCancelResponse toPaymentCancelResponse(
      PaymentCancelResponseDto paymentCancelResponseDto) {
    return PaymentCancelResponse.builder()
        .merchantUid(paymentCancelResponseDto.getMerchantUid())
        .status(paymentCancelResponseDto.getStatus())
        .canceledAt(paymentCancelResponseDto.getCanceledAt())
        .cancelReason(paymentCancelResponseDto.getCancelReason())
        .build();
  }

  public PaymentInfo toPaymentInfo(PaymentInfoDto paymentInfoDto) {
    return PaymentInfo.builder()
        .orderId(paymentInfoDto.getOrderId())
        .userId(paymentInfoDto.getUserId())
        .price(paymentInfoDto.getPrice())
        .payMethod(paymentInfoDto.getPayMethod())
        .status(paymentInfoDto.getStatus())
        .productName(paymentInfoDto.getProductName())
        .paymentDate(paymentInfoDto.getPaymentDate())
        .cardName(paymentInfoDto.getCardName())
        .cardNumber(paymentInfoDto.getCardNumber())
        .receiptUrl(paymentInfoDto.getReceiptUrl())
        .build();
  }

  public PaypleLinkResponse toPaypleLinkResponse(PaypleLinkResponseDto paypleLinkResponseDto) {
    return PaypleLinkResponse.builder()
        .linkRst(paypleLinkResponseDto.getLinkRst())
        .linkMsg(paypleLinkResponseDto.getLinkMsg())
        .linkKey(paypleLinkResponseDto.getLinkKey())
        .linkUrl(paypleLinkResponseDto.getLinkUrl())
        .linkOid(paypleLinkResponseDto.getLinkOid())
        .linkGoods(paypleLinkResponseDto.getLinkGoods())
        .linkTotal(paypleLinkResponseDto.getLinkTotal())
        .linkTime(paypleLinkResponseDto.getLinkTime())
        .linkExpire(paypleLinkResponseDto.getLinkExpire())
        .build();
  }
}
