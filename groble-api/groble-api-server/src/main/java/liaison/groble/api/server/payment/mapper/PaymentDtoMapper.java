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

  /** API PaymentPrepareRequest → Service PaymentPrepareDto 변환 (V2 버전) */
  public PaymentPrepareDto toServicePaymentPrepareDto(PaymentPrepareRequest request) {
    // V2 API에 맞게 추가 데이터 구성
    Map<String, Object> additionalData = new HashMap<>();

    // 기본 추가 데이터 복사
    if (request.getAdditionalOptions() != null) {
      additionalData.putAll(request.getAdditionalOptions());
    }

    // PG 사업자 설정
    if (request.getPgProvider() != null) {
      additionalData.put("pgProvider", request.getPgProvider());
    }

    // 주문명 설정
    if (request.getOrderName() != null) {
      additionalData.put("orderName", request.getOrderName());
    }

    // 결제 금액 설정 (있다면)
    if (request.getAmount() != null) {
      additionalData.put("amount", request.getAmount());
    }

    // 카드 옵션 설정
    if (request.getCardOptions() != null) {
      additionalData.put("cardOptions", request.getCardOptions());
    }

    // 가상계좌 옵션 설정
    if (request.getVirtualAccountOptions() != null) {
      additionalData.put("virtualAccountOptions", request.getVirtualAccountOptions());
    }

    // DTO 생성 및 반환
    return PaymentPrepareDto.builder()
        .orderId(request.getOrderId())
        .paymentMethod(request.getPaymentMethod())
        .customerName(request.getCustomerName())
        .customerEmail(request.getCustomerEmail())
        .customerPhone(request.getCustomerPhone())
        .successUrl(request.getSuccessUrl())
        .failUrl(request.getFailUrl())
        .additionalData(additionalData)
        .totalAmount(100L)
        .taxFreeAmount(50L)
        .build();
  }

  /** API PaymentApproveRequest → Service PaymentApproveDto 변환 (V2 버전) */
  public PaymentApproveDto toServicePaymentApproveDto(PaymentApproveRequest request) {
    return PaymentApproveDto.builder()
        .paymentKey(request.getPaymentKey())
        .orderId(request.getMerchantUid()) // V2에서는 orderId로 명명되지만 호환성 유지
        .amount(request.getAmount())
        .build();
  }

  /** API PaymentCancelRequest → Service PaymentCancelDto 변환 (V2 버전) */
  public PaymentCancelDto toServicePaymentCancelDto(PaymentCancelRequest request) {
    return PaymentCancelDto.builder()
        .paymentKey(request.getPaymentKey())
        .amount(request.getAmount())
        .reason(request.getReason())
        .build();
  }

  /** API VirtualAccountRequest → Service VirtualAccountDto 변환 (V2 버전) */
  public VirtualAccountDto toServiceVirtualAccountDto(VirtualAccountRequest request) {
    Map<String, Object> bankInfo = new HashMap<>();

    // V2 API에 맞게 필드명 변경
    bankInfo.put("bankCode", request.getBankCode());
    bankInfo.put("dueDate", request.getDueDate().toString());
    bankInfo.put("pgProvider", "tosspayments");

    // 만료 시간 계산 (옵션)
    // 만료일로부터 현재까지의 시간을 계산하여 validHours로 설정 가능

    return VirtualAccountDto.builder().orderId(request.getOrderId()).bankInfo(bankInfo).build();
  }

  /** Service PaymentResultDto → API PaymentPrepareResponse 변환 (V2 버전) */
  public PaymentPrepareResponse toApiPaymentPrepareResponse(PaymentResultDto resultDto) {
    return PaymentPrepareResponse.builder()
        .paymentKey(resultDto.getPaymentKey())
        .merchantUid(resultDto.getMerchantUid())
        .amount(resultDto.getAmount())
        .status(resultDto.getStatus()) // V2에서는 상태가 대문자로 반환됨 (READY, DONE 등)
        .pgProvider(resultDto.getPgProvider() != null ? resultDto.getPgProvider() : "tosspayments")
        .clientKey(resultDto.getClientKey())
        .build();
  }

  /** Service PaymentResultDto → API PaymentResponse 변환 (V2 버전) */
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
