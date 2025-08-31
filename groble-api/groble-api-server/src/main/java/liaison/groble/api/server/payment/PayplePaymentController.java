package liaison.groble.api.server.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.payment.docs.PaymentApiResponses;
import liaison.groble.api.server.payment.docs.PaymentSwaggerDocs;
import liaison.groble.api.server.payment.processor.PaymentProcessorFactory;
import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.context.UserContext;
import liaison.groble.common.factory.UserContextFactory;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.payment.PaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Validated
@RestController
@RequestMapping(ApiPaths.Payment.PAYPLE_BASE)
@Tag(name = PaymentSwaggerDocs.TAG_NAME, description = PaymentSwaggerDocs.TAG_DESCRIPTION)
public class PayplePaymentController extends BaseController {
  // Factory
  private final PaymentProcessorFactory processorFactory;

  // Mapper
  private final PaymentMapper paymentMapper;

  public PayplePaymentController(
      ResponseHelper responseHelper,
      PaymentProcessorFactory processorFactory,
      PaymentMapper paymentMapper) {
    super(responseHelper);
    this.processorFactory = processorFactory;
    this.paymentMapper = paymentMapper;
  }

  @Operation(
      summary = PaymentSwaggerDocs.PAYMENT_SUMMARY,
      description = PaymentSwaggerDocs.PAYMENT_DESCRIPTION)
  @PaymentApiResponses.PaymentRequestResponses
  @Logging(
      item = "Payment",
      action = "requestAppCardPayment",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Payment.APP_CARD_REQUEST)
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> requestAppCardPayment(
      @Auth(required = false) Accessor accessor,
      @Valid @RequestBody PaypleAuthResultRequest request) {

    PaypleAuthResultDTO authResultDTO = paymentMapper.toPaypleAuthResultDTO(request);
    UserContext userContext = UserContextFactory.from(accessor);
    AppCardPayplePaymentDTO responseDTO =
        processorFactory.getProcessor(userContext).processPayment(userContext, authResultDTO);

    AppCardPayplePaymentResponse response =
        paymentMapper.toAppCardPayplePaymentResponse(responseDTO);

    return success(response, ResponseMessages.Payment.REQUEST_SUCCESS);
  }

  @Operation(
      summary = PaymentSwaggerDocs.CANCEL_SUMMARY,
      description = PaymentSwaggerDocs.CANCEL_DESCRIPTION)
  @PaymentApiResponses.PaymentCancelResponses
  @Logging(item = "Payment", action = "CancelPayment", includeParam = true, includeResult = true)
  @PostMapping(ApiPaths.Payment.CANCEL)
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

    UserContext userContext = UserContextFactory.from(accessor);
    PaymentCancelResponse response =
        processorFactory
            .getProcessor(userContext)
            .cancelPayment(userContext, merchantUid, request.getDetailReason());

    return success(response, ResponseMessages.Payment.CANCEL_SUCCESS);
  }
}
