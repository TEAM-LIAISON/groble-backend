package liaison.groble.api.server.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.server.payment.docs.PaymentApiResponses;
import liaison.groble.api.server.payment.docs.PaymentSwaggerDocs;
import liaison.groble.api.server.payment.processor.PaymentProcessorFactory;
import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.payment.PaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/payments/payple")
@RequiredArgsConstructor
@Tag(name = PaymentSwaggerDocs.TAG_NAME, description = PaymentSwaggerDocs.TAG_DESCRIPTION)
public class PayplePaymentController {
  // Factory
  private final PaymentProcessorFactory processorFactory;

  // Mapper
  private final PaymentMapper paymentMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = PaymentSwaggerDocs.PAYMENT_SUMMARY,
      description = PaymentSwaggerDocs.PAYMENT_DESCRIPTION)
  @PaymentApiResponses.PaymentRequestResponses
  @Logging(
      item = "Payment",
      action = "requestAppCardPayment",
      includeParam = true,
      includeResult = true)
  @PostMapping("/app-card/request")
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> requestAppCardPayment(
      @Auth(required = false) Accessor accessor,
      @Valid @RequestBody PaypleAuthResultRequest request) {

    PaypleAuthResultDTO authResultDTO = paymentMapper.toPaypleAuthResultDTO(request);
    AppCardPayplePaymentResponse response =
        processorFactory.getProcessor(accessor).processPayment(accessor, authResultDTO);

    return responseHelper.success(response, "결제 승인 요청이 성공적으로 처리되었습니다.", HttpStatus.OK);
  }

  @Operation(
      summary = PaymentSwaggerDocs.CANCEL_SUMMARY,
      description = PaymentSwaggerDocs.CANCEL_DESCRIPTION)
  @PaymentApiResponses.PaymentCancelResponses
  @Logging(item = "Payment", action = "CancelPayment", includeParam = true, includeResult = true)
  @PostMapping("/{merchantUid}/cancel")
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @Auth(required = false) Accessor accessor,
      @Parameter(
              description = PaymentSwaggerDocs.MERCHANT_UID_DESC,
              required = true,
              example = PaymentSwaggerDocs.MERCHANT_UID_EXAMPLE)
          @PathVariable
          @NotBlank
          String merchantUid,
      @Valid @RequestBody PaymentCancelRequest request) {

    PaymentCancelResponse response =
        processorFactory
            .getProcessor(accessor)
            .cancelPayment(accessor, merchantUid, request.getDetailReason());

    return responseHelper.success(response, "결제 취소 요청이 성공적으로 처리되었습니다.", HttpStatus.OK);
  }
}
