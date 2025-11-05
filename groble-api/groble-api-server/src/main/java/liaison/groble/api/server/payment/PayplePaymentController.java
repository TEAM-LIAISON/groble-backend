package liaison.groble.api.server.payment;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import liaison.groble.api.model.payment.request.PaypleBillingChargeRequest;
import liaison.groble.api.model.payment.request.PaypleBillingRegistrationRequest;
import liaison.groble.api.model.payment.response.AppCardPayplePaymentResponse;
import liaison.groble.api.model.payment.response.BillingKeyResponse;
import liaison.groble.api.model.payment.response.PaypleBillingAuthResponse;
import liaison.groble.api.model.payment.response.PaypleSubscriptionResultResponse;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.payment.docs.PaymentApiResponses;
import liaison.groble.api.server.payment.docs.PaymentSwaggerDocs;
import liaison.groble.api.server.payment.processor.PaymentProcessorFactory;
import liaison.groble.application.payment.dto.AppCardPayplePaymentDTO;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.billing.BillingKeyAction;
import liaison.groble.application.payment.dto.billing.RegisterBillingKeyCommand;
import liaison.groble.application.payment.dto.billing.SubscriptionPaymentResult;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.exception.PaymentAuthenticationRequiredException;
import liaison.groble.application.payment.exception.PaypleMobileRedirectException;
import liaison.groble.application.payment.service.BillingKeyService;
import liaison.groble.application.payment.service.PaypleBillingAuthService;
import liaison.groble.application.payment.service.PaypleMobileRedirectService;
import liaison.groble.application.payment.service.PaypleMobileRedirectService.MobileRedirectContext;
import liaison.groble.application.payment.service.SubscriptionPaymentService;
import liaison.groble.application.subscription.service.SubscriptionService;
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
  private final PaypleBillingAuthService paypleBillingAuthService;
  private final PaypleMobileRedirectService paypleMobileRedirectService;
  private final BillingKeyService billingKeyService;
  private final SubscriptionPaymentService subscriptionPaymentService;
  private final SubscriptionService subscriptionService;
  private final ObjectMapper objectMapper;

  private static final String BILLING_CHARGE_SUCCESS_MESSAGE = "빌링키 결제가 완료되었습니다.";
  private static final String BILLING_KEY_CONFIRM_SUCCESS_MESSAGE = "빌링키 등록이 완료되었습니다.";
  private static final String SUBSCRIPTION_CONFIRM_SUCCESS_MESSAGE = "정기 결제가 완료되었습니다.";

  public PayplePaymentController(
      ResponseHelper responseHelper,
      PaymentProcessorFactory processorFactory,
      PaymentMapper paymentMapper,
      PaypleBillingAuthService paypleBillingAuthService,
      PaypleMobileRedirectService paypleMobileRedirectService,
      BillingKeyService billingKeyService,
      SubscriptionPaymentService subscriptionPaymentService,
      SubscriptionService subscriptionService,
      ObjectMapper objectMapper) {
    super(responseHelper);
    this.processorFactory = processorFactory;
    this.paymentMapper = paymentMapper;
    this.paypleBillingAuthService = paypleBillingAuthService;
    this.paypleMobileRedirectService = paypleMobileRedirectService;
    this.billingKeyService = billingKeyService;
    this.subscriptionPaymentService = subscriptionPaymentService;
    this.subscriptionService = subscriptionService;
    this.objectMapper = objectMapper;
  }

  @Operation(summary = "빌링키 재과금", description = "활성 빌링키를 사용해 결제를 수행합니다.")
  @Logging(
      item = "Payment",
      action = "chargeWithBillingKey",
      includeParam = true,
      includeResult = true)
  @PostMapping("/billing/charge")
  public ResponseEntity<GrobleResponse<PaypleSubscriptionResultResponse>> chargeWithBillingKey(
      @Auth(required = true) Accessor accessor,
      @Valid @RequestBody PaypleBillingChargeRequest request) {

    SubscriptionPaymentResult result =
        subscriptionPaymentService.chargeWithBillingKey(
            accessor.getUserId(), request.getMerchantUid());

    PaypleSubscriptionResultResponse response =
        paymentMapper.toPaypleSubscriptionResultResponse(result);
    return success(response, BILLING_CHARGE_SUCCESS_MESSAGE);
  }

  @Operation(summary = "빌링키 등록 확인", description = "페이플 AUTH 응답을 확인하고 빌링키를 저장합니다.")
  @Logging(
      item = "Payment",
      action = "confirmBillingKey",
      includeParam = true,
      includeResult = true)
  @PostMapping("/billingkey/confirm")
  public ResponseEntity<GrobleResponse<PaypleSubscriptionResultResponse>> confirmBillingKey(
      @Auth(required = true) Accessor accessor,
      @Valid @RequestBody PaypleAuthResultRequest request) {

    PaypleAuthResultDTO dto = paymentMapper.toPaypleAuthResultDTO(request);
    SubscriptionPaymentResult result =
        subscriptionPaymentService.confirmBillingKeyRegistration(accessor.getUserId(), dto);

    PaypleSubscriptionResultResponse response =
        paymentMapper.toPaypleSubscriptionResultResponse(result);
    return success(response, BILLING_KEY_CONFIRM_SUCCESS_MESSAGE);
  }

  @Operation(summary = "정기결제 확정", description = "CERT 응답을 검증하고 결제 및 빌링키 저장을 완료합니다.")
  @Logging(
      item = "Payment",
      action = "confirmSubscription",
      includeParam = true,
      includeResult = true)
  @PostMapping("/subscription/confirm")
  public ResponseEntity<GrobleResponse<PaypleSubscriptionResultResponse>> confirmSubscription(
      @Auth(required = true) Accessor accessor,
      @Valid @RequestBody PaypleAuthResultRequest request) {

    PaypleAuthResultDTO dto = paymentMapper.toPaypleAuthResultDTO(request);
    SubscriptionPaymentResult result =
        subscriptionPaymentService.confirmSubscriptionPayment(accessor.getUserId(), dto);

    PaypleSubscriptionResultResponse response =
        paymentMapper.toPaypleSubscriptionResultResponse(result);
    return success(response, SUBSCRIPTION_CONFIRM_SUCCESS_MESSAGE);
  }

  @Operation(
      summary = PaymentSwaggerDocs.SUBSCRIPTION_CANCEL_SUMMARY,
      description = PaymentSwaggerDocs.SUBSCRIPTION_CANCEL_DESCRIPTION)
  @Logging(
      item = "Payment",
      action = "cancelSubscription",
      includeParam = true,
      includeResult = true)
  @DeleteMapping(ApiPaths.Payment.SUBSCRIPTION + "/{merchantUid}")
  public ResponseEntity<GrobleResponse<Void>> cancelSubscription(
      @Auth(required = true) Accessor accessor, @PathVariable("merchantUid") String merchantUid) {

    UserContext userContext = UserContextFactory.from(accessor);
    if (!userContext.isMember()) {
      throw PaymentAuthenticationRequiredException.forPayment();
    }

    subscriptionService.cancelSubscription(userContext.getId(), merchantUid);

    return responseHelper.success(
        null, ResponseMessages.Payment.SUBSCRIPTION_CANCELLED, HttpStatus.OK);
  }

  @Operation(
      summary = PaymentSwaggerDocs.BILLING_AUTH_MO_SUMMARY,
      description = PaymentSwaggerDocs.BILLING_AUTH_MO_DESCRIPTION)
  @PaymentApiResponses.BillingAuthResponses
  @Logging(
      item = "Payment",
      action = "requestBillingAuthMo",
      includeParam = false,
      includeResult = true)
  @GetMapping(ApiPaths.Payment.BILLING_AUTH_MO)
  public ResponseEntity<GrobleResponse<PaypleBillingAuthResponse>> requestBillingAuthMo() {
    PaypleAuthResponseDTO authResponse = paypleBillingAuthService.requestMoAuth();
    PaypleBillingAuthResponse response = paymentMapper.toPaypleBillingAuthResponse(authResponse);
    return success(response, ResponseMessages.Payment.BILLING_AUTH_SUCCESS);
  }

  @Operation(
      summary = PaymentSwaggerDocs.BILLING_AUTH_API_SUMMARY,
      description = PaymentSwaggerDocs.BILLING_AUTH_API_DESCRIPTION)
  @PaymentApiResponses.BillingAuthResponses
  @Logging(
      item = "Payment",
      action = "requestBillingAuthApi",
      includeParam = false,
      includeResult = true)
  @GetMapping(ApiPaths.Payment.BILLING_AUTH_API)
  public ResponseEntity<GrobleResponse<PaypleBillingAuthResponse>> requestBillingAuthApi() {
    PaypleAuthResponseDTO authResponse = paypleBillingAuthService.requestApiAuth();
    PaypleBillingAuthResponse response = paymentMapper.toPaypleBillingAuthResponse(authResponse);
    return success(response, ResponseMessages.Payment.BILLING_AUTH_SUCCESS);
  }

  @Operation(
      summary = PaymentSwaggerDocs.BILLING_REGISTER_SUMMARY,
      description = PaymentSwaggerDocs.BILLING_REGISTER_DESCRIPTION)
  @PaymentApiResponses.BillingKeyResponses
  @Logging(
      item = "Payment",
      action = "registerBillingKey",
      includeParam = true,
      includeResult = true)
  @PostMapping(ApiPaths.Payment.BILLING_REGISTER)
  public ResponseEntity<GrobleResponse<BillingKeyResponse>> registerBillingKey(
      @Auth(required = true) Accessor accessor,
      @Valid @RequestBody PaypleBillingRegistrationRequest request) {

    UserContext userContext = UserContextFactory.from(accessor);
    if (!userContext.isMember()) {
      throw PaymentAuthenticationRequiredException.forPayment();
    }

    RegisterBillingKeyCommand command = paymentMapper.toRegisterBillingKeyCommand(request);
    var billingKeyInfo = billingKeyService.registerBillingKey(userContext.getId(), command);
    BillingKeyResponse response = paymentMapper.toBillingKeyResponse(billingKeyInfo);

    return success(response, ResponseMessages.Payment.BILLING_KEY_REGISTERED);
  }

  @Operation(summary = "빌링키 삭제", description = "등록된 정기결제 빌링키를 삭제합니다.")
  @Logging(item = "Payment", action = "deleteBillingKey", includeParam = true, includeResult = true)
  @DeleteMapping("/billingkey")
  public ResponseEntity<GrobleResponse<Void>> deleteBillingKey(
      @Auth(required = true) Accessor accessor) {

    UserContext userContext = UserContextFactory.from(accessor);
    if (!userContext.isMember()) {
      throw PaymentAuthenticationRequiredException.forPayment();
    }

    billingKeyService.deleteBillingKey(userContext.getId());
    return success(null, ResponseMessages.Payment.BILLING_KEY_DELETED, HttpStatus.OK);
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
      boolean subscriptionFlow = isSubscriptionFlow(request);
      BillingKeyAction billingKeyAction =
          subscriptionFlow ? resolveBillingKeyAction(request, authResultDTO) : null;

      if (subscriptionFlow
          && billingKeyAction != null
          && context.getUserId() != null
          && !paymentProcessed) {
        try {
          SubscriptionPaymentResult subscriptionResult =
              processSubscriptionRedirect(billingKeyAction, authResultDTO, context, merchantUid);
          paymentProcessed = true;
          log.info(
              "✅ 모바일 정기결제 처리 완료 - merchantUid: {}, action: {}, status: {}",
              merchantUid,
              billingKeyAction.name(),
              subscriptionResult.getStatus());
        } catch (PaypleMobileRedirectException e) {
          log.error(
              "❌ 모바일 정기결제 처리 실패 - merchantUid: {}, error: {}",
              merchantUid,
              e.getClientMessage(),
              e);
          String errorRedirectUrl =
              paypleMobileRedirectService.buildFailureRedirectUrl(
                  merchantUid, e.getClientMessage());
          response.sendRedirect(errorRedirectUrl);
          return;
        } catch (Exception subscriptionException) {
          log.error(
              "❌ 모바일 정기결제 처리 중 예기치 못한 오류 - merchantUid: {}", merchantUid, subscriptionException);
          String errorRedirectUrl =
              paypleMobileRedirectService.buildFailureRedirectUrl(
                  merchantUid, PaypleMobileRedirectException.defaultClientMessage());
          response.sendRedirect(errorRedirectUrl);
          return;
        }
      }

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

  private boolean isSubscriptionFlow(HttpServletRequest request) {
    String subscriptionParam = request.getParameter("subscription");
    return subscriptionParam != null && subscriptionParam.equalsIgnoreCase("true");
  }

  private BillingKeyAction resolveBillingKeyAction(
      HttpServletRequest request, PaypleAuthResultDTO authResultDTO) {
    BillingKeyAction action = parseBillingKeyAction(request.getParameter("billingKeyAction"));
    if (action == null
        && authResultDTO != null
        && StringUtils.hasText(authResultDTO.getPayWork())) {
      String payWork = authResultDTO.getPayWork();
      if ("CERT".equalsIgnoreCase(payWork)) {
        action = BillingKeyAction.REGISTER_AND_CHARGE;
      } else if ("AUTH".equalsIgnoreCase(payWork)) {
        action = BillingKeyAction.REGISTER;
      }
    }
    return action;
  }

  private BillingKeyAction parseBillingKeyAction(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    try {
      return BillingKeyAction.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException ex) {
      log.warn("알 수 없는 billingKeyAction 값: {}", value);
      return null;
    }
  }

  private SubscriptionPaymentResult processSubscriptionRedirect(
      BillingKeyAction action,
      PaypleAuthResultDTO authResultDTO,
      MobileRedirectContext context,
      String merchantUid) {

    Long userId = context.getUserId();
    if (userId == null) {
      throw PaypleMobileRedirectException.orderUserMissing(merchantUid, null);
    }

    switch (action) {
      case REUSE:
        return subscriptionPaymentService.chargeWithBillingKey(userId, merchantUid);
      case REGISTER:
        if (authResultDTO == null) {
          throw PaypleMobileRedirectException.unexpected(
              new IllegalStateException("정기결제 인증 정보가 없습니다."));
        }
        return subscriptionPaymentService.confirmBillingKeyRegistration(userId, authResultDTO);
      case REGISTER_AND_CHARGE:
        if (authResultDTO == null) {
          throw PaypleMobileRedirectException.unexpected(
              new IllegalStateException("정기결제 인증 정보가 없습니다."));
        }
        return subscriptionPaymentService.confirmSubscriptionPayment(userId, authResultDTO);
      default:
        throw PaypleMobileRedirectException.unexpected(
            new IllegalArgumentException("지원하지 않는 billingKeyAction: " + action));
    }
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
