package liaison.groble.api.server.payment;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentApproveRequest;
import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaymentPrepareRequest;
import liaison.groble.api.model.payment.request.VirtualAccountRequest;
import liaison.groble.api.model.payment.response.PaymentPrepareResponse;
import liaison.groble.api.model.payment.response.PaymentResponse;
import liaison.groble.api.server.payment.mapper.PaymentDtoMapper;
import liaison.groble.application.payment.dto.PaymentApproveDto;
import liaison.groble.application.payment.dto.PaymentCancelDto;
import liaison.groble.application.payment.dto.PaymentPrepareDto;
import liaison.groble.application.payment.dto.PaymentResultDto;
import liaison.groble.application.payment.dto.VirtualAccountDto;
import liaison.groble.application.payment.service.PortOnePaymentService;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PortOnePaymentService paymentService;
  private final PaymentDtoMapper paymentDtoMapper;

  /** 결제 준비 API 클라이언트에서 결제 요청 시 호출하는 API */
  @PostMapping("/prepare")
  public ResponseEntity<GrobleResponse<PaymentPrepareResponse>> preparePayment(
      @Valid @RequestBody PaymentPrepareRequest request) {

    log.info(
        "Preparing payment for order: {}, method: {}",
        request.getOrderId(),
        request.getPaymentMethod());

    // 1. API DTO → 서비스 DTO 변환
    PaymentPrepareDto prepareDto = paymentDtoMapper.toServicePaymentPrepareDto(request);

    // 2. 서비스 호출
    PaymentResultDto resultDto = paymentService.preparePayment(prepareDto);

    // 3. 서비스 DTO → API DTO 변환
    PaymentPrepareResponse response = paymentDtoMapper.toApiPaymentPrepareResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(response, "결제 준비가 완료되었습니다.", HttpStatus.OK.value()));
  }

  /** 결제 승인 API 결제 완료 후 클라이언트에서 호출하는 API */
  @PostMapping("/approve")
  public ResponseEntity<GrobleResponse<PaymentResponse>> approvePayment(
      @Valid @RequestBody PaymentApproveRequest request) {

    log.info(
        "Approving payment: key={}, orderId={}, amount={}",
        request.getPaymentKey(),
        request.getMerchantUid(),
        request.getAmount());

    // 1. API DTO → 서비스 DTO 변환
    PaymentApproveDto approveDto = paymentDtoMapper.toServicePaymentApproveDto(request);

    // 2. 서비스 호출
    PaymentResultDto resultDto = paymentService.approvePayment(approveDto);

    // 3. 서비스 DTO → API DTO 변환
    PaymentResponse response = paymentDtoMapper.toApiPaymentResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(response, "결제가 승인되었습니다.", HttpStatus.OK.value()));
  }

  /** 결제 취소 API */
  @PostMapping("/cancel")
  public ResponseEntity<GrobleResponse<PaymentResponse>> cancelPayment(
      @Valid @RequestBody PaymentCancelRequest request) {

    log.info(
        "Cancelling payment: key={}, amount={}, reason={}",
        request.getPaymentKey(),
        request.getAmount(),
        request.getReason());

    // 1. API DTO → 서비스 DTO 변환
    PaymentCancelDto cancelDto = paymentDtoMapper.toServicePaymentCancelDto(request);

    // 2. 서비스 호출
    PaymentResultDto resultDto = paymentService.cancelPayment(cancelDto);

    // 3. 서비스 DTO → API DTO 변환
    PaymentResponse response = paymentDtoMapper.toApiPaymentResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(response, "결제가 취소되었습니다.", HttpStatus.OK.value()));
  }

  /** 결제 정보 조회 API */
  @GetMapping("/{paymentKey}")
  public ResponseEntity<GrobleResponse<PaymentResponse>> getPaymentDetails(
      @PathVariable String paymentKey) {

    log.info("Getting payment details: key={}", paymentKey);

    // 1. 서비스 호출
    PaymentResultDto resultDto = paymentService.getPaymentDetails(paymentKey);

    // 2. 서비스 DTO → API DTO 변환
    PaymentResponse response = paymentDtoMapper.toApiPaymentResponse(resultDto);

    // 3. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(response, "결제 정보가 조회되었습니다.", HttpStatus.OK.value()));
  }

  /** 가상계좌 발급 API */
  @PostMapping("/virtual-account")
  public ResponseEntity<GrobleResponse<PaymentResponse>> issueVirtualAccount(
      @Valid @RequestBody VirtualAccountRequest request) {

    log.info("Issuing virtual account for order: {}", request.getOrderId());

    // 1. API DTO → 서비스 DTO 변환
    VirtualAccountDto virtualAccountDto = paymentDtoMapper.toServiceVirtualAccountDto(request);

    // 2. 서비스 호출
    PaymentResultDto resultDto = paymentService.issueVirtualAccount(virtualAccountDto);

    // 3. 서비스 DTO → API DTO 변환
    PaymentResponse response = paymentDtoMapper.toApiPaymentResponse(resultDto);

    // 4. API 응답 생성
    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success(response, "가상계좌가 발급되었습니다.", HttpStatus.OK.value()));
  }

  /** 웹훅 처리 API */
  @PostMapping("/webhook")
  public ResponseEntity<Map<String, String>> handleWebhook(
      @RequestBody Map<String, Object> webhookData,
      @RequestHeader(value = "X-Portone-Signature", required = false) String signature) {

    log.info("Received payment webhook");

    // 서명 검증 (선택 사항)
    if (signature != null) {
      // signature 검증 로직
      // portOneApiClient.verifyWebhookSignature(...)
    }

    // 웹훅 처리
    paymentService.handleWebhook(webhookData);

    // 성공 응답
    Map<String, String> response = new HashMap<>();
    response.put("status", "success");
    return ResponseEntity.ok(response);
  }
}
