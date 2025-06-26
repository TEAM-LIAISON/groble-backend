package liaison.groble.application.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.application.order.service.OrderReader;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.link.PaypleLinkResendResponse;
import liaison.groble.application.payment.dto.link.PaypleLinkStatusResponse;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.payment.entity.PayplePayment;
import liaison.groble.domain.payment.enums.PayplePaymentStatus;
import liaison.groble.domain.payment.repository.PaymentRepository;
import liaison.groble.domain.payment.repository.PayplePaymentRepository;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.external.adapter.payment.PaypleRefundRequest;
import liaison.groble.external.adapter.payment.PaypleService;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플(Payple) 결제 서비스
 *
 * <p>페이플 PG사를 통한 결제 처리를 담당하는 서비스입니다.
 *
 * <p>주요 기능:
 *
 * <ul>
 *   <li>앱카드 결제 인증 정보 저장
 *   <li>앱카드 결제 승인 처리
 *   <li>결제 취소 및 환불 처리
 *   <li>결제 정보 검증
 *   <li>구매 완료 처리
 * </ul>
 *
 * <p>결제 프로세스:
 *
 * <ol>
 *   <li>클라이언트에서 페이플 SDK를 통해 결제 인증
 *   <li>인증 결과를 서버에 전송하여 저장
 *   <li>서버에서 페이플 API를 통해 결제 승인 요청
 *   <li>승인 성공 시 Order, Payment, Purchase 상태 업데이트
 * </ol>
 *
 * @author [oznchex]
 * @since [1.0]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayplePaymentService {

  private final PayplePaymentRepository payplePaymentRepository;
  private final PaypleService paypleService;
  private final PaypleConfig paypleConfig;
  private final OrderReader orderReader;
  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final PurchaseRepository purchaseRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void saveAppCardAuthResponse(Long userId, PaypleAuthResultDTO dto) {
    log.info("앱카드 인증 정보 저장 시작 - 주문번호: {}, userId: {}", dto.getPayOid(), userId);

    // 1. 주문 조회 및 검증
    Order order = orderReader.getOrderByMerchantUid(dto.getPayOid());

    // 2. 권한 검증
    validateOrderOwnership(order, userId);

    // 3. 주문 상태 검증
    validateOrderPendingStatus(order);

    // 4. 금액 검증
    validatePaymentPrice(order, dto.getPayTotal());

    // 5. PayplePayment 엔티티 생성 및 저장
    PayplePayment payplePayment = createPayplePayment(order, dto);
    payplePaymentRepository.save(payplePayment);

    log.info(
        "앱카드 인증 정보 저장 완료 - 주문번호: {}, payplePaymentId: {}", dto.getPayOid(), payplePayment.getId());
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

      // 2. 결제 상태 검증
      validatePaymentStatus(payplePayment);

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
  public PaypleAuthResponseDto getPaymentAuth(String pcdPayWork) {
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

      PaypleAuthResponseDto authResponse =
          PaypleAuthResponseDto.builder()
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
  public PaypleAuthResponseDto getPaymentAuthForCancel() {
    log.info("결제 취소를 위한 페이플 파트너 인증 요청 시작");

    // 취소 요청을 위한 일반 인증 사용
    return getPaymentAuth("AUTH");
  }

  /**
   * 결제 상태 검증
   *
   * <p>이미 처리된 결제인지 확인합니다.
   *
   * @param payplePayment 페이플 결제 정보
   * @throws IllegalStateException 이미 처리된 결제인 경우
   */
  private void validatePaymentStatus(PayplePayment payplePayment) {
    if (payplePayment.getStatus() != PayplePaymentStatus.PENDING) {
      throw new IllegalStateException(
          String.format(
              "이미 처리된 결제입니다. 주문번호: %s, 상태: %s",
              payplePayment.getPcdPayOid(), payplePayment.getStatus()));
    }
  }

  /**
   * 결제 취소 처리
   *
   * <p>승인된 결제를 취소하고 환불 처리합니다. 페이플 API를 통해 환불을 요청하고, 성공 시 관련 엔티티들의 상태를 업데이트합니다.
   *
   * @param paypleAuthResponseDto 페이플 인증 정보
   * @param merchantUid 주문 번호 (merchantUid)
   * @param reason 취소 사유
   * @return 취소 결과 JSON
   * @throws IllegalArgumentException 주문이나 결제 정보를 찾을 수 없는 경우
   * @throws IllegalStateException 취소할 수 없는 상태인 경우
   * @throws RuntimeException 결제 취소가 실패한 경우
   */
  @Transactional
  public JSONObject cancelPayment(
      PaypleAuthResponseDto paypleAuthResponseDto, String merchantUid, String reason) {
    log.info("결제 취소 처리 시작 - 주문번호: {}, 사유: {}", merchantUid, reason);

    try {
      // 1. 주문 및 결제 정보 조회
      log.debug("주문 및 결제 정보 조회 시작 - 주문번호: {}", merchantUid);
      Order order = findOrderByMerchantUid(merchantUid);
      PayplePayment payplePayment = findPayplePayment(merchantUid);
      Purchase purchase = findPurchaseByOrder(order);
      log.info(
          "주문 및 결제 정보 조회 완료 - 주문ID: {}, 결제ID: {}, 구매ID: {}",
          order.getId(),
          payplePayment.getId(),
          purchase.getId());

      // 2. 취소 가능 상태 검증
      log.debug(
          "취소 가능 상태 검증 시작 - 주문상태: {}, 결제상태: {}", order.getStatus(), payplePayment.getStatus());
      validateCancellableStatus(order, payplePayment);
      log.info("취소 가능 상태 검증 완료 - 주문번호: {}", merchantUid);

      // 3. 환불 요청
      log.info("페이플 환불 요청 시작 - 주문번호: {}, 결제금액: {}", merchantUid, purchase.getFinalPrice());
      PaypleRefundRequest refundRequest =
          createRefundRequest(merchantUid, payplePayment, paypleAuthResponseDto);
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

  /**
   * 취소 가능 상태 검증
   *
   * @param order 주문
   * @param payplePayment 페이플 결제 정보
   * @throws IllegalStateException 취소할 수 없는 상태인 경우
   */
  private void validateCancellableStatus(Order order, PayplePayment payplePayment) {
    // 주문 상태 검증
    if (order.getStatus() != Order.OrderStatus.PAID) {
      throw new IllegalStateException(
          String.format(
              "결제 완료된 주문만 취소할 수 있습니다. orderId: %d, status: %s", order.getId(), order.getStatus()));
    }

    // 결제 상태 검증
    if (payplePayment.getStatus() != PayplePaymentStatus.COMPLETED) {
      throw new IllegalStateException(
          String.format(
              "완료된 결제만 취소할 수 있습니다. 주문번호: %s, 결제상태: %s",
              payplePayment.getPcdPayOid(), payplePayment.getStatus()));
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
        .status(PayplePaymentStatus.PENDING)
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

      log.info(
          "페이플 결제 승인 성공 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}, "
              + "userId: {}, contentId: {}, finalPrice: {}원, purchaseStatus: {}",
          order.getId(),
          payment.getId(),
          purchase.getId(),
          order.getUser().getId(),
          purchase.getContent().getId(),
          order.getFinalPrice(),
          purchase.getStatus());

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

    // PayplePayment 상태 업데이트
    payplePayment.updateStatus(PayplePaymentStatus.FAILED);
    payplePaymentRepository.save(payplePayment);

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
    payplePayment.updateStatus(PayplePaymentStatus.COMPLETED);
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
    payment.markAsInProgress(); // 결제 진행 중 상태로 설정
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
      purchase.complete(); // PENDING → COMPLETED 상태 변경
      Purchase savedPurchase = purchaseRepository.save(purchase);

      log.debug(
          "Purchase 생성 및 완료 처리 성공 - orderId: {}, purchaseId: {}, contentId: {}, status: {}",
          order.getId(),
          savedPurchase.getId(),
          savedPurchase.getContent().getId(),
          savedPurchase.getStatus());

      return savedPurchase;
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
   * 주문으로 구매 정보 조회
   *
   * @param order 주문
   * @return 구매 정보
   * @throws IllegalArgumentException 구매 정보를 찾을 수 없는 경우
   */
  private Purchase findPurchaseByOrder(Order order) {
    return purchaseRepository
        .findByOrder(order)
        .orElseThrow(
            () -> new IllegalArgumentException("구매 정보를 찾을 수 없습니다. orderId=" + order.getId()));
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
      String merchantUid, PayplePayment payplePayment, PaypleAuthResponseDto authResponse) {
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

    // 1. PayplePayment 상태 업데이트
    payplePayment.updateStatus(PayplePaymentStatus.CANCELLED);
    payplePaymentRepository.save(payplePayment);

    // 2. Payment 취소 처리
    Payment payment = order.getPayment();
    if (payment == null) {
      throw new IllegalStateException("결제 정보를 찾을 수 없습니다. orderId=" + order.getId());
    }
    payment.cancel(reason);
    paymentRepository.save(payment);

    // 3. Order 취소 처리 (쿠폰 사용 취소 포함)
    order.cancelOrder(reason);
    orderRepository.save(order);

    // 4. Purchase 취소 처리
    purchase.cancel(reason);
    purchaseRepository.save(purchase);

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

  /**
   * 링크 결제 실패 처리
   *
   * <p>링크 결제가 실패하거나 취소된 경우의 처리를 수행합니다.
   *
   * @param resultDto 결제 결과 정보
   */
  @Transactional
  public void handleLinkPaymentFailure(PaypleAuthResultDTO resultDto) {
    log.info("링크 결제 실패 처리 시작 - 주문번호: {}, 사유: {}", resultDto.getPayOid(), resultDto.getPayMsg());

    try {
      // 1. PayplePayment 조회
      PayplePayment payplePayment = findPayplePayment(resultDto.getPayOid());

      // 2. 상태 업데이트
      payplePayment.updateStatus(PayplePaymentStatus.FAILED);
      payplePaymentRepository.save(payplePayment);

      // 3. Order 실패 처리
      Order order = orderReader.getOrderByMerchantUid(resultDto.getPayOid());
      order.failOrder("링크 결제 실패: " + resultDto.getPayMsg());
      orderRepository.save(order);

      log.info("링크 결제 실패 처리 완료 - orderId: {}", order.getId());

    } catch (Exception e) {
      log.error("링크 결제 실패 처리 중 오류 발생", e);
      throw new RuntimeException("링크 결제 실패 처리 오류: " + e.getMessage(), e);
    }
  }

  /**
   * 링크 결제 성공 처리
   *
   * <p>링크 결제가 성공한 경우의 처리를 수행합니다. 일반 결제와 동일하게 Order, Payment, Purchase를 완료 상태로 업데이트합니다.
   *
   * @param resultDto 결제 결과 정보
   */
  @Transactional
  public void handleLinkPaymentSuccess(PaypleAuthResultDTO resultDto) {
    log.info("링크 결제 성공 처리 시작 - 주문번호: {}", resultDto.getPayOid());

    try {
      // 1. 주문 및 결제 정보 조회
      Order order = orderReader.getOrderByMerchantUid(resultDto.getPayOid());
      PayplePayment payplePayment = findPayplePayment(resultDto.getPayOid());

      // 2. 결제 정보 업데이트
      updatePayplePaymentFromLinkResult(payplePayment, resultDto);

      // 3. Payment 엔티티 생성 및 완료 처리
      Payment payment = createAndSavePayment(order);

      // 4. Order 상태 업데이트 (결제 완료 + 쿠폰 사용 처리)
      order.completePayment();
      orderRepository.save(order);

      // 5. Purchase 생성 및 확정 처리
      Purchase purchase = createAndCompletePurchase(order);

      log.info(
          "링크 결제 성공 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
          order.getId(),
          payment.getId(),
          purchase.getId());

    } catch (Exception e) {
      log.error("링크 결제 성공 처리 중 오류 발생", e);
      throw new RuntimeException("링크 결제 성공 처리 오류: " + e.getMessage(), e);
    }
  }

  /**
   * 링크 결제 결과로 PayplePayment 업데이트
   *
   * @param payplePayment 페이플 결제 정보
   * @param resultDto 결제 결과
   */
  private void updatePayplePaymentFromLinkResult(
      PayplePayment payplePayment, PaypleAuthResultDTO resultDto) {
    payplePayment.updateStatus(PayplePaymentStatus.COMPLETED);
    payplePayment.updateApprovalInfo(
        resultDto.getPayTime(),
        resultDto.getPayCardName(),
        resultDto.getPayCardNum(),
        resultDto.getPayCardTradeNum(),
        resultDto.getPayCardAuthNo(),
        resultDto.getPayCardReceipt());
    payplePaymentRepository.save(payplePayment);
  }

  /**
   * 링크 결제 상태 조회
   *
   * @param merchantUid 주문번호
   * @param userId 사용자 ID
   * @return 링크 결제 상태 정보
   */
  @Transactional(readOnly = true)
  public PaypleLinkStatusResponse getLinkPaymentStatus(String merchantUid, Long userId) {
    log.info("링크 결제 상태 조회 - merchantUid: {}, userId: {}", merchantUid, userId);

    // 1. 주문 조회 및 권한 검증
    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    validateOrderOwnership(order, userId);

    // 2. PayplePayment 조회
    PayplePayment payplePayment = findPayplePayment(merchantUid);

    // 3. 상태 정보 생성
    return PaypleLinkStatusResponse.builder()
        .merchantUid(merchantUid)
        .status(payplePayment.getStatus().name())
        .linkUrl(payplePayment.getPcdRstUrl())
        .createdAt(payplePayment.getCreatedAt())
        .expireAt(payplePayment.getCreatedAt().plusDays(7)) // 보통 7일 후 만료
        .paymentStatus(payplePayment.getPcdPayRst())
        .paymentMessage(payplePayment.getPcdPayMsg())
        .paymentAt(
            payplePayment.getStatus() == PayplePaymentStatus.COMPLETED
                ? payplePayment.getUpdatedAt()
                : null)
        .build();
  }

  /**
   * 링크 결제 재전송
   *
   * @param merchantUid 주문번호
   * @param userId 사용자 ID
   * @param method 전송 방법 (SMS/EMAIL)
   * @return 재전송 결과
   */
  @Transactional
  public PaypleLinkResendResponse resendLinkPayment(
      String merchantUid, Long userId, String method) {
    log.info("링크 결제 재전송 - merchantUid: {}, userId: {}, method: {}", merchantUid, userId, method);

    // 1. 주문 조회 및 권한 검증
    Order order = orderReader.getOrderByMerchantUid(merchantUid);
    validateOrderOwnership(order, userId);

    // 2. PayplePayment 조회
    PayplePayment payplePayment = findPayplePayment(merchantUid);

    // 3. 재전송 가능 상태 검증
    if (payplePayment.getStatus() != PayplePaymentStatus.LINK_CREATED) {
      throw new IllegalStateException(
          "링크가 생성된 상태에서만 재전송이 가능합니다. 현재 상태: " + payplePayment.getStatus());
    }

    // 4. 링크 만료 검증 (7일)
    if (payplePayment.getCreatedAt().plusDays(7).isBefore(java.time.LocalDateTime.now())) {
      throw new IllegalStateException("링크가 만료되었습니다. 새로운 링크를 생성해주세요.");
    }

    // 5. 재전송 처리 (실제 SMS/EMAIL 전송 로직은 별도 서비스에서 구현)
    String linkUrl = payplePayment.getPcdRstUrl();
    String targetPhoneNumber = null;
    String targetEmail = null;
    String resultMessage = "";

    if ("SMS".equals(method)) {
      targetPhoneNumber = order.getUser().getPhoneNumber();
      // TODO: SMS 전송 서비스 호출
      resultMessage = "SMS 전송 완료";
    } else if ("EMAIL".equals(method)) {
      targetEmail = order.getUser().getEmail();
      // TODO: Email 전송 서비스 호출
      resultMessage = "이메일 전송 완료";
    }

    return PaypleLinkResendResponse.builder()
        .merchantUid(merchantUid)
        .linkUrl(linkUrl)
        .sentAt(java.time.LocalDateTime.now())
        .method(method)
        .targetPhoneNumber(targetPhoneNumber)
        .targetEmail(targetEmail)
        .resultMessage(resultMessage)
        .build();
  }

  /**
   * 빌링 카드 등록
   *
   * <p>정기결제를 위한 빌링 카드를 등록하고 빌링키를 발급받습니다. AUTH 방식으로 카드 인증만 진행하며, 실제 결제는 이루어지지 않습니다.
   *
   * @param userId 사용자 ID
   * @param authResult 카드 인증 결과
   * @return 등록 결과 (빌링키, 카드 정보 등)
   * @throws RuntimeException 등록 실패 시
   */
  @Transactional
  public Map<String, Object> registerBillingCard(Long userId, PaypleAuthResultDTO authResult) {
    log.info("빌링 카드 등록 시작 - userId: {}, payerId: {}", userId, authResult.getPayerId());

    try {
      // 1. 빌링키(PCD_PAYER_ID) 확인
      String billingKey = authResult.getPayerId();
      if (billingKey == null || billingKey.isEmpty()) {
        throw new IllegalStateException("빌링키가 반환되지 않았습니다.");
      }

      // 2. PayplePayment에 빌링 정보 저장
      PayplePayment billingInfo =
          PayplePayment.builder()
              .pcdPayRst(authResult.getPayRst())
              .pcdPayCode(authResult.getPayCode())
              .pcdPayMsg(authResult.getPayMsg())
              .pcdPayType(authResult.getPayType())
              .pcdPayWork("AUTH") // 빌링 카드 등록
              .pcdPayerNo(userId.toString())
              .pcdPayerId(billingKey) // 빌링키 저장
              .pcdPayerName(authResult.getPayerName())
              .pcdPayerHp(authResult.getPayerHp())
              .pcdPayerEmail(authResult.getPayerEmail())
              .pcdPayCardName(authResult.getPayCardName())
              .pcdPayCardNum(authResult.getPayCardNum())
              .pcdSimpleFlag("Y") // 빌링은 간편결제로 처리
              .status(PayplePaymentStatus.BILLING_REGISTERED)
              .build();

      payplePaymentRepository.save(billingInfo);

      // 3. 응답 생성
      Map<String, Object> result = new HashMap<>();
      result.put("payRst", authResult.getPayRst());
      result.put("payCode", authResult.getPayCode());
      result.put("payMsg", "빌링키 등록이 완료되었습니다.");
      result.put("payerId", billingKey);
      result.put("cardName", authResult.getPayCardName());
      result.put("cardNum", authResult.getPayCardNum());
      result.put("registeredAt", billingInfo.getCreatedAt());

      log.info("빌링 카드 등록 완료 - userId: {}, billingKey: {}", userId, billingKey);
      return result;

    } catch (Exception e) {
      log.error("빌링 카드 등록 실패 - userId: {}", userId, e);
      throw new RuntimeException("빌링 카드 등록 실패: " + e.getMessage(), e);
    }
  }

  /**
   * 빌링 결제 실행
   *
   * <p>등록된 빌링키를 사용하여 정기결제를 실행합니다.
   *
   * @param merchantUid 주문번호
   * @param userId 사용자 ID
   * @return 결제 결과
   * @throws IllegalArgumentException 주문 또는 빌링키를 찾을 수 없는 경우
   * @throws IllegalStateException 결제 불가능한 상태인 경우
   * @throws RuntimeException 결제 실패 시
   */
  @Transactional
  public JSONObject executeBillingPayment(String merchantUid, Long userId) {
    log.info("빌링 결제 실행 시작 - merchantUid: {}, userId: {}", merchantUid, userId);

    try {
      // 1. 주문 조회 및 검증
      Order order = orderReader.getOrderByMerchantUid(merchantUid);
      validateOrderOwnership(order, userId);
      validateOrderPendingStatus(order);

      // 2. 빌링키 조회
      PayplePayment billingInfo =
          payplePaymentRepository
              .findByPcdPayerNoAndStatus(userId.toString(), PayplePaymentStatus.BILLING_REGISTERED)
              .stream()
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("등록된 빌링키를 찾을 수 없습니다."));

      String billingKey = billingInfo.getPcdPayerId();
      if (billingKey == null || billingKey.isEmpty()) {
        throw new IllegalStateException("유효한 빌링키가 없습니다.");
      }

      // 3. 파트너 인증
      PaypleAuthResponseDto authResponse = getPaymentAuth("PAY");

      // 4. 빌링 결제 요청 파라미터 생성
      Map<String, String> params = new HashMap<>();
      params.put("PCD_CST_ID", authResponse.getCstId());
      params.put("PCD_CUST_KEY", authResponse.getCustKey());
      params.put("PCD_AUTH_KEY", authResponse.getAuthKey());
      params.put("PCD_PAY_TYPE", "card");
      params.put("PCD_PAYER_ID", billingKey); // 빌링키
      params.put("PCD_PAY_GOODS", order.getOrderItems().get(0).getContent().getTitle());
      params.put("PCD_PAY_TOTAL", order.getFinalPrice().toString());
      params.put("PCD_SIMPLE_FLAG", "Y");
      params.put("PCD_PAY_OID", merchantUid);
      params.put("PCD_PAYER_NO", userId.toString());
      params.put("PCD_PAYER_NAME", order.getUser().getUserProfile().getNickname());
      params.put("PCD_PAYER_HP", order.getUser().getPhoneNumber());
      params.put("PCD_PAYER_EMAIL", order.getUser().getEmail());

      // 5. 빌링 결제 실행
      JSONObject paymentResult = paypleService.paySimplePayment(params);

      // 6. 결제 결과 확인
      String payRst = (String) paymentResult.get("PCD_PAY_RST");

      if ("success".equalsIgnoreCase(payRst)) {
        // 결제 성공 처리
        handleBillingPaymentSuccess(order, paymentResult);
      } else {
        // 결제 실패 처리
        String errorMsg = (String) paymentResult.get("PCD_PAY_MSG");
        log.error("빌링 결제 실패 - merchantUid: {}, message: {}", merchantUid, errorMsg);

        // Order 상태 업데이트
        order.failOrder("빌링 결제 실패: " + errorMsg);
        orderRepository.save(order);
      }

      return paymentResult;

    } catch (Exception e) {
      log.error("빌링 결제 실행 중 오류 발생 - merchantUid: {}", merchantUid, e);
      throw e;
    }
  }

  /**
   * 빌링 결제 성공 처리
   *
   * @param order 주문
   * @param paymentResult 결제 결과
   */
  private void handleBillingPaymentSuccess(Order order, JSONObject paymentResult) {
    try {
      // 1. PayplePayment 엔티티 생성 및 저장
      PayplePayment payplePayment =
          PayplePayment.builder()
              .pcdPayRst("success")
              .pcdPayCode((String) paymentResult.get("PCD_PAY_CODE"))
              .pcdPayMsg((String) paymentResult.get("PCD_PAY_MSG"))
              .pcdPayType("card")
              .pcdPayWork("PAY")
              .pcdPayOid(order.getMerchantUid())
              .pcdPayTotal(order.getFinalPrice().toString())
              .pcdPayerNo(order.getUser().getId().toString())
              .pcdPayerId((String) paymentResult.get("PCD_PAYER_ID"))
              .pcdPayerName(order.getUser().getUserProfile().getNickname())
              .pcdPayCardName((String) paymentResult.get("PCD_PAY_CARDNAME"))
              .pcdPayCardNum((String) paymentResult.get("PCD_PAY_CARDNUM"))
              .pcdPayCardTradeNum((String) paymentResult.get("PCD_PAY_CARDTRADENUM"))
              .pcdPayCardAuthNo((String) paymentResult.get("PCD_PAY_CARDAUTHNO"))
              .pcdPayCardReceipt((String) paymentResult.get("PCD_CARD_RECEIPT"))
              .pcdPayTime((String) paymentResult.get("PCD_PAY_TIME"))
              .pcdSimpleFlag("Y")
              .status(PayplePaymentStatus.COMPLETED)
              .build();

      payplePaymentRepository.save(payplePayment);

      // 2. Payment 엔티티 생성 및 저장
      Payment payment = createAndSavePayment(order);

      // 3. Order 상태 업데이트
      order.completePayment();
      orderRepository.save(order);

      // 4. Purchase 생성 및 완료 처리
      Purchase purchase = createAndCompletePurchase(order);

      log.info(
          "빌링 결제 성공 처리 완료 - orderId: {}, paymentId: {}, purchaseId: {}",
          order.getId(),
          payment.getId(),
          purchase.getId());

    } catch (Exception e) {
      log.error("빌링 결제 성공 처리 중 오류 발생 - orderId: {}", order.getId(), e);

      // 실패 시 Order 상태 롤백
      rollbackOrderStatus(order, e);
      throw new RuntimeException("빌링 결제 처리 실패: " + e.getMessage(), e);
    }
  }
}
