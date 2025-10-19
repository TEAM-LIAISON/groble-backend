package liaison.groble.api.server.payment;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import liaison.groble.application.payment.exception.PaypleMobileRedirectException;
import liaison.groble.application.payment.service.PaypleMobileRedirectService;
import liaison.groble.application.payment.service.PaypleMobileRedirectService.MobileRedirectContext;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiPaths.Payment.PAYPLE_BASE)
@Tag(name = PaymentSwaggerDocs.TAG_NAME, description = PaymentSwaggerDocs.TAG_DESCRIPTION)
public class PayplePaymentController extends BaseController {
  // Factory
  private final PaymentProcessorFactory processorFactory;

  // Mapper
  private final PaymentMapper paymentMapper;

  // Service
  private final PaypleMobileRedirectService paypleMobileRedirectService;
  private final ObjectMapper objectMapper;

  public PayplePaymentController(
      ResponseHelper responseHelper,
      PaymentProcessorFactory processorFactory,
      PaymentMapper paymentMapper,
      PaypleMobileRedirectService paypleMobileRedirectService,
      ObjectMapper objectMapper) {
    super(responseHelper);
    this.processorFactory = processorFactory;
    this.paymentMapper = paymentMapper;
    this.paypleMobileRedirectService = paypleMobileRedirectService;
    this.objectMapper = objectMapper;
  }

  @Operation(
      summary = PaymentSwaggerDocs.PAYMENT_APP_CARD_SUMMARY,
      description = PaymentSwaggerDocs.PAYMENT_APP_CARD_DESCRIPTION)
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

  @Operation(
      summary = "모바일 Payple 결제 리다이렉트",
      description = "모바일 환경에서 Payple 결제 완료 후 프론트엔드 결제 완료 페이지로 리다이렉트합니다.")
  @Logging(item = "Payment", action = "MobileRedirect", includeParam = true, includeResult = false)
  @RequestMapping(
      value = ApiPaths.Payment.MOBILE_REDIRECT,
      method = {RequestMethod.GET, RequestMethod.POST})
  public void handleMobileRedirect(
      @RequestParam(value = "PCD_PAY_OID", required = true) String merchantUid,
      @RequestParam(value = "PCD_PAY_RST", required = false, defaultValue = "error")
          String payResult,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {

    boolean isSuccess = paypleMobileRedirectService.isPaymentSuccess(payResult);
    log.info(
        "🔄 모바일 결제 리다이렉트 요청 - method: {}, merchantUid: {}, payResult: {}, success: {}",
        request.getMethod(),
        merchantUid,
        payResult,
        isSuccess);

    if (!isSuccess) {
      log.warn("모바일 결제 결과가 실패로 전달되었습니다 - merchantUid: {}", merchantUid);
      String failureRedirect =
          paypleMobileRedirectService.buildFailureRedirectUrl(
              merchantUid, PaypleMobileRedirectException.defaultClientMessage());
      response.sendRedirect(failureRedirect);
      return;
    }

    PaypleAuthResultDTO authResultDTO = null;
    if ("POST".equalsIgnoreCase(request.getMethod())) {
      authResultDTO = extractAuthResultDTO(request, merchantUid, payResult);
      if (authResultDTO != null) {
        log.debug("모바일 결제 인증 파라미터 파싱 완료 - merchantUid: {}", authResultDTO.getPayOid());
      } else {
        log.warn("모바일 결제 인증 파라미터 파싱에 실패했습니다 - merchantUid: {}", merchantUid);
      }
    }

    try {
      MobileRedirectContext context = paypleMobileRedirectService.loadContext(merchantUid);
      boolean paymentProcessed = paypleMobileRedirectService.isAlreadyProcessed(context);

      if (!paymentProcessed && !paypleMobileRedirectService.isProcessableStatus(context)) {
        log.warn("결제 처리가 불가능한 주문 상태입니다 - merchantUid: {}", merchantUid);
      }

      if (!paymentProcessed
          && paypleMobileRedirectService.isProcessableStatus(context)
          && isProcessablePayload(authResultDTO)) {
        try {
          UserContext userContext = paypleMobileRedirectService.buildUserContext(context);
          processorFactory.getProcessor(userContext).processPayment(userContext, authResultDTO);
          paymentProcessed = true;
          log.info("✅ 모바일 결제 승인 처리 완료 - merchantUid: {}", merchantUid);
        } catch (Exception approvalException) {
          log.error(
              "❌ 모바일 결제 승인 처리 실패 - merchantUid: {}, error: {}",
              merchantUid,
              approvalException.getMessage(),
              approvalException);
          String errorRedirectUrl =
              paypleMobileRedirectService.buildFailureRedirectUrl(
                  merchantUid, PaypleMobileRedirectException.defaultClientMessage());
          response.sendRedirect(errorRedirectUrl);
          return;
        }
      }

      if (!paymentProcessed) {
        log.warn(
            "모바일 결제 승인을 처리하지 못했습니다 - merchantUid: {}, processable: {}, payloadValid: {}",
            merchantUid,
            paypleMobileRedirectService.isProcessableStatus(context),
            isProcessablePayload(authResultDTO));
        String errorRedirectUrl =
            paypleMobileRedirectService.buildFailureRedirectUrl(
                merchantUid, PaypleMobileRedirectException.defaultClientMessage());
        response.sendRedirect(errorRedirectUrl);
        return;
      }

      String redirectUrl = paypleMobileRedirectService.buildSuccessRedirectUrl(context, true);
      log.info("✅ 모바일 결제 리다이렉트 성공 - redirectUrl: {}", redirectUrl);
      response.sendRedirect(redirectUrl);
    } catch (PaypleMobileRedirectException e) {
      log.error("❌ 모바일 결제 리다이렉트 실패 - merchantUid: {}, error: {}", merchantUid, e.getMessage(), e);
      String errorRedirectUrl =
          paypleMobileRedirectService.buildFailureRedirectUrl(merchantUid, e.getClientMessage());
      response.sendRedirect(errorRedirectUrl);
    } catch (Exception e) {
      log.error("❌ 모바일 결제 리다이렉트 처리 중 알 수 없는 오류 - merchantUid: {}", merchantUid, e);
      String errorRedirectUrl =
          paypleMobileRedirectService.buildFailureRedirectUrl(
              merchantUid, PaypleMobileRedirectException.defaultClientMessage());
      response.sendRedirect(errorRedirectUrl);
    }
  }

  private boolean isProcessablePayload(PaypleAuthResultDTO dto) {
    return dto != null
        && dto.isSuccess()
        && StringUtils.hasText(dto.getAuthKey())
        && StringUtils.hasText(dto.getPayReqKey())
        && StringUtils.hasText(dto.getPayCofUrl())
        && StringUtils.hasText(dto.getPayTotal());
  }

  private PaypleAuthResultDTO extractAuthResultDTO(
      HttpServletRequest request, String merchantUid, String fallbackPayResult) {

    Map<String, String[]> params = request.getParameterMap();
    if (params != null && !params.isEmpty()) {
      return buildAuthResultDTO(key -> firstParam(params, key), merchantUid, fallbackPayResult);
    }

    String contentType = request.getContentType();
    if (contentType != null && contentType.contains("application/json")) {
      try {
        JsonNode node = objectMapper.readTree(request.getInputStream());
        if (node != null && node.isObject()) {
          return buildAuthResultDTO(key -> getJsonValue(node, key), merchantUid, fallbackPayResult);
        }
      } catch (IOException e) {
        log.warn("모바일 결제 인증 JSON 파싱 실패 - merchantUid: {}", merchantUid, e);
      }
    }

    return null;
  }

  private PaypleAuthResultDTO buildAuthResultDTO(
      Function<String, String> source, String merchantUid, String fallbackPayResult) {

    String payRst = defaultValue(source.apply("PCD_PAY_RST"), fallbackPayResult);
    String payOid = defaultValue(source.apply("PCD_PAY_OID"), merchantUid);
    String payCardReceipt =
        defaultValue(source.apply("PCD_PAY_CARDRECEIPT"), source.apply("PCD_CARD_RECEIPT"));

    return PaypleAuthResultDTO.builder()
        .payRst(payRst)
        .pcdPayMethod(source.apply("PCD_PAY_METHOD"))
        .payCode(source.apply("PCD_PAY_CODE"))
        .payMsg(source.apply("PCD_PAY_MSG"))
        .payType(source.apply("PCD_PAY_TYPE"))
        .cardVer(source.apply("PCD_CARD_VER"))
        .payWork(source.apply("PCD_PAY_WORK"))
        .authKey(source.apply("PCD_AUTH_KEY"))
        .payReqKey(source.apply("PCD_PAY_REQKEY"))
        .payReqTime(source.apply("PCD_PAY_REQ_TIME"))
        .payHost(source.apply("PCD_PAY_HOST"))
        .payCofUrl(source.apply("PCD_PAY_COFURL"))
        .payDiscount(source.apply("PCD_PAY_DISCOUNT"))
        .payEasyPayMethod(source.apply("PCD_PAY_EASY_PAY_METHOD"))
        .easyPayMethod(source.apply("PCD_EASY_PAY_METHOD"))
        .payerNo(source.apply("PCD_PAYER_NO"))
        .payAmount(source.apply("PCD_PAY_AMOUNT"))
        .payAmountReal(source.apply("PCD_PAY_AMOUNT_REAL"))
        .payerName(source.apply("PCD_PAYER_NAME"))
        .payerHp(source.apply("PCD_PAYER_HP"))
        .payerId(source.apply("PCD_PAYER_ID"))
        .payerEmail(source.apply("PCD_PAYER_EMAIL"))
        .payOid(payOid)
        .payGoods(source.apply("PCD_PAY_GOODS"))
        .payTotal(source.apply("PCD_PAY_TOTAL"))
        .payTaxTotal(source.apply("PCD_PAY_TAXTOTAL"))
        .payIsTax(source.apply("PCD_PAY_ISTAX"))
        .payCardName(source.apply("PCD_PAY_CARDNAME"))
        .payCardNum(source.apply("PCD_PAY_CARDNUM"))
        .payCardQuota(source.apply("PCD_PAY_CARDQUOTA"))
        .payCardTradeNum(source.apply("PCD_PAY_CARDTRADENUM"))
        .payCardAuthNo(source.apply("PCD_PAY_CARDAUTHNO"))
        .payCardReceipt(payCardReceipt)
        .payTime(source.apply("PCD_PAY_TIME"))
        .regulerFlag(source.apply("PCD_REGULER_FLAG"))
        .payYear(source.apply("PCD_PAY_YEAR"))
        .payMonth(source.apply("PCD_PAY_MONTH"))
        .simpleFlag(source.apply("PCD_SIMPLE_FLAG"))
        .rstUrl(source.apply("PCD_RST_URL"))
        .userDefine1(source.apply("PCD_USER_DEFINE1"))
        .userDefine2(source.apply("PCD_USER_DEFINE2"))
        .pcdPayUrl(source.apply("PCD_PAY_URL"))
        .build();
  }

  private String firstParam(Map<String, String[]> params, String key) {
    String[] values = params.get(key);
    return (values != null && values.length > 0) ? values[0] : null;
  }

  private String getJsonValue(JsonNode node, String key) {
    JsonNode valueNode = node.get(key);
    return valueNode != null && !valueNode.isNull() ? valueNode.asText() : null;
  }

  private String defaultValue(String value, String fallback) {
    return StringUtils.hasText(value) ? value : fallback;
  }
}
