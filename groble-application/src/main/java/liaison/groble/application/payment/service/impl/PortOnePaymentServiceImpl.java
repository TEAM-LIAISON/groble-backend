package liaison.groble.application.payment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.payment.dto.PaymentApproveDto;
import liaison.groble.application.payment.dto.PaymentCancelDto;
import liaison.groble.application.payment.dto.PaymentPrepareDto;
import liaison.groble.application.payment.dto.PaymentResultDto;
import liaison.groble.application.payment.dto.VirtualAccountDto;
import liaison.groble.application.payment.service.PortOnePaymentService;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.entity.OrderItem;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PaymentCancel;
import liaison.groble.domain.payment.enums.PaymentCancelStatus;
import liaison.groble.domain.payment.enums.PaymentLogType;
import liaison.groble.domain.payment.repository.PaymentCancelRepository;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.external.config.PortOneProperties;
import liaison.groble.external.payment.PortOneApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOnePaymentServiceImpl implements PortOnePaymentService {

  private final PortOneApiClient portOneClient;
  private final PortOneProperties portOneProperties;
  private final PaymentRepository paymentRepository;
  private final PaymentCancelRepository paymentCancelRepository;
  private final OrderRepository orderRepository;

  @Override
  @Transactional
  public PaymentResultDto preparePayment(PaymentPrepareDto prepareDto) {
    log.info(
        "Preparing payment for order: {}, method: {}",
        prepareDto.getOrderId(),
        prepareDto.getPaymentMethod());

    // 주문 조회
    Order order =
        orderRepository
            .findById(prepareDto.getOrderId())
            .orElseThrow(
                () -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + prepareDto.getOrderId()));

    // 결제 수단 변환
    Payment.PaymentMethod paymentMethod =
        Payment.PaymentMethod.valueOf(prepareDto.getPaymentMethod());

    // V2 API용 결제 요청 데이터 구성
    Map<String, Object> requestData = createPaymentPrePareRequestDataV2(prepareDto);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi(
              "/payments/1/pre-register", HttpMethod.POST, requestData, Map.class);

      // 응답에서 결제 키 추출
      String paymentKey = (String) response.get("paymentKey");

      // 결제 정보 생성
      Payment payment =
          Payment.builder()
              .order(order)
              .paymentMethod(paymentMethod)
              .amount(order.getTotalAmount())
              .selectedOptionType(getSelectedOptionType(order))
              .selectedOptionId(getSelectedOptionId(order))
              .customerName(prepareDto.getCustomerName())
              .customerEmail(prepareDto.getCustomerEmail())
              .customerMobilePhone(prepareDto.getCustomerPhone())
              .build();

      // 결제 준비 정보 설정
      payment.preparePayment(paymentKey, requestData);

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.PAYMENT_CREATED,
          Payment.PaymentStatus.READY,
          "결제 준비됨",
          requestData,
          response,
          "127.0.0.1", // 실제 구현에서는 클라이언트 IP 사용
          "User-Agent" // 실제 구현에서는 클라이언트의 User-Agent 사용
          );

      // 저장
      Payment savedPayment = paymentRepository.save(payment);

      // 서비스 DTO 반환
      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .paymentMethod(savedPayment.getPaymentMethod().name())
          .customerName(savedPayment.getCustomerName())
          .customerEmail(savedPayment.getCustomerEmail())
          .customerPhone(savedPayment.getCustomerMobilePhone())
          .pgProvider("tosspayments")
          .clientKey(portOneProperties.getClientKey())
          .build();

    } catch (Exception e) {
      log.error("Failed to prepare payment", e);
      throw new RuntimeException("결제 준비 중 오류가 발생했습니다", e);
    }
  }

  private Map<String, Object> createPaymentPrePareRequestDataV2(PaymentPrepareDto prepareDto) {
    Map<String, Object> requestData = new HashMap<>();

    requestData.put("storeId", "store-e71e2cf1-047a-4618-8989-5582069acb4e");
    requestData.put("totalAmount", prepareDto.getTotalAmount());
    requestData.put("taxFreeAmount", prepareDto.getTaxFreeAmount());
    requestData.put("currency", "KRW");

    return requestData;
  }

  private Map<String, Object> createPaymentRequestDataV2(
      Order order, PaymentPrepareDto prepareDto) {
    // V2 API에 맞게 요청 데이터 구성
    Map<String, Object> requestData = new HashMap<>();

    // 주문 정보
    requestData.put("orderName", getOrderName(order));
    requestData.put("orderId", order.getMerchantUid());
    requestData.put("amount", order.getTotalAmount());

    // 결제 방법
    requestData.put(
        "method",
        convertToPortOnePayMethodV2(Payment.PaymentMethod.valueOf(prepareDto.getPaymentMethod())));

    // 고객 정보
    Map<String, Object> customerData = new HashMap<>();
    customerData.put(
        "name",
        prepareDto.getCustomerName() != null
            ? prepareDto.getCustomerName()
            : (order.getPurchaser() != null ? order.getPurchaser().getName() : ""));
    customerData.put(
        "email",
        prepareDto.getCustomerEmail() != null
            ? prepareDto.getCustomerEmail()
            : (order.getPurchaser() != null ? order.getPurchaser().getEmail() : ""));
    customerData.put(
        "phoneNumber",
        prepareDto.getCustomerPhone() != null
            ? prepareDto.getCustomerPhone()
            : (order.getPurchaser() != null ? order.getPurchaser().getPhone() : ""));
    requestData.put("customer", customerData);

    // 성공/실패 URL
    requestData.put(
        "successUrl",
        prepareDto.getSuccessUrl() != null
            ? prepareDto.getSuccessUrl()
            : portOneProperties.getPaymentRedirectUrl() + "/success");
    requestData.put(
        "failUrl",
        prepareDto.getFailUrl() != null
            ? prepareDto.getFailUrl()
            : portOneProperties.getPaymentRedirectUrl() + "/fail");

    // 추가 설정
    if (prepareDto.getAdditionalData() != null && !prepareDto.getAdditionalData().isEmpty()) {
      // V2 API에 맞게 추가 데이터 변환
      if (prepareDto.getAdditionalData().containsKey("pg")) {
        requestData.put("pgProvider", prepareDto.getAdditionalData().get("pg"));
      } else {
        requestData.put("pgProvider", portOneProperties.getDefaultPg());
      }

      // 기타 필요한 추가 데이터 처리
      if (prepareDto.getAdditionalData().containsKey("card_quota")) {
        // 카드 할부 관련 설정
        Map<String, Object> cardOptions = new HashMap<>();
        cardOptions.put("installment", prepareDto.getAdditionalData().get("card_quota"));
        requestData.put("cardOptions", cardOptions);
      }
    } else {
      requestData.put("pgProvider", portOneProperties.getDefaultPg());
    }

    return requestData;
  }

  @Override
  @Transactional
  public PaymentResultDto approvePayment(PaymentApproveDto approveDto) {
    log.info(
        "Approving payment: key={}, orderId={}, amount={}",
        approveDto.getPaymentKey(),
        approveDto.getOrderId(),
        approveDto.getAmount());

    // 주문 조회
    Order order =
        orderRepository
            .findByMerchantUid(approveDto.getOrderId())
            .orElseThrow(
                () -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + approveDto.getOrderId()));

    // 결제 정보 조회
    Payment payment = order.getPayment();
    if (payment == null) {
      throw new IllegalStateException("주문에 대한 결제 정보가 없습니다: " + approveDto.getOrderId());
    }

    // 금액 검증
    if (payment.getAmount().compareTo(approveDto.getAmount()) != 0) {
      log.error(
          "Payment amount mismatch: expected={}, actual={}",
          payment.getAmount(),
          approveDto.getAmount());
      throw new IllegalArgumentException("결제 금액이 일치하지 않습니다");
    }

    // V2 API 요청 데이터 구성
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("paymentKey", approveDto.getPaymentKey());
    requestData.put("orderId", approveDto.getOrderId());
    requestData.put("amount", approveDto.getAmount());

    try {
      // 포트원 V2 API 호출 - 결제 승인
      Map<String, Object> response =
          portOneClient.callApi(
              "/payments/" + approveDto.getPaymentKey() + "/approve",
              HttpMethod.POST,
              requestData,
              Map.class);

      // 응답에서 필요한 정보 추출
      String status = (String) response.get("status");
      String pgTid = (String) response.get("transactionKey");

      // 카드 결제 정보 처리
      String cardNumber = "";
      String cardIssuerName = "";
      String cardAcquirerName = "";
      String cardInstallmentPlanMonths = "";

      if (payment.getPaymentMethod() == Payment.PaymentMethod.CARD
          && response.containsKey("card")) {
        Map<String, Object> cardInfo = (Map<String, Object>) response.get("card");
        cardNumber = (String) cardInfo.getOrDefault("number", "");
        cardIssuerName = (String) cardInfo.getOrDefault("issuerName", "");
        cardAcquirerName = (String) cardInfo.getOrDefault("acquirerName", "");
        cardInstallmentPlanMonths =
            String.valueOf(cardInfo.getOrDefault("installmentPlanMonths", ""));
      }

      // 가상계좌 정보 처리
      String vbankNumber = "";
      String vbankBankName = "";
      LocalDateTime vbankExpiryDate = null;

      if (payment.getPaymentMethod() == Payment.PaymentMethod.VIRTUAL_ACCOUNT
          && response.containsKey("virtualAccount")) {
        Map<String, Object> vbankInfo = (Map<String, Object>) response.get("virtualAccount");
        vbankNumber = (String) vbankInfo.getOrDefault("accountNumber", "");
        vbankBankName = (String) vbankInfo.getOrDefault("bankName", "");
        String dueDateStr = (String) vbankInfo.getOrDefault("dueDate", "");

        if (dueDateStr != null && !dueDateStr.isEmpty()) {
          vbankExpiryDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME);
        }
      }

      // 결제 상태 업데이트
      Payment.PaymentStatus paymentStatus;
      switch (status) {
        case "DONE":
          paymentStatus = Payment.PaymentStatus.PAID;
          break;
        case "READY":
          paymentStatus = Payment.PaymentStatus.WAITING_FOR_DEPOSIT;
          break;
        case "FAILED":
          paymentStatus = Payment.PaymentStatus.FAILED;
          break;
        default:
          paymentStatus = Payment.PaymentStatus.READY;
      }

      // 영수증 URL 저장
      String receiptUrl = null;
      if (response.containsKey("receipt")) {
        Map<String, Object> receiptInfo = (Map<String, Object>) response.get("receipt");
        receiptUrl = (String) receiptInfo.get("url");
      }

      // 결제 정보 업데이트
      payment.markAsPaid(approveDto.getPaymentKey(), pgTid, response);

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.PAYMENT_APPROVED,
          Payment.PaymentStatus.READY,
          "결제 승인됨",
          requestData,
          response,
          "127.0.0.1",
          "User-Agent");

      Payment savedPayment = paymentRepository.save(payment);

      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .paymentMethod(savedPayment.getPaymentMethod().name())
          .customerName(savedPayment.getCustomerName())
          .customerEmail(savedPayment.getCustomerEmail())
          .customerPhone(savedPayment.getCustomerMobilePhone())
          .pgProvider("tosspayments")
          .clientKey(portOneProperties.getClientKey())
          .cardNumber(cardNumber)
          .cardIssuerName(cardIssuerName)
          .cardAcquirerName(cardAcquirerName)
          .cardInstallmentPlanMonths(cardInstallmentPlanMonths)
          .vbankNumber(vbankNumber)
          .vbankBankName(vbankBankName)
          .vbankExpiryDate(vbankExpiryDate)
          .receiptUrl(receiptUrl)
          .build();

    } catch (Exception e) {
      log.error("Failed to approve payment", e);
      payment.markAsFailed(e.getMessage(), Map.of("error", e.getMessage()));
      Payment savedPayment = paymentRepository.save(payment);

      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .build();
    }
  }

  @Override
  @Transactional
  public PaymentResultDto cancelPayment(PaymentCancelDto cancelDto) {
    log.info(
        "Cancelling payment: key={}, amount={}, reason={}",
        cancelDto.getPaymentKey(),
        cancelDto.getAmount(),
        cancelDto.getReason());

    // 결제 정보 조회
    Payment payment =
        paymentRepository
            .findByPaymentKey(cancelDto.getPaymentKey())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + cancelDto.getPaymentKey()));

    // 취소 가능 상태 확인
    if (payment.getStatus() != Payment.PaymentStatus.PAID
        && payment.getStatus() != Payment.PaymentStatus.PARTIALLY_CANCELLED) {
      throw new IllegalStateException("취소할 수 없는 결제 상태입니다: " + payment.getStatus());
    }

    // V2 API 요청 데이터 구성
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("cancelReason", cancelDto.getReason());

    // V2 API에서는 부분 취소시에만 amount 포함
    if (cancelDto.getAmount().compareTo(payment.getAmount()) < 0) {
      requestData.put("cancelAmount", cancelDto.getAmount());
    }

    try {
      // 포트원 V2 API 호출 - 결제 취소
      Map<String, Object> response =
          portOneClient.callApi(
              "/payments/" + cancelDto.getPaymentKey() + "/cancel",
              HttpMethod.POST,
              requestData,
              Map.class);

      // 응답에서 필요한 정보 추출
      String cancelKey = (String) response.get("cancelKey");
      String status = (String) response.get("status");

      // 취소 내역 생성
      PaymentCancel paymentCancel = payment.cancel(cancelDto.getReason(), cancelDto.getAmount());
      paymentCancel.complete(cancelKey, response);

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.PAYMENT_CANCELED,
          payment.getStatus(),
          "결제 취소됨: " + cancelDto.getReason(),
          requestData,
          response,
          "127.0.0.1",
          "User-Agent");

      // 주문 상태 업데이트 (전체 취소인 경우)
      if (cancelDto.getAmount().compareTo(payment.getAmount()) == 0) {
        payment.getOrder().cancelOrder("결제 취소: " + cancelDto.getReason());
        orderRepository.save(payment.getOrder());
      }

      Payment savedPayment = paymentRepository.save(payment);
      PaymentCancel savedCancel = paymentCancelRepository.save(paymentCancel);

      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .paymentMethod(savedPayment.getPaymentMethod().name())
          .cancelReason(cancelDto.getReason())
          .cancelAmount(cancelDto.getAmount())
          .canceledAt(savedCancel.getCreatedAt())
          .build();

    } catch (Exception e) {
      log.error("Failed to cancel payment", e);

      PaymentCancel paymentCancel = payment.cancel(cancelDto.getReason(), cancelDto.getAmount());
      paymentCancel.fail(e.getMessage());

      Payment savedPayment = paymentRepository.save(payment);
      PaymentCancel savedCancel = paymentCancelRepository.save(paymentCancel);

      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .cancelReason("취소 실패: " + e.getMessage())
          .build();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentResultDto getPaymentDetails(String paymentKey) {
    log.info("Getting payment details: key={}", paymentKey);

    // 저장된 결제 정보 조회
    Payment payment = paymentRepository.findByPaymentKey(paymentKey).orElse(null);

    // 결제 정보가 없으면 포트원 API를 통해 조회
    if (payment == null) {
      try {
        // V2 API 호출 - 결제 정보 조회
        Map<String, Object> response =
            portOneClient.callApi("/payments/" + paymentKey, HttpMethod.GET, null, Map.class);

        // 임시 결과 반환
        return PaymentResultDto.builder().paymentKey(paymentKey).status("UNKNOWN").build();

      } catch (Exception e) {
        log.error("Failed to get payment details from API", e);
        throw new RuntimeException("결제 정보 조회 중 오류가 발생했습니다", e);
      }
    }

    // 결제 정보 반환
    return PaymentResultDto.builder()
        .id(payment.getId())
        .paymentKey(payment.getPaymentKey())
        .merchantUid(payment.getMerchantUid())
        .amount(payment.getAmount())
        .status(payment.getStatus().name())
        .paymentMethod(payment.getPaymentMethod().name())
        .customerName(payment.getCustomerName())
        .customerEmail(payment.getCustomerEmail())
        .customerPhone(payment.getCustomerMobilePhone())
        .receiptUrl(payment.getReceiptUrl())
        .isEscrow(payment.isEscrow())
        .isCashReceipt(payment.isCashReceipt())
        .build();
  }

  @Override
  @Transactional
  public PaymentResultDto issueVirtualAccount(VirtualAccountDto virtualAccountDto) {
    log.info("Issuing virtual account for order: {}", virtualAccountDto.getOrderId());

    // 주문 조회
    Order order =
        orderRepository
            .findById(virtualAccountDto.getOrderId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "주문을 찾을 수 없습니다: " + virtualAccountDto.getOrderId()));

    // V2 API용 가상계좌 발급 데이터 구성
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("orderName", getOrderName(order));
    requestData.put("orderId", order.getMerchantUid());
    requestData.put("amount", order.getTotalAmount());
    requestData.put("method", "가상계좌");

    // 고객 정보
    Map<String, Object> customerData = new HashMap<>();
    customerData.put("name", order.getPurchaser() != null ? order.getPurchaser().getName() : "");
    customerData.put("email", order.getPurchaser() != null ? order.getPurchaser().getEmail() : "");
    customerData.put(
        "phoneNumber", order.getPurchaser() != null ? order.getPurchaser().getPhone() : "");
    requestData.put("customer", customerData);

    // 가상계좌 옵션
    Map<String, Object> virtualAccountOptions = new HashMap<>();

    // 은행 정보가 있으면 추가
    if (virtualAccountDto.getBankInfo() != null && !virtualAccountDto.getBankInfo().isEmpty()) {
      if (virtualAccountDto.getBankInfo().containsKey("bank_code")) {
        virtualAccountOptions.put("bankCode", virtualAccountDto.getBankInfo().get("bank_code"));
      }
    }

    // 가상계좌 만료일 설정 (기본 7일)
    virtualAccountOptions.put("validHours", 168); // 7일 * 24시간

    requestData.put("virtualAccountOptions", virtualAccountOptions);
    requestData.put("pgProvider", portOneProperties.getDefaultPg());

    try {
      // 포트원 V2 API 호출 - 가상계좌 발급
      Map<String, Object> response =
          portOneClient.callApi("/payments", HttpMethod.POST, requestData, Map.class);

      // 응답에서 필요한 정보 추출
      String paymentKey = (String) response.get("paymentKey");

      // 결제 정보 생성 및 저장
      Payment payment =
          Payment.builder()
              .order(order)
              .paymentMethod(Payment.PaymentMethod.VIRTUAL_ACCOUNT)
              .amount(order.getTotalAmount())
              .selectedOptionType(getSelectedOptionType(order))
              .selectedOptionId(getSelectedOptionId(order))
              .customerName(order.getPurchaser() != null ? order.getPurchaser().getName() : "")
              .customerEmail(order.getPurchaser() != null ? order.getPurchaser().getEmail() : "")
              .customerMobilePhone(
                  order.getPurchaser() != null ? order.getPurchaser().getPhone() : "")
              .build();

      payment.preparePayment(paymentKey, requestData);
      payment.markAsWaitingForDeposit();

      // 가상계좌 정보 설정 (있는 경우)
      if (response.containsKey("virtualAccount")) {
        Map<String, Object> vbankInfo = (Map<String, Object>) response.get("virtualAccount");
        String vbankNumber = (String) vbankInfo.getOrDefault("accountNumber", "");
        String bankCode = (String) vbankInfo.getOrDefault("bankCode", "");
        String bankName = (String) vbankInfo.getOrDefault("bankName", "");

        // 만료일 설정
        String dueDateStr = (String) vbankInfo.getOrDefault("dueDate", "");
        LocalDateTime expiryDate = null;
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
          expiryDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME);
        }

        payment.setVirtualAccountInfo(vbankNumber, bankCode, bankName, expiryDate);
      }

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.VIRTUAL_ACCOUNT_ISSUED,
          payment.getStatus(),
          "가상계좌 발급됨",
          requestData,
          response,
          "127.0.0.1",
          "User-Agent");

      Payment savedPayment = paymentRepository.save(payment);

      return PaymentResultDto.builder()
          .id(savedPayment.getId())
          .paymentKey(savedPayment.getPaymentKey())
          .merchantUid(savedPayment.getMerchantUid())
          .amount(savedPayment.getAmount())
          .status(savedPayment.getStatus().name())
          .paymentMethod(savedPayment.getPaymentMethod().name())
          .customerName(savedPayment.getCustomerName())
          .customerEmail(savedPayment.getCustomerEmail())
          .customerPhone(savedPayment.getCustomerMobilePhone())
          .vbankNumber(savedPayment.getVirtualAccountNumber())
          .vbankBankName(savedPayment.getVirtualAccountBankName())
          .vbankExpiryDate(savedPayment.getVirtualAccountExpiryDate())
          .build();

    } catch (Exception e) {
      log.error("Failed to issue virtual account", e);
      throw new RuntimeException("가상계좌 발급 중 오류가 발생했습니다", e);
    }
  }

  @Override
  @Transactional
  public void handleWebhook(Map<String, Object> webhookData) {
    log.info("Handling payment webhook: {}", webhookData);

    // V2 API 웹훅 처리
    String eventType = (String) webhookData.get("eventType");
    String paymentKey = (String) webhookData.get("paymentKey");
    String status = (String) webhookData.get("status");

    if (paymentKey == null) {
      log.warn("Ignoring webhook without paymentKey: {}", webhookData);
      return;
    }

    // 결제 정보 조회
    Payment payment = paymentRepository.findByPaymentKey(paymentKey).orElse(null);

    if (payment == null) {
      log.warn("Payment not found for key: {}", paymentKey);
      return;
    }

    // 웹훅 ID 및 메타데이터 저장
    payment.updateFromWebhook(webhookData);

    // 이벤트 유형에 따른 처리
    switch (eventType) {
      case "PAYMENT_APPROVED":
        handlePaymentApprovedWebhook(payment, status, webhookData);
        break;
      case "PAYMENT_CANCELED":
        handlePaymentCanceledWebhook(payment, webhookData);
        break;
      case "PAYMENT_STATUS_CHANGED":
        handlePaymentStatusChangedWebhook(payment, status, webhookData);
        break;
      case "VIRTUAL_ACCOUNT_ISSUED":
        handleVirtualAccountIssuedWebhook(payment, webhookData);
        break;
      case "VIRTUAL_ACCOUNT_DEPOSIT_MATCHED":
        handleVirtualAccountDepositMatchedWebhook(payment, webhookData);
        break;
      default:
        log.debug("Unhandled webhook event type: {}", eventType);
    }

    paymentRepository.save(payment);
  }

  // 결제 승인 웹훅 처리
  private void handlePaymentApprovedWebhook(
      Payment payment, String status, Map<String, Object> webhookData) {
    log.debug("Handling payment approved webhook: status={}", status);

    Payment.PaymentStatus beforeStatus = payment.getStatus();

    if ("DONE".equals(status)) {
      if (payment.getStatus() != Payment.PaymentStatus.PAID) {
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        String pgTid = (String) data.getOrDefault("transactionKey", "");
        payment.markAsPaid(payment.getPaymentKey(), pgTid, webhookData);
        payment.getOrder().completePayment();
        orderRepository.save(payment.getOrder());

        // 결제 로그 추가
        payment.addLog(
            PaymentLogType.PAYMENT_APPROVED,
            beforeStatus,
            "웹훅으로 결제 승인됨",
            null,
            webhookData,
            "webhook",
            "webhook");
      }
    }
  }

  // 결제 취소 웹훅 처리
  private void handlePaymentCanceledWebhook(Payment payment, Map<String, Object> webhookData) {
    log.debug("Handling payment canceled webhook");

    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    String cancelKey = (String) data.get("cancelKey");

    BigDecimal cancelAmount = BigDecimal.ZERO;
    String cancelReason = "웹훅으로 취소됨";

    // cancels 필드가 리스트로 제공됨
    if (data.containsKey("cancels")) {
      Object cancelsObj = data.get("cancels");

      if (cancelsObj instanceof java.util.List) {
        // List<Map> 형태로 처리
        java.util.List<?> cancelsList = (java.util.List<?>) cancelsObj;
        if (!cancelsList.isEmpty()) {
          Object lastCancelObj = cancelsList.get(cancelsList.size() - 1);
          if (lastCancelObj instanceof Map) {
            Map<?, ?> lastCancel = (Map<?, ?>) lastCancelObj;
            Object amountObj = lastCancel.get("cancelAmount");
            if (amountObj != null) {
              cancelAmount = new BigDecimal(amountObj.toString());
            }

            Object reasonObj = lastCancel.get("cancelReason");
            if (reasonObj != null) {
              cancelReason = reasonObj.toString();
            }
          }
        }
      } else if (cancelsObj instanceof Map) {
        // 단일 Map으로 제공되는 경우
        Map<?, ?> cancelMap = (Map<?, ?>) cancelsObj;
        Object amountObj = cancelMap.get("cancelAmount");
        if (amountObj != null) {
          cancelAmount = new BigDecimal(amountObj.toString());
        }

        Object reasonObj = cancelMap.get("cancelReason");
        if (reasonObj != null) {
          cancelReason = reasonObj.toString();
        }
      }
    } else {
      // 취소 정보가 data 객체에 직접 포함된 경우
      Object amountObj = data.get("cancelAmount");
      if (amountObj != null) {
        cancelAmount = new BigDecimal(amountObj.toString());
      }

      Object reasonObj = data.get("cancelReason");
      if (reasonObj != null) {
        cancelReason = reasonObj.toString();
      }
    }

    // 이미 처리된 취소 여부 확인
    boolean alreadyProcessed =
        payment.getCancellations().stream().anyMatch(c -> cancelKey.equals(c.getCancelKey()));

    if (alreadyProcessed) {
      log.debug("Cancel already processed: {}", cancelKey);
      return;
    }

    // 새 취소 내역 생성
    PaymentCancel paymentCancel =
        PaymentCancel.builder()
            .payment(payment)
            .amount(cancelAmount)
            .reason(cancelReason)
            .status(PaymentCancelStatus.COMPLETED)
            .build();

    paymentCancel.complete(cancelKey, webhookData);
    payment.getCancellations().add(paymentCancel);

    // 결제 상태 업데이트
    if (cancelAmount.compareTo(payment.getAmount()) >= 0) {
      payment.updateStatus(Payment.PaymentStatus.CANCELLED);
      payment.getOrder().cancelOrder("결제 취소: " + cancelReason);
      orderRepository.save(payment.getOrder());
    } else {
      payment.updateStatus(Payment.PaymentStatus.PARTIALLY_CANCELLED);
    }

    // 취소 로그 추가
    payment.addLog(
        PaymentLogType.PAYMENT_CANCELED,
        payment.getStatus(),
        "웹훅으로 취소됨: " + cancelReason,
        null,
        webhookData,
        "webhook",
        "webhook");

    paymentCancelRepository.save(paymentCancel);
  }

  // 결제 상태 변경 웹훅 처리
  private void handlePaymentStatusChangedWebhook(
      Payment payment, String status, Map<String, Object> webhookData) {
    log.debug("Handling payment status changed webhook: status={}", status);

    Payment.PaymentStatus beforeStatus = payment.getStatus();
    Payment.PaymentStatus newStatus;

    // V2 API 상태값을 내부 상태값으로 변환
    switch (status) {
      case "READY":
        newStatus = Payment.PaymentStatus.READY;
        break;
      case "IN_PROGRESS":
        newStatus = Payment.PaymentStatus.IN_PROGRESS;
        break;
      case "WAITING_FOR_DEPOSIT":
        newStatus = Payment.PaymentStatus.WAITING_FOR_DEPOSIT;
        break;
      case "DONE":
        newStatus = Payment.PaymentStatus.PAID;
        break;
      case "CANCELED":
        newStatus = Payment.PaymentStatus.CANCELLED;
        break;
      case "PARTIAL_CANCELED":
        newStatus = Payment.PaymentStatus.PARTIALLY_CANCELLED;
        break;
      case "ABORTED":
        newStatus = Payment.PaymentStatus.ABORTED;
        break;
      case "EXPIRED":
        newStatus = Payment.PaymentStatus.EXPIRED;
        break;
      case "FAILED":
        newStatus = Payment.PaymentStatus.FAILED;
        break;
      default:
        log.warn("Unknown payment status in webhook: {}", status);
        return;
    }

    // 상태 변경
    if (beforeStatus != newStatus) {
      payment.updateStatus(newStatus);

      // 실패 처리
      if (newStatus == Payment.PaymentStatus.FAILED) {
        Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
        String failReason = (String) data.getOrDefault("failReason", "알 수 없는 실패 사유");
        payment.getOrder().failOrder("결제 실패: " + failReason);
        orderRepository.save(payment.getOrder());
      }

      // 취소 처리
      if (newStatus == Payment.PaymentStatus.CANCELLED) {
        String cancelReason = "결제 취소됨";
        payment.getOrder().cancelOrder("결제 취소: " + cancelReason);
        orderRepository.save(payment.getOrder());
      }

      // 중단 처리
      if (newStatus == Payment.PaymentStatus.ABORTED) {
        payment.getOrder().failOrder("결제 중단");
        orderRepository.save(payment.getOrder());
      }

      // 만료 처리
      if (newStatus == Payment.PaymentStatus.EXPIRED) {
        payment.getOrder().failOrder("결제 만료");
        orderRepository.save(payment.getOrder());
      }

      // 상태 변경 로그 추가
      payment.addLog(
          PaymentLogType.STATUS_CHANGED,
          beforeStatus,
          "웹훅으로 상태 변경됨: " + status,
          null,
          webhookData,
          "webhook",
          "webhook");
    }
  }

  // 가상계좌 발급 웹훅 처리
  private void handleVirtualAccountIssuedWebhook(Payment payment, Map<String, Object> webhookData) {
    log.debug("Handling virtual account issued webhook");

    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    Map<String, Object> virtualAccount = (Map<String, Object>) data.get("virtualAccount");

    if (virtualAccount != null) {
      String accountNumber = (String) virtualAccount.getOrDefault("accountNumber", "");
      String bankCode = (String) virtualAccount.getOrDefault("bankCode", "");
      String bankName = (String) virtualAccount.getOrDefault("bankName", "");
      String dueDateStr = (String) virtualAccount.getOrDefault("dueDate", "");

      LocalDateTime expiryDate = null;
      if (dueDateStr != null && !dueDateStr.isEmpty()) {
        expiryDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME);
      }

      payment.setVirtualAccountInfo(accountNumber, bankCode, bankName, expiryDate);
      payment.updateStatus(Payment.PaymentStatus.WAITING_FOR_DEPOSIT);

      // 로그 추가
      payment.addLog(
          PaymentLogType.VIRTUAL_ACCOUNT_ISSUED,
          payment.getStatus(),
          "가상계좌 발급됨",
          null,
          webhookData,
          "webhook",
          "webhook");
    }
  }

  // 가상계좌 입금 매칭 웹훅 처리
  private void handleVirtualAccountDepositMatchedWebhook(
      Payment payment, Map<String, Object> webhookData) {
    log.debug("Handling virtual account deposit matched webhook");

    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    String pgTid = (String) data.getOrDefault("transactionKey", "");

    if (payment.getStatus() != Payment.PaymentStatus.PAID) {
      payment.markAsPaid(payment.getPaymentKey(), pgTid, webhookData);
      payment.getOrder().completePayment();
      orderRepository.save(payment.getOrder());

      // 로그 추가
      payment.addLog(
          PaymentLogType.VIRTUAL_ACCOUNT_DEPOSIT,
          Payment.PaymentStatus.WAITING_FOR_DEPOSIT,
          "가상계좌 입금됨",
          null,
          webhookData,
          "webhook",
          "webhook");
    }
  }

  /**
   * 주문에서 결제 상품명을 추출하는 메서드
   *
   * @param order 주문 객체
   * @return 결제에 표시할 상품명
   */
  private String getOrderName(Order order) {
    // 주문 항목이 없는 경우 기본값 반환
    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      return "상품 주문";
    }

    // 첫 번째 상품명 가져오기
    String firstItemName = order.getOrderItems().get(0).getContentName();

    // 상품명이 없는 경우 기본값 설정
    if (firstItemName == null || firstItemName.trim().isEmpty()) {
      firstItemName = "상품";
    }

    // 여러 상품인 경우 "외 N건" 형식으로 표현
    int totalItems = order.getOrderItems().size();
    if (totalItems > 1) {
      return firstItemName + " 외 " + (totalItems - 1) + "건";
    }

    // 단일 상품인 경우 상품명만 반환
    return firstItemName;
  }

  // 포트원 V2 결제 수단 코드로 변환
  private String convertToPortOnePayMethodV2(Payment.PaymentMethod method) {
    switch (method) {
      case CARD:
        return "카드";
      case VIRTUAL_ACCOUNT:
        return "가상계좌";
      case BANK_TRANSFER:
        return "계좌이체";
      case MOBILE_PHONE:
        return "휴대폰";
      case KAKAO_PAY:
        return "카카오페이";
      case PAYCO:
        return "페이코";
      case NAVER_PAY:
        return "네이버페이";
      case SAMSUNG_PAY:
        return "삼성페이";
      case TOSS_PAY:
        return "토스페이";
      case PAYPAL:
        return "페이팔";
      default:
        return "카드"; // 기본값
    }
  }

  // 주문에서 선택된 옵션 유형 가져오기
  private Payment.SelectedOptionType getSelectedOptionType(Order order) {
    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      return null;
    }

    OrderItem firstItem = order.getOrderItems().get(0);
    switch (firstItem.getOptionType()) {
      case COACHING_OPTION:
        return Payment.SelectedOptionType.COACHING_OPTION;
      case DOCUMENT_OPTION:
        return Payment.SelectedOptionType.DOCUMENT_OPTION;
      default:
        return null;
    }
  }

  // 주문에서 선택된 옵션 ID 가져오기
  private Long getSelectedOptionId(Order order) {
    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      return null;
    }

    OrderItem firstItem = order.getOrderItems().get(0);
    return firstItem.getOptionId();
  }
}
