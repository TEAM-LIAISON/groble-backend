package liaison.groble.api.server.payment;

// groble-api/groble-api-server/src/main/java/liaison/groble/api/server/payment/PortOnePaymentController.java

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentApproveRequest;
import liaison.groble.api.model.payment.request.PaymentPrepareRequest;
import liaison.groble.api.model.payment.response.PaymentPrepareResponse;
import liaison.groble.api.model.payment.response.PaymentResponse;
import liaison.groble.api.server.payment.mapper.PaymentDtoMapper;
import liaison.groble.application.payment.dto.PaymentApproveDto;
import liaison.groble.application.payment.dto.PaymentPrepareDto;
import liaison.groble.application.payment.dto.PaymentResultDto;
import liaison.groble.application.payment.service.PortOnePaymentService;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PortOnePaymentController {

  private final PortOnePaymentService paymentService;
  private final PaymentDtoMapper paymentDtoMapper;

  /** 클라이언트에서 사용할 결제 초기화 정보 제공 */
  @GetMapping("/client-key")
  public ResponseEntity<GrobleResponse<Map<String, String>>> getClientKey() {
    Map<String, String> response = paymentService.getClientKey();
    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  /** 결제 준비 API */
  @PostMapping("/prepare")
  public ResponseEntity<GrobleResponse<PaymentPrepareResponse>> preparePayment(
      @Valid @RequestBody PaymentPrepareRequest request) {
    log.info(
        "Preparing payment for order: {}, method: {}",
        request.getOrderId(),
        request.getPaymentMethod());

    PaymentPrepareDto prepareDto = paymentDtoMapper.toServicePaymentPrepareDto(request);
    PaymentResultDto resultDto = paymentService.preparePayment(prepareDto);
    PaymentPrepareResponse response = paymentDtoMapper.toApiPaymentPrepareResponse(resultDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "결제 준비가 완료되었습니다."));
  }

  /** 결제 승인 API */
  @PostMapping("/approve")
  public ResponseEntity<GrobleResponse<PaymentResponse>> approvePayment(
      @Valid @RequestBody PaymentApproveRequest request) {
    log.info(
        "Approving payment: key={}, orderId={}, amount={}",
        request.getPaymentKey(),
        request.getMerchantUid(),
        request.getAmount());

    PaymentApproveDto approveDto = paymentDtoMapper.toServicePaymentApproveDto(request);
    PaymentResultDto resultDto = paymentService.approvePayment(approveDto);
    PaymentResponse response = paymentDtoMapper.toApiPaymentResponse(resultDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "결제가 승인되었습니다."));
  }

  // 다른 필요한 엔드포인트 (취소, 조회 등)
}
