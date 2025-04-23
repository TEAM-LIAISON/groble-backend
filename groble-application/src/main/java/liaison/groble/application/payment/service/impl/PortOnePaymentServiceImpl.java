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

    // 결제 요청 데이터 구성
    Map<String, Object> requestData = createPaymentRequestData(order, prepareDto);

    try {
      // 포트원 API 호출
      Map<String, Object> response =
          portOneClient.callApi("/payments/prepare", HttpMethod.POST, requestData, Map.class);

      // 응답에서 결제 키 추출
      String paymentKey = (String) response.get("payment_key");

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

  private Map<String, Object> createPaymentRequestData(Order order, PaymentPrepareDto prepareDto) {
    // 주문, 결제 정보 등으로 요청 데이터 구성하는 로직
    Map<String, Object> requestData = prepareDto.getAdditionalData();

    // 주문 정보 설정
    requestData.put("merchant_uid", order.getMerchantUid());
    requestData.put("amount", order.getTotalAmount());

    // 상품 정보
    String orderName = getOrderName(order);
    requestData.put("name", orderName);

    // 고객 정보가 없다면 주문 정보에서 추출
    if (!requestData.containsKey("buyer_name") && order.getPurchaser() != null) {
      requestData.put("buyer_name", order.getPurchaser().getName());
    }

    if (!requestData.containsKey("buyer_email") && order.getPurchaser() != null) {
      requestData.put("buyer_email", order.getPurchaser().getEmail());
    }

    if (!requestData.containsKey("buyer_tel") && order.getPurchaser() != null) {
      requestData.put("buyer_tel", order.getPurchaser().getPhone());
    }

    // 결제 수단
    requestData.put(
        "pay_method",
        convertToPortOnePayMethod(Payment.PaymentMethod.valueOf(prepareDto.getPaymentMethod())));

    // PG 설정 (이미 additionalData에 설정되어 있을 것)
    if (!requestData.containsKey("pg")) {
      requestData.put("pg", portOneProperties.getDefaultPg());
    }

    // 성공/실패 URL
    if (prepareDto.getSuccessUrl() != null) {
      requestData.put("success_url", prepareDto.getSuccessUrl());
    } else {
      requestData.put("success_url", portOneProperties.getPaymentRedirectUrl() + "/success");
    }

    if (prepareDto.getFailUrl() != null) {
      requestData.put("fail_url", prepareDto.getFailUrl());
    } else {
      requestData.put("fail_url", portOneProperties.getPaymentRedirectUrl() + "/fail");
    }

    return requestData;
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

    // 요청 데이터 구성
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("payment_key", approveDto.getPaymentKey());
    requestData.put("merchant_uid", approveDto.getOrderId());
    requestData.put("amount", approveDto.getAmount());

    try {
      // 포트원 API 호출
      Map<String, Object> response =
          portOneClient.callApi(
              "/payments/" + approveDto.getPaymentKey() + "/approve",
              HttpMethod.POST,
              requestData,
              Map.class);

      // 응답에서 필요한 정보 추출
      String status = (String) response.get("status");
      String pgTid = (String) response.get("pg_tid");
      String methodDetail = (String) response.getOrDefault("method_detail", "");

      // 카드 결제 정보 처리
      String cardNumber = "";
      String cardIssuerName = "";
      String cardAcquirerName = "";
      String cardInstallmentPlanMonths = "";

      if (payment.getPaymentMethod() == Payment.PaymentMethod.CARD
          && response.containsKey("card_info")) {
        Map<String, Object> cardInfo = (Map<String, Object>) response.get("card_info");
        cardNumber = (String) cardInfo.getOrDefault("number", "");
        String cardExpiryYear = (String) cardInfo.getOrDefault("expiry_year", "");
        String cardExpiryMonth = (String) cardInfo.getOrDefault("expiry_month", "");
        String cardIssuerCode = (String) cardInfo.getOrDefault("issuer_code", "");
        cardIssuerName = (String) cardInfo.getOrDefault("issuer_name", "");
        String cardAcquirerCode = (String) cardInfo.getOrDefault("acquirer_code", "");
        cardAcquirerName = (String) cardInfo.getOrDefault("acquirer_name", "");
        cardInstallmentPlanMonths = (String) cardInfo.getOrDefault("installment_plan_months", "");

        payment.setCardInfo(
            "카드 정보", // 실제로는 카드 정보 JSON을 저장
            cardNumber,
            cardExpiryYear,
            cardExpiryMonth,
            cardIssuerCode,
            cardIssuerName,
            cardAcquirerCode,
            cardAcquirerName,
            cardInstallmentPlanMonths);
      }

      // 가상계좌 정보 처리
      String vbankNumber = "";
      String vbankBankName = "";
      LocalDateTime vbankExpiryDate = null;

      if (payment.getPaymentMethod() == Payment.PaymentMethod.VIRTUAL_ACCOUNT
          && response.containsKey("vbank_info")) {
        Map<String, Object> vbankInfo = (Map<String, Object>) response.get("vbank_info");
        vbankNumber = (String) vbankInfo.getOrDefault("number", "");
        String bankCode = (String) vbankInfo.getOrDefault("code", "");
        vbankBankName = (String) vbankInfo.getOrDefault("name", "");
        String dueDateStr = (String) vbankInfo.getOrDefault("due_date", "");

        if (!dueDateStr.isEmpty()) {
          vbankExpiryDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME);
        }

        payment.setVirtualAccountInfo(vbankNumber, bankCode, vbankBankName, vbankExpiryDate);
      }

      // 결제 상태 업데이트
      Payment.PaymentStatus paymentStatus;
      switch (status) {
        case "paid":
          paymentStatus = Payment.PaymentStatus.PAID;
          break;
        case "ready":
          paymentStatus = Payment.PaymentStatus.WAITING_FOR_DEPOSIT;
          break;
        case "failed":
          paymentStatus = Payment.PaymentStatus.FAILED;
          break;
        default:
          paymentStatus = Payment.PaymentStatus.READY;
      }

      // 영수증 URL 저장
      String receiptUrl = null;
      if (response.containsKey("receipt_url")) {
        receiptUrl = (String) response.get("receipt_url");
        payment.setReceiptUrl(receiptUrl);
      }

      // 에스크로 정보 저장
      Boolean isEscrow = null;
      if (response.containsKey("escrow")) {
        isEscrow = Boolean.TRUE.equals(response.get("escrow"));
        payment.setEscrow(isEscrow);
      }

      // 현금영수증 정보 저장
      Boolean isCashReceipt = null;
      if (response.containsKey("cash_receipt")) {
        isCashReceipt = Boolean.TRUE.equals(response.get("cash_receipt"));
        payment.setCashReceipt(isCashReceipt);
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
          "127.0.0.1", // 실제 구현에서는 클라이언트 IP 사용
          "User-Agent" // 실제 구현에서는 클라이언트의 User-Agent 사용
          );

      log.info(
          "Payment approved successfully: key={}, status={}",
          approveDto.getPaymentKey(),
          paymentStatus);
      Payment savedPayment = paymentRepository.save(payment);

      // PaymentResultDto 구성하여 반환
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
          .isEscrow(isEscrow)
          .isCashReceipt(isCashReceipt)
          .build();

    } catch (Exception e) {
      log.error("Failed to approve payment", e);

      // 결제 실패 상태 업데이트
      payment.markAsFailed(e.getMessage(), Map.of("error", e.getMessage()));

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.ERROR,
          Payment.PaymentStatus.READY,
          "결제 승인 실패: " + e.getMessage(),
          Map.of(
              "payment_key",
              approveDto.getPaymentKey(),
              "merchant_uid",
              approveDto.getOrderId(),
              "amount",
              approveDto.getAmount()),
          Map.of("error", e.getMessage()),
          "127.0.0.1",
          "User-Agent");

      Payment savedPayment = paymentRepository.save(payment);

      // 실패한 경우에도 결과 반환
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

    // 취소 금액 검증
    if (cancelDto.getAmount().compareTo(BigDecimal.ZERO) <= 0
        || cancelDto.getAmount().compareTo(payment.getAmount()) > 0) {
      throw new IllegalArgumentException("올바르지 않은 취소 금액입니다: " + cancelDto.getAmount());
    }

    // 요청 데이터 구성
    Map<String, Object> requestData = new HashMap<>();
    requestData.put("payment_key", cancelDto.getPaymentKey());
    requestData.put("amount", cancelDto.getAmount());
    requestData.put("reason", cancelDto.getReason());

    try {
      // 포트원 API 호출
      Map<String, Object> response =
          portOneClient.callApi(
              "/payments/" + cancelDto.getPaymentKey() + "/cancel",
              HttpMethod.POST,
              requestData,
              Map.class);

      // 응답에서 필요한 정보 추출
      String cancelKey = (String) response.get("cancel_key");
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

      log.info(
          "Payment cancelled successfully: key={}, cancelKey={}",
          cancelDto.getPaymentKey(),
          cancelKey);
      Payment savedPayment = paymentRepository.save(payment);
      PaymentCancel savedCancel = paymentCancelRepository.save(paymentCancel);

      // PaymentResultDto 구성하여 반환
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

      // 취소 실패 정보 생성
      PaymentCancel paymentCancel = payment.cancel(cancelDto.getReason(), cancelDto.getAmount());
      paymentCancel.fail(e.getMessage());

      // 결제 로그 추가
      payment.addLog(
          PaymentLogType.ERROR,
          payment.getStatus(),
          "결제 취소 실패: " + e.getMessage(),
          requestData,
          Map.of("error", e.getMessage()),
          "127.0.0.1",
          "User-Agent");

      Payment savedPayment = paymentRepository.save(payment);
      PaymentCancel savedCancel = paymentCancelRepository.save(paymentCancel);

      // 실패한 경우에도 결과 반환
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
        Map<String, Object> response =
            portOneClient.callApi("/payments/" + paymentKey, HttpMethod.GET, null, Map.class);

        // TODO: 응답 데이터로 결제 정보 생성 로직 구현 필요
        log.warn("Payment not found in DB, retrieved from API but not saved: {}", paymentKey);

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

    // 추가 데이터 구성
    Map<String, Object> additionalData = new HashMap<>();

    // 가상계좌 발급 관련 필드 설정
    additionalData.put("pay_method", "vbank");

    // 은행 정보 설정
    if (virtualAccountDto.getBankInfo() != null) {
      additionalData.putAll(virtualAccountDto.getBankInfo());
    }

    // 결제 준비 - 내부적으로 원래 메서드 호출
    Payment payment = createPayment(order, Payment.PaymentMethod.VIRTUAL_ACCOUNT, additionalData);

    // 가상계좌 결제는 즉시 완료되지 않고 대기 상태로 변경
    payment.markAsWaitingForDeposit();

    log.info("Virtual account issued successfully: key={}", payment.getPaymentKey());
    Payment savedPayment = paymentRepository.save(payment);

    // PaymentResultDto 구성하여 반환
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
        .build();
  }

  @Override
  @Transactional
  public void handleWebhook(Map<String, Object> webhookData) {
    log.info("Handling payment webhook: {}", webhookData);

    String type = (String) webhookData.get("type");
    String paymentKey = (String) webhookData.get("payment_key");
    String status = (String) webhookData.get("status");

    if (paymentKey == null) {
      log.warn("Ignoring webhook without payment_key: {}", webhookData);
      return;
    }

    // 결제 정보 조회
    Payment payment = paymentRepository.findByPaymentKey(paymentKey).orElse(null);

    if (payment == null) {
      log.warn("Payment not found for key: {}", paymentKey);

      // 필요시 결제 정보 생성 로직 구현 가능
      return;
    }

    // 웹훅 ID 및 메타데이터 저장
    payment.updateFromWebhook(webhookData);

    // 이벤트 유형에 따른 처리
    switch (type) {
      case "PAYMENT":
        handlePaymentWebhook(payment, status, webhookData);
        break;

      case "CANCEL":
        handleCancelWebhook(payment, webhookData);
        break;

      case "VBANK":
        handleVbankWebhook(payment, status, webhookData);
        break;

      default:
        log.debug("Unhandled webhook type: {}", type);
    }

    paymentRepository.save(payment);
  }

  // 결제 웹훅 처리 메서드
  private void handlePaymentWebhook(
      Payment payment, String status, Map<String, Object> webhookData) {
    log.debug("Handling payment webhook: status={}", status);

    Payment.PaymentStatus beforeStatus = payment.getStatus();

    // 상태에 따른 처리
    switch (status) {
      case "ready":
        payment.updateStatus(Payment.PaymentStatus.READY);
        break;

      case "in_progress":
        payment.updateStatus(Payment.PaymentStatus.IN_PROGRESS);
        break;

      case "waiting_for_deposit":
        payment.updateStatus(Payment.PaymentStatus.WAITING_FOR_DEPOSIT);
        break;

      case "done":
      case "paid":
        if (payment.getStatus() != Payment.PaymentStatus.PAID) {
          String pgTid = (String) webhookData.getOrDefault("pg_tid", "");
          payment.markAsPaid(payment.getPaymentKey(), pgTid, webhookData);
          payment.getOrder().completePayment();
          orderRepository.save(payment.getOrder());
        }
        break;

      case "canceled":
        if (payment.getStatus() != Payment.PaymentStatus.CANCELLED
            && payment.getStatus() != Payment.PaymentStatus.PARTIALLY_CANCELLED) {

          BigDecimal cancelAmount =
              new BigDecimal(webhookData.getOrDefault("cancel_amount", "0").toString());
          String cancelReason = (String) webhookData.getOrDefault("cancel_reason", "웹훅으로 취소됨");

          if (cancelAmount.compareTo(payment.getAmount()) >= 0) {
            payment.updateStatus(Payment.PaymentStatus.CANCELLED);
            payment.getOrder().cancelOrder("결제 취소: " + cancelReason);
            orderRepository.save(payment.getOrder());
          } else {
            payment.updateStatus(Payment.PaymentStatus.PARTIALLY_CANCELLED);
          }
        }
        break;

      case "aborted":
        payment.updateStatus(Payment.PaymentStatus.ABORTED);
        payment.getOrder().failOrder("결제 중단");
        orderRepository.save(payment.getOrder());
        break;

      case "failed":
        payment.updateStatus(Payment.PaymentStatus.FAILED);
        String failReason = (String) webhookData.getOrDefault("fail_reason", "알 수 없는 실패 사유");
        payment.getOrder().failOrder("결제 실패: " + failReason);
        orderRepository.save(payment.getOrder());
        break;

      default:
        log.warn("Unknown payment status in webhook: {}", status);
    }

    // 상태 변경 로그 추가
    if (beforeStatus != payment.getStatus()) {
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

  // 취소 웹훅 처리 메서드
  private void handleCancelWebhook(Payment payment, Map<String, Object> webhookData) {
    log.debug("Handling cancel webhook");

    String cancelKey = (String) webhookData.get("cancel_key");
    BigDecimal cancelAmount =
        new BigDecimal(webhookData.getOrDefault("cancel_amount", "0").toString());
    String cancelReason = (String) webhookData.getOrDefault("cancel_reason", "웹훅으로 취소됨");

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

  // 가상계좌 웹훅 처리 메서드
  private void handleVbankWebhook(Payment payment, String status, Map<String, Object> webhookData) {
    log.debug("Handling vbank webhook: status={}", status);

    switch (status) {
      case "issued":
        // 가상계좌 발급 완료
        payment.updateStatus(Payment.PaymentStatus.WAITING_FOR_DEPOSIT);

        // 가상계좌 정보 업데이트
        String accountNumber = (String) webhookData.getOrDefault("vbank_num", "");
        String bankCode = (String) webhookData.getOrDefault("vbank_code", "");
        String bankName = (String) webhookData.getOrDefault("vbank_name", "");
        String dueDateStr = (String) webhookData.getOrDefault("vbank_due", "");

        LocalDateTime expiryDate = null;
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
          expiryDate = LocalDateTime.parse(dueDateStr, DateTimeFormatter.ISO_DATE_TIME);
        }

        payment.setVirtualAccountInfo(accountNumber, bankCode, bankName, expiryDate);

        // 로그 추가
        payment.addLog(
            PaymentLogType.VIRTUAL_ACCOUNT_ISSUED,
            payment.getStatus(),
            "가상계좌 발급됨",
            null,
            webhookData,
            "webhook",
            "webhook");
        break;

      case "deposited":
        // 입금 완료 처리
        String pgTid = (String) webhookData.getOrDefault("pg_tid", "");
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
        break;

      default:
        log.warn("Unknown vbank status in webhook: {}", status);
    }
  }

  // 새로 필요한 내부 메서드
  private Payment createPayment(
      Order order, Payment.PaymentMethod paymentMethod, Map<String, Object> additionalData) {
    // 결제 정보 생성
    Payment payment =
        Payment.builder()
            .order(order)
            .paymentMethod(paymentMethod)
            .amount(order.getTotalAmount())
            .selectedOptionType(getSelectedOptionType(order))
            .selectedOptionId(getSelectedOptionId(order))
            .build();

    // 추가 데이터 설정 - 실제 구현시 필요에 따라 조정
    String paymentKey = "virtual_" + System.currentTimeMillis(); // 가상의 키 생성
    payment.preparePayment(paymentKey, additionalData);

    return payment;
  }

  // 포트원 결제 수단 코드로 변환
  private String convertToPortOnePayMethod(Payment.PaymentMethod method) {
    switch (method) {
      case CARD:
        return "card";
      case VIRTUAL_ACCOUNT:
        return "vbank";
      case BANK_TRANSFER:
        return "trans";
      case MOBILE_PHONE:
        return "phone";
      case KAKAO_PAY:
        return "kakaopay";
      case PAYCO:
        return "payco";
      case NAVER_PAY:
        return "naverpay";
      case SAMSUNG_PAY:
        return "samsungpay";
      case TOSS_PAY:
        return "tosspay";
      case PAYPAL:
        return "paypal";
      default:
        return "card"; // 기본값
    }
  }

  // 주문에서 선택된 옵션 유형 가져오기
  private Payment.SelectedOptionType getSelectedOptionType(Order order) {
    if (order.getOrderItems().isEmpty()) {
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
    if (order.getOrderItems().isEmpty()) {
      return null;
    }

    OrderItem firstItem = order.getOrderItems().get(0);
    return firstItem.getOptionId();
  }
}
