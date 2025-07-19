package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.purchase.service.PurchaseReader;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayplePaymentService {

  private final PayplePaymentRepository payplePaymentRepository;
  private final PaypleService paypleService;
  private final NotificationService notificationService;
  private final PaypleConfig paypleConfig;
  private final PurchaseReader purchaseReader;
  private final OrderReader orderReader;
  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final PurchaseRepository purchaseRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void saveAppCardAuthResponse(Long userId, PaypleAuthResultDTO paypleAuthResultDTO) {
    log.info("앱카드 인증 정보 저장 시작 - 주문번호: {}, userId: {}", paypleAuthResultDTO.getPayOid(), userId);

    // 1. 주문 조회 및 검증
    Order order = orderReader.getOrderByMerchantUid(paypleAuthResultDTO.getPayOid());

    // 2. 권한 검증
    validateOrderOwnership(order, userId);

    // 3. 주문 상태 검증
    validateOrderPendingStatus(order);

    // 4. 금액 검증
    validatePaymentPrice(order, paypleAuthResultDTO.getPayTotal());

    // 5. PayplePayment 엔티티 생성 및 저장
    PayplePayment payplePayment = createPayplePayment(order, paypleAuthResultDTO);
    payplePaymentRepository.save(payplePayment);
  }

  /**
   * 앱카드 결제 승인 처리
   *
   * <p>저장된 인증 정보를 바탕으로 페이플 API를 통해 실제 결제 승인을 요청합니다. 승인 성공 시 주문, 결제, 구매 정보를 모두 완료 상태로 업데이트합니다.
   *
   * @param authResult 인증 결과 정보
   * @return 페이플 승인 응답 (성공/실패 정보 포함)
   * @throws IllegalArgumentException 결제 정보를 찾을 수 없는 경우
   * @throws RuntimeException 결제 승인 처리 중 오류가 발생한 경우
   */
  @Transactional
  public JSONObject processAppCardApproval(PaypleAuthResultDTO authResult) {
    log.info("앱카드 승인 처리 시작 - 주문번호: {}", authResult.getPayOid());

    try {
      // 1. 주문 및 결제 정보 조회
      Order order = orderReader.getOrderByMerchantUid(authResult.getPayOid());
      PayplePayment payplePayment = findPayplePayment(authResult.getPayOid());

      // 3. 페이플 승인 요청
      JSONObject approvalResult = requestPaypleApproval(authResult);

      // 4. 승인 결과에 따른 처리
      String payRst = (String) approvalResult.get("PCD_PAY_RST");

      if ("success".equalsIgnoreCase(payRst)) {
        handleApprovalSuccess(order, payplePayment, approvalResult);
      } else {
        handleApprovalFailure(order, payplePayment, approvalResult);
      }

      return approvalResult;

    } catch (Exception e) {
      log.error("앱카드 승인 처리 중 오류 발생 - 주문번호: {}", authResult.getPayOid(), e);
      throw e;
    }
  }

  /**
   * 페이플 파트너 인증 요청
   *
   * <p>페이플 API 사용을 위한 파트너 인증을 요청합니다. 인증 성공 시 반환되는 AuthKey와 ClientKey를 사용하여 결제 요청을 진행합니다.
   *
   * @param pcdPayWork 결제작업 구분 (AUTH: 인증, PAY: 결제, CERT: 인증 후 결제, LINKREG: 링크 결제)
   * @return 인증 응답 정보 (AuthKey, ClientKey 등)
   * @throws RuntimeException 인증 실패 시
   */
  @Transactional(readOnly = true)
  public PaypleAuthResponseDTO getPaymentAuth(String pcdPayWork) {
    log.info("페이플 파트너 인증 요청 시작 - payWork: {}", pcdPayWork);

    Map<String, String> params = new HashMap<>();
    params.put("cst_id", paypleConfig.getCstId()); // "test"
    params.put("custKey", paypleConfig.getCustKey()); // "abcd1234567890"
    params.put("PCD_PAY_WORK", pcdPayWork); // "LINKREG"

    try {
      JSONObject authResult = paypleService.payAuth(params);

      String authRst = (String) authResult.get("result");
      if (!"success".equalsIgnoreCase(authRst)) {
        String errorMsg = (String) authResult.get("result_msg");
        log.error("페이플 파트너 인증 실패 - message: {}", errorMsg);
        throw new RuntimeException("페이플 파트너 인증 실패: " + errorMsg);
      }

      PaypleAuthResponseDTO authResponse =
          PaypleAuthResponseDTO.builder()
              .result(authRst)
              .resultMsg((String) authResult.get("result_msg"))
              .cstId((String) authResult.get("cst_id"))
              .custKey((String) authResult.get("custKey"))
              .authKey((String) authResult.get("AuthKey"))
              .payWork((String) authResult.get("PCD_PAY_WORK"))
              .payUrl((String) authResult.get("PCD_PAY_URL"))
              .returnUrl((String) authResult.get("return_url"))
              .build();

      log.info("페이플 파트너 인증 성공 - authKey: {}", authResponse.getAuthKey());
      return authResponse;

    } catch (Exception e) {
      log.error("페이플 파트너 인증 중 오류 발생", e);
      throw new RuntimeException("페이플 파트너 인증 실패: " + e.getMessage(), e);
    }
  }

  @Transactional(readOnly = true)
  public PaypleAuthResponseDTO getPaymentAuthForCancel() {
    log.info("결제 취소를 위한 페이플 파트너 인증 요청 시작");

    // 취소 요청을 위한 일반 인증 사용
    return getPaymentAuth("AUTH");
  }

  /**
   * 결제 취소 처리
   *
   * <p>승인된 결제를 취소하고 환불 처리합니다. 페이플 API를 통해 환불을 요청하고, 성공 시 관련 엔티티들의 상태를 업데이트합니다.
   *
   * @param paypleAuthResponseDTO 페이플 인증 정보
   * @param merchantUid 주문 번호 (merchantUid)
   * @param reason 취소 사유
   * @return 취소 결과 JSON
   * @throws IllegalArgumentException 주문이나 결제 정보를 찾을 수 없는 경우
   * @throws IllegalStateException 취소할 수 없는 상태인 경우
   * @throws RuntimeException 결제 취소가 실패한 경우
   */
  @Transactional
  public JSONObject cancelPayment(
      PaypleAuthResponseDTO paypleAuthResponseDTO, String merchantUid, String reason) {
    log.info("결제 취소 처리 시작 - 주문번호: {}, 사유: {}", merchantUid, reason);

    try {
      // 1. 주문 및 결제 정보 조회
      log.debug("주문 및 결제 정보 조회 시작 - 주문번호: {}", merchantUid);
      Order order = findOrderByMerchantUid(merchantUid);
      PayplePayment payplePayment = findPayplePayment(merchantUid);
      Purchase purchase = purchaseReader.getPurchaseByOrderId(order.getId());
      log.info(
          "주문 및 결제 정보 조회 완료 - 주문ID: {}, 결제ID: {}, 구매ID: {}",
          order.getId(),
          payplePayment.getId(),
          purchase.getId());

      // 2. 취소 가능 상태 검증
      log.debug("취소 가능 상태 검증 시작 - 주문상태: {}", order.getStatus());
      validateCancellableStatus(order);
      log.info("취소 가능 상태 검증 완료 - 주문번호: {}", merchantUid);

      // 3. 환불 요청
      log.info("페이플 환불 요청 시작 - 주문번호: {}, 결제금액: {}", merchantUid, purchase.getFinalPrice());
      PaypleRefundRequest refundRequest =
          createRefundRequest(merchantUid, payplePayment, paypleAuthResponseDTO);
      log.debug("환불 요청 데이터 생성 완료 - 요청금액: {}", refundRequest.getRefundTotal());

      JSONObject refundResult = paypleService.payRefund(refundRequest);
      log.info("페이플 환불 요청 완료 - 주문번호: {}, 응답결과: {}", merchantUid, refundResult.get("PCD_PAY_RST"));

      // 4. 환불 결과 처리
      String refundRst = (String) refundResult.get("PCD_PAY_RST");
      log.debug("환불 결과 처리 시작 - 결과코드: {}, 주문번호: {}", refundRst, merchantUid);

      if ("success".equalsIgnoreCase(refundRst)) {
        log.info("환불 성공 처리 시작 - 주문번호: {}", merchantUid);
        handleRefundSuccess(order, payplePayment, purchase, reason);
        log.info("결제 취소 처리 완료 - 주문번호: {}, 환불금액: {}", merchantUid, purchase.getFinalPrice());
      } else {
        log.warn("환불 실패 처리 시작 - 주문번호: {}, 실패사유: {}", merchantUid, refundResult.get("PCD_PAY_MSG"));
        handleRefundFailure(merchantUid, refundResult);
      }

      return refundResult;

    } catch (IllegalArgumentException | IllegalStateException e) {
      log.warn("결제 취소 처리 실패 - 주문번호: {}, 사유: {}", merchantUid, e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("결제 취소 처리 중 예상치 못한 오류 발생 - 주문번호: {}", merchantUid, e);
      throw e;
    }
  }

  private void validateCancellableStatus(Order order) {
    // 주문 상태 검증
    if (order.getStatus() != Order.OrderStatus.CANCEL_REQUEST) {
      throw new IllegalStateException(
          String.format(
              "결제 완료된 주문만 취소할 수 있습니다. orderId: %d, status: %s", order.getId(), order.getStatus()));
    }
  }

  /**
   * 주문 소유권 검증
   *
   * @param order 주문
   * @param userId 요청 사용자 ID
   * @throws IllegalStateException 주문 사용자와 요청 사용자가 다른 경우
   */
  private void validateOrderOwnership(Order order, Long userId) {
    if (!order.getUser().getId().equals(userId)) {
      throw new IllegalStateException(
          String.format(
              "주문 접근 권한이 없습니다. orderId=%d, 주문userId=%d, 요청userId=%d",
              order.getId(), order.getUser().getId(), userId));
    }
  }

  /**
   * 주문 상태 검증 (결제 대기 상태인지 확인)
   *
   * @param order 주문
   * @throws IllegalStateException 결제 대기 상태가 아닌 경우
   */
  private void validateOrderPendingStatus(Order order) {
    if (order.getStatus() != Order.OrderStatus.PENDING) {
      throw new IllegalStateException(
          String.format(
              "결제 대기 상태의 주문만 처리할 수 있습니다. orderId=%d, status=%s", order.getId(), order.getStatus()));
    }
  }

  /**
   * 결제 금액 검증
   *
   * @param order 주문
   * @param paymentPrice 결제 금액 (문자열)
   * @throws IllegalStateException 금액이 일치하지 않는 경우
   */
  private void validatePaymentPrice(Order order, String paymentPrice) {
    try {
      // 문자열을 BigDecimal로 변환하여 비교
      BigDecimal paymentPriceDecimal = new BigDecimal(paymentPrice);

      if (order.getFinalPrice().compareTo(paymentPriceDecimal) != 0) {
        throw new IllegalStateException(
            String.format("결제 금액 불일치 - 주문금액: %s원, 결제금액: %s원", order.getFinalPrice(), paymentPrice));
      }
    } catch (NumberFormatException e) {
      throw new IllegalStateException(String.format("결제 금액 형식 오류 - 결제금액: %s", paymentPrice), e);
    }
  }

  private PayplePayment createPayplePayment(Order order, PaypleAuthResultDTO dto) {
    // 1) 들어온 DTO 전체를 JSON 문자열로 변환해서 로그로 출력
    try {
      String dtoJson = objectMapper.writeValueAsString(dto);
      log.debug("▶▶▶ PaypleAuthResultDTO = {}", dtoJson);
    } catch (JsonProcessingException e) {
      log.warn("DTO to JSON 변환 중 오류 발생, dto={}", dto, e);
    }

    // 2) Order 정보도 같이 로깅
    log.debug(
        "▶▶▶ Order 정보 = id:{}, userId:{}, finalPrice:{}",
        order.getId(),
        order.getUser().getId(),
        order.getFinalPrice());

    // 3) 할부 개월수 normalization
    String quota = normalizeQuota(dto.getPayCardQuota());
    log.debug("▶▶▶ normalized payCardQuota = {}", quota);

    // 4) 실제 엔티티 빌드
    return PayplePayment.builder()
        .pcdPayRst(dto.getPayRst())
        .pcdPayCode(dto.getPayCode())
        .pcdPayMsg(dto.getPayMsg())
        .pcdPayType(dto.getPayType())
        .pcdPayCardVer(dto.getCardVer())
        .pcdPayWork(dto.getPayWork())
        .pcdPayAuthKey(dto.getAuthKey())
        .pcdPayReqKey(dto.getPayReqKey())
        .pcdPayHost(dto.getPayHost())
        .pcdPayCofUrl(dto.getPayCofUrl())
        .pcdPayerNo(order.getUser().getId().toString())
        .pcdPayerName(dto.getPayerName())
        .pcdPayerHp(dto.getPayerHp())
        .pcdPayerEmail(dto.getPayerEmail())
        .pcdPayOid(dto.getPayOid())
        .pcdEasyPayMethod(dto.getEasyPayMethod())
        .pcdPayGoods(dto.getPayGoods())
        .pcdPayTotal(dto.getPayTotal())
        .pcdPayTaxTotal(dto.getPayTaxTotal())
        .pcdPayIsTax(dto.getPayIsTax())
        .pcdPayCardName(dto.getPayCardName())
        .pcdPayCardNum(dto.getPayCardNum())
        .pcdPayCardQuota(quota)
        .pcdPayCardTradeNum(dto.getPayCardTradeNum())
        .pcdPayCardAuthNo(dto.getPayCardAuthNo())
        .pcdPayCardReceipt(dto.getPayCardReceipt())
        .pcdPayTime(dto.getPayTime())
        .pcdRegulerFlag(dto.getRegulerFlag())
        .pcdPayYear(dto.getPayYear())
        .pcdPayMonth(dto.getPayMonth())
        .pcdSimpleFlag(dto.getSimpleFlag())
        .pcdRstUrl(dto.getRstUrl())
        .pcdUserDefine1(dto.getUserDefine1())
        .pcdUserDefine2(dto.getUserDefine2())
        .build();
  }

  /**
   * PayplePayment 조회
   *
   * @param payOid 주문 번호
   * @return PayplePayment
   * @throws IllegalArgumentException 결제 정보를 찾을 수 없는 경우
   */
  private PayplePayment findPayplePayment(String payOid) {
    return payplePaymentRepository
        .findByPcdPayOid(payOid)
        .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + payOid));
  }

  /**
   * 페이플 승인 요청
   *
   * @param authResult 인증 결과
   * @return 승인 응답
   */
  private JSONObject requestPaypleApproval(PaypleAuthResultDTO authResult) {
    Map<String, String> params = new HashMap<>();
    params.put("PCD_CST_ID", paypleConfig.getCstId());
    params.put("PCD_CUST_KEY", paypleConfig.getCustKey());
    params.put("PCD_AUTH_KEY", authResult.getAuthKey());
    params.put("PCD_PAY_REQKEY", authResult.getPayReqKey());
    params.put("PCD_PAY_COFURL", authResult.getPayCofUrl());
    log.info("페이플 승인 요청 - 주문번호: {}, authKey: {}", authResult.getPayOid(), authResult.getAuthKey());

    return paypleService.payAppCard(params);
  }

  /**
   * 결제 승인 성공 처리
   *
   * <p>결제 승인이 성공한 경우의 처리 로직:
   *
   * <ol>
   *   <li>결제 정보 일관성 검증
   *   <li>PayplePayment 상태 업데이트
   *   <li>Payment 엔티티 생성 및 완료 처리
   *   <li>Order 상태를 결제 완료로 변경
   *   <li>Purchase 생성 및 확정 처리
   * </ol>
   *
   * @param order 주문
   * @param payplePayment 페이플 결제 정보
   * @param approvalResult 승인 결과
   */
  private void handleApprovalSuccess(
      Order order, PayplePayment payplePayment, JSONObject approvalResult) {
    try {
      // 1. 결제 정보 일관성 검증
      validatePaymentConsistency(payplePayment, approvalResult);

      // 2. PayplePayment 승인 정보 업데이트
      updatePayplePaymentApproval(payplePayment, approvalResult);

      // 3. Payment 엔티티 생성 및 완료 처리
      Payment payment = createAndSavePayment(order);

      // 4. Order 상태 업데이트 (결제 완료 + 쿠폰 사용 처리)
      order.completePayment();

      // 5. Purchase 생성 및 확정 처리
      Purchase purchase = createAndCompletePurchase(order);

      // 6. 구매 알림 생성
      notificationService.sendContentSoldNotification(
          purchase.getUser(), purchase.getContent().getId());
    } catch (Exception e) {
      log.error("결제 승인 성공 처리 중 오류 발생 - orderId: {}", order.getId(), e);

      // 실패 시 Order 상태 롤백 처리
      rollbackOrderStatus(order, e);

      throw new RuntimeException("결제 승인 처리 실패: " + e.getMessage(), e);
    }
  }

  /**
   * 주문 상태 롤백 처리
   *
   * <p>결제 처리 중 오류 발생 시 주문 상태를 실패로 변경합니다. 쿠폰이 적용되어 있던 경우 쿠폰 사용도 취소됩니다.
   *
   * @param order 주문
   * @param e 발생한 예외
   */
  private void rollbackOrderStatus(Order order, Exception e) {
    try {
      order.failOrder("결제 승인 처리 실패: " + e.getMessage());
      orderRepository.save(order);
      log.info("주문 상태 롤백 완료 - orderId: {}, status: {}", order.getId(), order.getStatus());
    } catch (Exception rollbackException) {
      log.error("주문 상태 롤백 실패 - orderId: {}", order.getId(), rollbackException);
    }
  }

  /**
   * 결제 승인 실패 처리
   *
   * @param order 주문
   * @param payplePayment 페이플 결제 정보
   * @param approvalResult 승인 결과
   */
  private void handleApprovalFailure(
      Order order, PayplePayment payplePayment, JSONObject approvalResult) {
    String errorMsg = (String) approvalResult.get("PCD_PAY_MSG");
    String errorCode = (String) approvalResult.get("PCD_PAY_CODE");
    // Order 실패 처리 (쿠폰 사용 취소 포함)
    order.failOrder(String.format("결제 승인 실패 [%s]: %s", errorCode, errorMsg));
    orderRepository.save(order);

    log.error(
        "앱카드 결제 승인 실패 - orderId: {}, errorCode: {}, message: {}",
        order.getId(),
        errorCode,
        errorMsg);
  }

  /**
   * PayplePayment 승인 정보 업데이트
   *
   * @param payplePayment 페이플 결제 정보
   * @param approvalResult 승인 결과
   */
  private void updatePayplePaymentApproval(PayplePayment payplePayment, JSONObject approvalResult) {
    payplePayment.updateApprovalInfo(
        (String) approvalResult.get("PCD_PAY_TIME"),
        (String) approvalResult.get("PCD_PAY_CARDNAME"),
        (String) approvalResult.get("PCD_PAY_CARDNUM"),
        (String) approvalResult.get("PCD_PAY_CARDTRADENUM"),
        (String) approvalResult.get("PCD_PAY_CARDAUTHNO"),
        (String) approvalResult.get("PCD_PAY_CARDRECEIPT"));
    payplePaymentRepository.save(payplePayment);
  }

  /**
   * Payment 엔티티 생성 및 저장
   *
   * @param order 주문
   * @return 생성된 Payment
   */
  private Payment createAndSavePayment(Order order) {
    Payment payment =
        Payment.createPgPayment(
            order, order.getFinalPrice(), Payment.PaymentMethod.CARD, order.getMerchantUid());
    return paymentRepository.save(payment);
  }

  /**
   * Purchase 생성 및 완료 처리
   *
   * <p>결제 완료 후 구매 정보를 생성하고 즉시 완료 상태로 설정합니다. OrderService의 무료 주문 처리와 동일한 패턴을 따릅니다.
   *
   * @param order 주문
   * @return 생성된 Purchase
   */
  private Purchase createAndCompletePurchase(Order order) {
    try {
      Purchase purchase = Purchase.createFromOrder(order);
      return purchaseRepository.save(purchase);
    } catch (Exception e) {
      log.error("Purchase 생성 및 완료 처리 실패 - orderId: {}", order.getId(), e);
      throw new RuntimeException("구매 정보 생성 실패: " + e.getMessage(), e);
    }
  }

  /**
   * 결제 정보 일관성 검증
   *
   * <p>DB에 저장된 결제 정보와 페이플 승인 결과가 일치하는지 검증합니다. 주문번호, 금액, 상품명, 구매자 정보 등을 비교합니다.
   *
   * @param payment 저장된 결제 정보
   * @param approvalResult 페이플 승인 결과
   * @throws IllegalStateException 정보가 일치하지 않는 경우
   */
  private void validatePaymentConsistency(PayplePayment payment, JSONObject approvalResult) {
    // 필수 정보 검증
    validateRequiredFields(payment, approvalResult);

    // 구매자 정보 검증
    validateBuyerInfo(payment, approvalResult);

    // 선택적 정보 검증
    validateOptionalFields(payment, approvalResult);

    log.debug("결제 정보 일관성 검증 완료 - 주문번호: {}", payment.getPcdPayOid());
  }

  /**
   * 필수 결제 정보 검증
   *
   * @param payment 저장된 결제 정보
   * @param approvalResult 승인 결과
   */
  private void validateRequiredFields(PayplePayment payment, JSONObject approvalResult) {
    // 주문번호 검증
    String approvedOid = (String) approvalResult.get("PCD_PAY_OID");
    if (!payment.getPcdPayOid().equals(approvedOid)) {
      throw new IllegalStateException(
          String.format("주문번호 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayOid(), approvedOid));
    }

    // 결제 금액 검증
    String approvedTotal = (String) approvalResult.get("PCD_PAY_TOTAL");
    if (!payment.getPcdPayTotal().equals(approvedTotal)) {
      throw new IllegalStateException(
          String.format("결제금액 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayTotal(), approvedTotal));
    }

    // 상품명 검증
    String approvedGoods = (String) approvalResult.get("PCD_PAY_GOODS");
    if (!payment.getPcdPayGoods().equals(approvedGoods)) {
      throw new IllegalStateException(
          String.format("상품명 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayGoods(), approvedGoods));
    }
  }

  /**
   * 구매자 정보 검증
   *
   * @param payment 저장된 결제 정보
   * @param approvalResult 승인 결과
   */
  private void validateBuyerInfo(PayplePayment payment, JSONObject approvalResult) {
    //    validateIfNotNull(
    //        payment.getPcdPayerNo(), (String) approvalResult.get("PCD_PAYER_NO"), "구매자번호");
    validateIfNotNull(
        payment.getPcdPayerName(), (String) approvalResult.get("PCD_PAYER_NAME"), "구매자명");
    validateIfNotNull(
        payment.getPcdPayerHp(), (String) approvalResult.get("PCD_PAYER_HP"), "구매자 연락처");
  }

  /**
   * 선택적 결제 정보 검증
   *
   * @param payment 저장된 결제 정보
   * @param approvalResult 승인 결과
   */
  private void validateOptionalFields(PayplePayment payment, JSONObject approvalResult) {
    // 과세 여부
    validateIfNotNull(
        payment.getPcdPayIsTax(), (String) approvalResult.get("PCD_PAY_ISTAX"), "과세여부");

    // 복합과세 부가세 (복합과세인 경우만)
    if (payment.getPcdPayTaxTotal() != null) {
      String approvedTaxTotal = (String) approvalResult.get("PCD_PAY_TAXTOTAL");
      if (!payment.getPcdPayTaxTotal().equals(approvedTaxTotal)) {
        throw new IllegalStateException(
            String.format(
                "복합과세 부가세 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayTaxTotal(), approvedTaxTotal));
      }
    }

    // 할부개월수 (카드결제인 경우)
    if (payment.getPcdPayCardQuota() != null) {
      String approvedCardQuota = (String) approvalResult.get("PCD_PAY_CARDQUOTA");
      if (approvedCardQuota != null && !payment.getPcdPayCardQuota().equals(approvedCardQuota)) {
        throw new IllegalStateException(
            String.format(
                "할부개월수 불일치 - DB: %s, 승인결과: %s", payment.getPcdPayCardQuota(), approvedCardQuota));
      }
    }
  }

  /**
   * null이 아닌 경우에만 값 검증
   *
   * @param dbValue DB에 저장된 값
   * @param approvalValue 승인 결과 값
   * @param fieldName 필드명
   */
  private void validateIfNotNull(String dbValue, String approvalValue, String fieldName) {
    if (dbValue != null && !dbValue.equals(approvalValue)) {
      throw new IllegalStateException(
          String.format("%s 불일치 - DB: %s, 승인결과: %s", fieldName, dbValue, approvalValue));
    }
  }

  /**
   * 주문 조회
   *
   * @param merchantUid 주문 번호
   * @return 주문
   * @throws IllegalArgumentException 주문을 찾을 수 없는 경우
   */
  private Order findOrderByMerchantUid(String merchantUid) {
    return orderRepository
        .findByMerchantUid(merchantUid)
        .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid));
  }

  /**
   * 환불 요청 객체 생성
   *
   * @param merchantUid 주문 번호
   * @param payplePayment 페이플 결제 정보
   * @param authResponse 인증 응답 정보
   * @return 환불 요청 객체
   */
  private PaypleRefundRequest createRefundRequest(
      String merchantUid, PayplePayment payplePayment, PaypleAuthResponseDTO authResponse) {
    // 결제일자를 YYYYMMDD 형식으로 변환
    String payDate =
        payplePayment
            .getCreatedAt()
            .toLocalDate()
            .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

    return PaypleRefundRequest.builder()
        .authKey(authResponse.getAuthKey())
        .payOid(merchantUid)
        .payDate(payDate)
        .refundTotal(payplePayment.getPcdPayTotal())
        .refundTaxtotal(payplePayment.getPcdPayTaxTotal()) // 복합과세인 경우
        .build();
  }

  /**
   * 환불 성공 처리
   *
   * @param order 주문
   * @param payplePayment 페이플 결제 정보
   * @param purchase 구매 정보
   * @param reason 취소 사유
   */
  private void handleRefundSuccess(
      Order order, PayplePayment payplePayment, Purchase purchase, String reason) {

    // 1. Payment 취소 처리
    Payment payment = order.getPayment();
    if (payment == null) {
      throw new IllegalStateException("결제 정보를 찾을 수 없습니다. orderId=" + order.getId());
    }

    order.cancelOrder(reason);
    purchase.cancelPayment();

    log.info(
        "결제 취소 완료 - orderId: {}, paymentId: {}, purchaseId: {}, " + "환불금액: {}원, 사유: {}",
        order.getId(),
        payment.getId(),
        purchase.getId(),
        payplePayment.getPcdPayTotal(),
        reason);
  }

  /**
   * 환불 실패 처리
   *
   * @param merchantUid 주문 번호
   * @param refundResult 환불 결과
   * @throws RuntimeException 환불 실패
   */
  private void handleRefundFailure(String merchantUid, JSONObject refundResult) {
    String errorMsg = (String) refundResult.get("PCD_PAY_MSG");
    String errorCode = (String) refundResult.get("PCD_PAY_CODE");

    log.error(
        "결제 취소 실패 - merchantUid: {}, errorCode: {}, message: {}", merchantUid, errorCode, errorMsg);

    throw new RuntimeException(String.format("결제 취소 실패 [%s]: %s", errorCode, errorMsg));
  }

  private String normalizeQuota(String quota) {
    return (quota == null || quota.trim().isEmpty()) ? "00" : quota;
  }
}
