package liaison.groble.api.server.payment;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaymentRequest;
import liaison.groble.api.model.payment.response.PaymentCancelResponse;
import liaison.groble.api.model.payment.response.PaymentCompleteResponse;
import liaison.groble.api.model.payment.response.PaymentInfo;
import liaison.groble.api.model.payment.response.PaymentRequestResponse;
import liaison.groble.api.model.payment.response.PaypleAuthResponse;
import liaison.groble.api.server.payment.mapper.PayplePaymentMapper;
import liaison.groble.application.payment.dto.PaymentCancelResponseDto;
import liaison.groble.application.payment.dto.PaymentCompleteResponseDto;
import liaison.groble.application.payment.dto.PaymentInfoDto;
import liaison.groble.application.payment.dto.PaymentRequestDto;
import liaison.groble.application.payment.dto.PaymentRequestResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PayplePaymentResult;
import liaison.groble.application.payment.dto.PayplePaymentResultDto;
import liaison.groble.application.payment.service.PayplePaymentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.ApiResponse;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/payple")
@RequiredArgsConstructor
public class PayplePaymentController {

  private final PayplePaymentService payplePaymentService;
  private final PayplePaymentMapper payplePaymentMapper;

  /** 결제창 호출을 위한 인증 정보 조회 */
  @GetMapping("/auth")
  public ResponseEntity<GrobleResponse<PaypleAuthResponse>> getPaymentAuth(
      @RequestParam(required = false) String payWork) {

    PaypleAuthResponseDto paypleAuthResponseDto = payplePaymentService.getPaymentAuth(payWork);
    PaypleAuthResponse authResponse =
        payplePaymentMapper.toPaypleAuthResponse(paypleAuthResponseDto);

    return ResponseEntity.ok(GrobleResponse.success(authResponse));
  }

  /** 결제 요청 생성 */
  @PostMapping("/request")
  public ResponseEntity<GrobleResponse<PaymentRequestResponse>> createPaymentRequest(
      @Auth Accessor accessor, @RequestBody PaymentRequest paymentRequest) {

    PaymentRequestDto paymentRequestDto = payplePaymentMapper.toPaymentRequestDto(paymentRequest);

    PaymentRequestResponseDto paymentRequestResponseDto =
        payplePaymentService.processPayment(accessor.getUserId(), paymentRequestDto);
    PaymentRequestResponse paymentRequestResponse =
        payplePaymentMapper.toPaymentRequestResponse(paymentRequestResponseDto);

    return ResponseEntity.ok(GrobleResponse.success(paymentRequestResponse));
  }

  /** 결제 완료 처리 (결제창에서 리턴) */
  @PostMapping("/complete")
  public ResponseEntity<ApiResponse<PaymentCompleteResponse>> completePayment(
      HttpServletRequest request) {

    // Payple 결제창에서 전달받은 파라미터 매핑
    PayplePaymentResult result =
        PayplePaymentResult.builder()
            .payRst(request.getParameter("PCD_PAY_RST"))
            .payMsg(request.getParameter("PCD_PAY_MSG"))
            .payOid(request.getParameter("PCD_PAY_OID"))
            .payerId(request.getParameter("PCD_PAYER_ID"))
            .payTime(request.getParameter("PCD_PAY_TIME"))
            .payCardName(request.getParameter("PCD_PAY_CARDNAME"))
            .payCardNum(request.getParameter("PCD_PAY_CARDNUM"))
            .payBankName(request.getParameter("PCD_PAY_BANKNAME"))
            .payBankNum(request.getParameter("PCD_PAY_BANKNUM"))
            .build();

    PayplePaymentResultDto payplePaymentResultDto = payplePaymentMapper.toPaymentResultDto(result);

    PaymentCompleteResponseDto paymentCompleteResponseDto =
        payplePaymentService.completePayment(payplePaymentResultDto);
    PaymentCompleteResponse response =
        payplePaymentMapper.toPaymentCompleteResponse(paymentCompleteResponseDto);

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /** 결제 취소 */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @PathVariable String orderId, @RequestBody PaymentCancelRequest request) {

    PaymentCancelResponseDto paymentCancelResponseDto =
        payplePaymentService.cancelPayment(orderId, request.getReason());
    PaymentCancelResponse paymentCancelResponse =
        payplePaymentMapper.toPaymentCancelResponse(paymentCancelResponseDto);
    return ResponseEntity.ok(GrobleResponse.success(paymentCancelResponse));
  }

  //    /**
  //     * 정기결제 실행 (빌링키 결제)
  //     */
  //    @PostMapping("/billing")
  //    public ResponseEntity<ApiResponse<BillingPaymentResponseDto>> processBillingPayment(
  //            @Auth Accessor accessor,
  //            @RequestBody BillingPaymentRequestDto request) {
  //
  //        var billingRequest = request.toServiceDto(accessor.getUserId());
  //        PayplePayment payment = payplePaymentService.processBillingPayment(billingRequest);
  //
  //        return ResponseEntity.ok(ApiResponse.success(
  //            BillingPaymentResponseDto.from(payment)
  //        ));
  //    }

  /** 결제 정보 조회 */
  @GetMapping("/{orderId}")
  public ResponseEntity<GrobleResponse<PaymentInfo>> getPaymentInfo(@PathVariable String orderId) {

    PaymentInfoDto paymentInfoDto = payplePaymentService.getPaymentInfo(orderId);
    PaymentInfo paymentInfo = payplePaymentMapper.toPaymentInfo(paymentInfoDto);

    return ResponseEntity.ok(GrobleResponse.success(paymentInfo));
  }

  /** 빌링키 해지 */
  @DeleteMapping("/billing/{billingKey}")
  public ResponseEntity<GrobleResponse<Void>> deleteBillingKey(
      @Auth Accessor accessor, @PathVariable String billingKey) {

    payplePaymentService.deleteBillingKey(accessor.getUserId(), billingKey);

    return ResponseEntity.ok(GrobleResponse.success());
  }
}
