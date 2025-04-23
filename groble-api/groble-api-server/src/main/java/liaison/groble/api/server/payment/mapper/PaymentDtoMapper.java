package liaison.groble.api.server.payment.mapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import liaison.groble.api.model.payment.request.PaymentApproveRequest;
import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaymentPrepareRequest;
import liaison.groble.api.model.payment.request.VirtualAccountRequest;
import liaison.groble.api.model.payment.response.PaymentPrepareResponse;
import liaison.groble.api.model.payment.response.PaymentResponse;
import liaison.groble.application.payment.dto.PaymentApproveDto;
import liaison.groble.application.payment.dto.PaymentCancelDto;
import liaison.groble.application.payment.dto.PaymentPrepareDto;
import liaison.groble.application.payment.dto.PaymentResultDto;
import liaison.groble.application.payment.dto.VirtualAccountDto;

@Component
public class PaymentDtoMapper {

  /** API PaymentPrepareRequest → Service PaymentPrepareDto 변환 */
  public PaymentPrepareDto toServicePaymentPrepareDto(PaymentPrepareRequest request) {
    Map<String, Object> additionalData = new HashMap<>();

    if (request.getAdditionalData() != null) {
      additionalData.putAll(request.getAdditionalData());
    }

    return PaymentPrepareDto.builder()
        .orderId(request.getOrderId())
        .paymentMethod(request.getPaymentMethod())
        .customerName(request.getCustomerName())
        .customerEmail(request.getCustomerEmail())
        .customerPhone(request.getCustomerPhone())
        .successUrl(request.getSuccessUrl())
        .failUrl(request.getFailUrl())
        .additionalData(additionalData)
        .build();
  }

  /** API PaymentApproveRequest → Service PaymentApproveDto 변환 */
  public PaymentApproveDto toServicePaymentApproveDto(PaymentApproveRequest request) {
    return PaymentApproveDto.builder()
        .paymentKey(request.getPaymentKey())
        .orderId(request.getMerchantUid())
        .amount(request.getAmount())
        .build();
  }

  /** API PaymentCancelRequest → Service PaymentCancelDto 변환 */
  public PaymentCancelDto toServicePaymentCancelDto(PaymentCancelRequest request) {
    return PaymentCancelDto.builder()
        .paymentKey(request.getPaymentKey())
        .amount(request.getAmount())
        .reason(request.getReason())
        .build();
  }

  /** API VirtualAccountRequest → Service VirtualAccountDto 변환 */
  public VirtualAccountDto toServiceVirtualAccountDto(VirtualAccountRequest request) {
    Map<String, Object> bankInfo = new HashMap<>();
    bankInfo.put("vbank_code", request.getBankCode());
    bankInfo.put("vbank_due", request.getDueDate().toString());
    bankInfo.put("pg", "tosspayments");

    return VirtualAccountDto.builder().orderId(request.getOrderId()).bankInfo(bankInfo).build();
  }

  /** Service PaymentResultDto → API PaymentPrepareResponse 변환 */
  public PaymentPrepareResponse toApiPaymentPrepareResponse(PaymentResultDto resultDto) {
    return PaymentPrepareResponse.builder()
        .paymentKey(resultDto.getPaymentKey())
        .merchantUid(resultDto.getMerchantUid())
        .amount(resultDto.getAmount())
        .status(resultDto.getStatus())
        .pgProvider("tosspayments") // 또는 결과에서 가져오기
        .clientKey(resultDto.getClientKey())
        .build();
  }

  /** Service PaymentResultDto → API PaymentResponse 변환 */
  public PaymentResponse toApiPaymentResponse(PaymentResultDto resultDto) {
    return PaymentResponse.builder()
        .paymentId(resultDto.getId())
        .paymentKey(resultDto.getPaymentKey())
        .merchantUid(resultDto.getMerchantUid())
        .amount(resultDto.getAmount())
        .status(resultDto.getStatus())
        .paymentMethod(resultDto.getPaymentMethod())
        .customerName(resultDto.getCustomerName())
        .customerEmail(resultDto.getCustomerEmail())
        .customerPhone(resultDto.getCustomerPhone())
        .receiptUrl(resultDto.getReceiptUrl())
        .isEscrow(resultDto.getIsEscrow())
        .isCashReceipt(resultDto.getIsCashReceipt())
        .cardNumber(resultDto.getCardNumber())
        .cardIssuerName(resultDto.getCardIssuerName())
        .cardAcquirerName(resultDto.getCardAcquirerName())
        .cardInstallmentPlanMonths(resultDto.getCardInstallmentPlanMonths())
        .virtualAccountNumber(resultDto.getVbankNumber())
        .virtualAccountBankName(resultDto.getVbankBankName())
        .virtualAccountExpiryDate(resultDto.getVbankExpiryDate())
        .cancelReason(resultDto.getCancelReason())
        .cancelAmount(resultDto.getCancelAmount())
        .cancelledAt(resultDto.getCanceledAt())
        .pgProvider(resultDto.getPgProvider())
        .clientKey(resultDto.getClientKey())
        .build();
  }
}
