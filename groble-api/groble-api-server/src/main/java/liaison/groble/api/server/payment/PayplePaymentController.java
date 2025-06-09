package liaison.groble.api.server.payment;

import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.response.PaymentCancelResponse;
import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.application.payment.exception.PayplePaymentAuthException;
import liaison.groble.application.payment.service.PayplePaymentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "결제 관련 API", description = "페이플 결제 관련 API")
@RestController
@RequestMapping("/api/v1/payments/payple")
@RequiredArgsConstructor
public class PayplePaymentController {
  private final PayplePaymentService payplePaymentService;

  // 앱카드 결제 인증 결과를 수신하고 결제 승인 요청을 페이플 서버에 보낸다.
  @Operation(
      summary = "앱카드 결제 승인 요청",
      description = "앱카드 결제 인증 결과를 수신하고, Payple 서버에 승인 요청을 보냅니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 승인 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                """
                  {
                    "success": true,
                    "data": {
                      "payRst": "success",
                      "payCode": "0000",
                      "payMsg": "결제가 정상적으로 완료되었습니다.",
                      "payOid": "ORDER_1234",
                      "payType": "card",
                      "payTime": "20250605123045",
                      "payTotal": "10000",
                      "payCardName": "Samsung Card",
                      "payCardNum": "1234-****-****-5678",
                      "payCardQuota": "00",
                      "payCardTradeNum": "20250605123456001",
                      "payCardAuthNo": "12345678",
                      "payCardReceipt": "https://receipt.payple.kr/receipt/abcd1234"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파라미터 검증 실패 등)"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/app-card/request")
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> requestAppCardPayment(
      @Auth Accessor accessor, @Valid @RequestBody PaypleAuthResultDto authResultDto) {

    log.info(
        "페이플 인증 결과 수신 - 결과: {}, 코드: {}, 메시지: {}, 주문번호: {}",
        authResultDto.getPayRst(),
        authResultDto.getPayCode(),
        authResultDto.getPayMsg(),
        authResultDto.getPayOid());

    if (authResultDto.isError()) {
      log.error(
          "페이플 인증 실패 - 코드: {}, 메시지: {}", authResultDto.getPayCode(), authResultDto.getPayMsg());
      throw new PayplePaymentAuthException("페이플 인증 실패: " + authResultDto.getPayMsg());
    }

    if (authResultDto.isClosed()) {
      log.warn("페이플 인증 취소 - 사용자가 결제창을 닫음");
      return ResponseEntity.ok(
          GrobleResponse.success(AppCardPayplePaymentResponse.builder().build()));
    }

    // 인증 결과 저장
    payplePaymentService.saveAppCardAuthResponse(accessor.getUserId(), authResultDto);

    try {
      // 인증 성공에 대한 결제 승인 요청 처리
      JSONObject approvalResult = payplePaymentService.processAppCardApproval(authResultDto);

      // 승인 결과 확인
      String payRst = (String) approvalResult.get("PCD_PAY_RST");
      if (!"success".equalsIgnoreCase(payRst)) {
        String errorMsg = (String) approvalResult.get("PCD_PAY_MSG");
        log.error("페이플 결제 승인 실패 - 메시지: {}", errorMsg);
        throw new PayplePaymentAuthException("페이플 결제 승인 실패: " + errorMsg);
      }

      // 승인 성공 응답 생성
      AppCardPayplePaymentResponse response =
          AppCardPayplePaymentResponse.builder()
              .payRst(payRst)
              .payCode((String) approvalResult.get("PCD_PAY_CODE"))
              .payMsg((String) approvalResult.get("PCD_PAY_MSG"))
              .payOid((String) approvalResult.get("PCD_PAY_OID"))
              .payType((String) approvalResult.get("PCD_PAY_TYPE"))
              .payTime((String) approvalResult.get("PCD_PAY_TIME"))
              .payTotal((String) approvalResult.get("PCD_PAY_TOTAL"))
              .payCardName((String) approvalResult.get("PCD_PAY_CARDNAME"))
              .payCardNum((String) approvalResult.get("PCD_PAY_CARDNUM"))
              .payCardQuota((String) approvalResult.get("PCD_PAY_CARDQUOTA"))
              .payCardTradeNum((String) approvalResult.get("PCD_PAY_CARDTRADENUM"))
              .payCardAuthNo((String) approvalResult.get("PCD_PAY_CARDAUTHNO"))
              .payCardReceipt((String) approvalResult.get("PCD_CARD_RECEIPT"))
              .build();

      return ResponseEntity.ok(GrobleResponse.success(response));

    } catch (IllegalStateException e) {
      log.error("페이플 결제 검증 실패 - {}", e.getMessage());
      throw new PayplePaymentAuthException("결제 정보 검증 실패: " + e.getMessage());
    } catch (Exception e) {
      log.error("페이플 결제 처리 중 오류 발생", e);
      throw new PayplePaymentAuthException("결제 처리 중 오류가 발생했습니다.");
    }
  }

  @Operation(
      summary = "결제 취소",
      description = "결제를 취소하고 환불 처리합니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 취소 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                """
                  {
                    "success": true,
                    "data": {
                      "orderId": "ORDER_1234",
                      "status": "CANCELLED",
                      "canceledAt": "2025-06-08T12:30:45",
                      "cancelReason": "고객 요청"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (주문을 찾을 수 없음, 취소할 수 없는 상태 등)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @Auth Accessor accessor,
      @PathVariable String orderId,
      @Valid @RequestBody PaymentCancelRequest request) {

    log.info(
        "결제 취소 요청 - 주문번호: {}, 사유: {}, userId: {}",
        orderId,
        request.getReason(),
        accessor.getUserId());

    try {
      // 결제 취소 처리
      payplePaymentService.cancelPayment(orderId, request.getReason());

      // 취소 성공 응답 생성
      PaymentCancelResponse response =
          PaymentCancelResponse.builder()
              .orderId(orderId)
              .status("CANCELLED")
              .canceledAt(LocalDateTime.now())
              .cancelReason(request.getReason())
              .build();

      log.info("결제 취소 완료 - 주문번호: {}", orderId);
      return ResponseEntity.ok(GrobleResponse.success(response));

    } catch (IllegalArgumentException e) {
      log.error("결제 취소 실패 - 주문을 찾을 수 없음: {}", orderId, e);
      throw new PayplePaymentAuthException("주문을 찾을 수 없습니다: " + orderId);
    } catch (IllegalStateException e) {
      log.error("결제 취소 실패 - 취소할 수 없는 상태: {}", orderId, e);
      throw new PayplePaymentAuthException("취소할 수 없는 상태입니다: " + e.getMessage());
    } catch (RuntimeException e) {
      log.error("결제 취소 실패 - 환불 처리 오류: {}", orderId, e);
      throw new PayplePaymentAuthException("환불 처리 중 오류가 발생했습니다: " + e.getMessage());
    } catch (Exception e) {
      log.error("결제 취소 중 예상치 못한 오류 발생: {}", orderId, e);
      throw new PayplePaymentAuthException("결제 취소 처리 중 오류가 발생했습니다.");
    }
  }
}

//  // 링크 결제 요청이 들어온다
//  // 1. 파트너 인증 요청을 페이플 서버에 보낸다
//  // 2. 인증 결과를 받아온다.
//  // 3. 해당 인증 결과를 바탕으로 링크 생성 요청을 페이플 서버에 보낸다.
//  // 4. 링크 생성 결과를 받아온다.
//  // 5. 링크 생성 결과를 클라이언트에 반환한다.
//
//  @Operation(summary = "페이플 링크 결제 요청", description = "링크 결제를 요청하고 결제 링크를 받아옵니다.")
//  @PostMapping("/link-payment")
//  public ResponseEntity<GrobleResponse<PaypleLinkResponse>> requestPaypleLinkPayment(
//      @Valid @RequestBody PayplePaymentLinkRequest payplePaymentLinkRequest) {
//    PayplePaymentLinkRequestDto payplePaymentLinkRequestDto =
//        payplePaymentMapper.toPayplePaymentLinkRequestDto(payplePaymentLinkRequest);
//    PaypleAuthResponseDto paypleAuthResponseDto = payplePaymentService.getPaymentAuth("LINKREG");
//    log.info(
//        "페이플 링크 결제 요청 - authKey: {}, clientKey: {}, returnUrl: {}",
//        paypleAuthResponseDto.getAuthKey(),
//        paypleAuthResponseDto.getClientKey(),
//        paypleAuthResponseDto.getReturnUrl());
//    PaypleLinkResponseDto paypleLinkResponseDto =
//        payplePaymentService.processLinkPayment(payplePaymentLinkRequestDto,
// paypleAuthResponseDto);
//    PaypleLinkResponse paypleLinkResponse =
//        payplePaymentMapper.toPaypleLinkResponse(paypleLinkResponseDto);
//    return ResponseEntity.ok(GrobleResponse.success(paypleLinkResponse));
//  }
// }
//

//// * 5. 결제 완료 콜백 (/api/v1/payments/payple/complete)
//// *    - 페이플로부터 결제 결과 수신
//// *    - PayplePayment 상태 업데이트 (COMPLETED/FAILED)
//// *    - Order 상태 업데이트 (PAID/FAILED)
//// *    - Payment 엔티티 생성 및 상태 업데이트
//// *    - Purchase 엔티티 생성 (구매 완료)
//// *    - 쿠폰 사용 처리
//// *
//// * 6. 결제 실패 시
//// *    - 모든 엔티티 상태를 FAILED로 업데이트
//// *    - 쿠폰 사용 취소
//// *
//// * 7. 결제 취소 요청 시
//// *    - 페이플 환불 API 호출
//// *    - 모든 엔티티 상태를 CANCELLED로 업데이트
//// *    - 쿠폰 사용 취소
//// */
//
////
////    private final PayplePaymentService payplePaymentService;
////    private final OrderTermsService orderService;
////    private final PaymentService paymentService;
////    private final PurchaseService purchaseService;
////
////    @Operation(summary = "결제 요청", description = "페이플 결제를 위한 초기 요청")
////    @PostMapping("/request")
////    public ResponseEntity<PaymentRequestResponse> requestPayment(
////            @CurrentUser Long userId,
////            @RequestBody PaymentRequest request) {
////
////        // 2. PayplePayment 생성 (PENDING 상태)
////        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
////            .orderId(order.getMerchantUid())
////            .amount(order.getFinalAmount())
////            .payMethod(request.getPayMethod())
////            .productName(request.getProductName())
////            .build();
////
////        PaymentRequestResponseDto response = payplePaymentService.processPayment(userId,
//// paymentRequestDto);
////
////        // 3. 페이플 인증 정보 조회
////        PaypleAuthResponseDto authInfo =
//// payplePaymentService.getPaymentAuth(request.getPayWork());
////
////        return ResponseEntity.ok(
////            PaymentRequestResponse.builder()
////                .orderId(order.getMerchantUid())
////                .amount(order.getFinalAmount())
////                .authKey(authInfo.getAuthKey())
////                .clientKey(authInfo.getClientKey())
////                .returnUrl(authInfo.getReturnUrl())
////                .build()
////        );
////    }
////
////    @Operation(summary = "결제 완료 처리", description = "페이플 결제 완료 후 콜백 처리")
////    @PostMapping("/complete")
////    public ResponseEntity<PaymentCompleteResponse> completePayment(
////            @CurrentUser Long userId,
////            @RequestBody PayplePaymentResultDto resultDto) {
////
////        // 1. PayplePayment 상태 업데이트
////        PaymentCompleteResponseDto paypleResponse =
//// payplePaymentService.completePayment(resultDto);
////
////        // 2. Order 조회
////        Order order = orderService.findByMerchantUid(resultDto.getPayOid());
////
////        if ("SUCCESS".equals(paypleResponse.getStatus())) {
////            // 3. Payment 엔티티 생성 및 저장
////            Payment payment = Payment.builder()
////                .order(order)
////                .paymentKey(resultDto.getPayKey())
////                .paymentMethod(PaymentMethod.valueOf(resultDto.getPayMethod()))
////                .amount(order.getFinalAmount())
////                .selectedOptionType(order.getOrderItems().get(0).getOptionType())
////                .selectedOptionId(order.getOrderItems().get(0).getOptionId())
////                .customerName(resultDto.getPayerName())
////                .customerEmail(resultDto.getPayerEmail())
////                .customerMobilePhone(resultDto.getPayerHp())
////                .build();
////
////            payment.markAsPaid(
////                resultDto.getPayKey(),
////                resultDto.getPayTid(),
////                resultDto.toMap()
////            );
////
////            Payment savedPayment = paymentService.save(payment);
////
////            // 4. Order 상태 업데이트
////            order.completePayment();
////            orderService.save(order);
////
////            // 5. Purchase 생성
////            Purchase purchase = Purchase.createFromOrder(order);
////            purchase.complete();
////            purchaseService.save(purchase);
////
////            // 6. 쿠폰 사용 처리 (Order.completePayment()에서 처리됨)
////
////            return ResponseEntity.ok(
////                PaymentCompleteResponse.builder()
////                    .orderId(order.getMerchantUid())
////                    .status("SUCCESS")
////                    .message("결제가 완료되었습니다.")
////                    .purchaseId(purchase.getId())
////                    .build()
////            );
////        } else {
////            // 결제 실패 처리
////            order.failOrder("결제 실패: " + paypleResponse.getMessage());
////            orderService.save(order);
////
////            return ResponseEntity.ok(
////                PaymentCompleteResponse.builder()
////                    .orderId(order.getMerchantUid())
////                    .status("FAILED")
////                    .message(paypleResponse.getMessage())
////                    .build()
////            );
////        }
////    }
////
////    @Operation(summary = "결제 취소", description = "결제 취소 요청")
////    @PostMapping("/cancel")
////    public ResponseEntity<PaymentCancelResponse> cancelPayment(
////            @CurrentUser Long userId,
////            @RequestBody PaymentCancelRequest request) {
////
////        // 1. Order 조회
////        Order order = orderService.findByMerchantUid(request.getOrderId());
////
////        // 권한 확인
////        if (!order.getUser().getId().equals(userId)) {
////            throw new UnauthorizedException("결제 취소 권한이 없습니다.");
////        }
////
////        // 2. PayplePayment 취소
////        PaymentCancelResponseDto cancelResponse = payplePaymentService.cancelPayment(
////            request.getOrderId(),
////            request.getReason()
////        );
////
////        // 3. Payment 취소 처리
////        Payment payment = order.getPayment();
////        payment.cancel(request.getReason(), payment.getAmount());
////        paymentService.save(payment);
////
////        // 4. Order 취소 처리 (쿠폰 사용 취소 포함)
////        order.cancelOrder(request.getReason());
////        orderService.save(order);
////
////        // 5. Purchase 취소 처리
////        Purchase purchase = purchaseService.findByOrder(order);
////        purchase.cancel(request.getReason());
////        purchaseService.save(purchase);
////
////        return ResponseEntity.ok(
////            PaymentCancelResponse.builder()
////                .orderId(order.getMerchantUid())
////                .status("CANCELLED")
////                .message("결제가 취소되었습니다.")
////                .cancelledAt(LocalDateTime.now())
////                .build()
////        );
////    }
//// }
