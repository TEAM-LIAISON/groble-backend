package liaison.groble.api.server.payment;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PayplePaymentLinkRequest;
import liaison.groble.api.model.payment.response.PaypleLinkResponse;
import liaison.groble.api.server.payment.mapper.PayplePaymentMapper;
import liaison.groble.application.order.OrderService;
import liaison.groble.application.payment.dto.PaymentCompleteResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.application.payment.dto.PaypleLinkResponseDto;
import liaison.groble.application.payment.dto.PayplePaymentLinkRequestDto;
import liaison.groble.application.payment.service.PayplePaymentService;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
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
  private final OrderService orderService;
  private final PayplePaymentMapper payplePaymentMapper;

  // 앱카드 결제 인증 결과를 수신하고 승인 요청을 페이플에 보낸다.
  @Operation(summary = "페이플 인증 결과 수신 및 승인 요청", description = "페이플 인증 결과를 수신하고 승인 요청을 처리합니다.")
  @PostMapping("/auth-result")
  public ResponseEntity<GrobleResponse<PaymentCompleteResponseDto>> receivePaypleAuthResult(
      @Valid @RequestBody PaypleAuthResultDto authResult) {
    log.info(
        "페이플 인증 결과 수신 - 결과: {}, 코드: {}, 메시지: {}, 주문번호: {}",
        authResult.getPayRst(),
        authResult.getPayCode(),
        authResult.getPayMsg(),
        authResult.getPayOid());

    // 인증 결과 검증
    if (authResult.isError()) {
      log.error("페이플 인증 실패 - 코드: {}, 메시지: {}", authResult.getPayCode(), authResult.getPayMsg());
      throw new RuntimeException("페이플 인증 실패: " + authResult.getPayMsg());
    }

    if (authResult.isClosed()) {
      log.warn("페이플 인증 취소 - 사용자가 결제창을 닫음");
      return ResponseEntity.ok(
          GrobleResponse.success(PaymentCompleteResponseDto.builder().status("CANCELLED").build()));
    }

    // 인증 성공 시 승인 요청 처리
    PaymentCompleteResponseDto response = payplePaymentService.processAuthResult(authResult);

    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  // 링크 결제 요청이 들어온다
  // 1. 파트너 인증 요청을 페이플 서버에 보낸다
  // 2. 인증 결과를 받아온다.
  // 3. 해당 인증 결과를 바탕으로 링크 생성 요청을 페이플 서버에 보낸다.
  // 4. 링크 생성 결과를 받아온다.
  // 5. 링크 생성 결과를 클라이언트에 반환한다.

  @Operation(summary = "페이플 링크 결제 요청", description = "링크 결제를 요청하고 결제 링크를 받아옵니다.")
  @PostMapping("/link-payment")
  public ResponseEntity<GrobleResponse<PaypleLinkResponse>> requestPaypleLinkPayment(
      @Valid @RequestBody PayplePaymentLinkRequest payplePaymentLinkRequest) {
    PayplePaymentLinkRequestDto payplePaymentLinkRequestDto =
        payplePaymentMapper.toPayplePaymentLinkRequestDto(payplePaymentLinkRequest);
    PaypleAuthResponseDto paypleAuthResponseDto = payplePaymentService.getPaymentAuth("LINKREG");
    log.info(
        "페이플 링크 결제 요청 - authKey: {}, clientKey: {}, returnUrl: {}",
        paypleAuthResponseDto.getAuthKey(),
        paypleAuthResponseDto.getClientKey(),
        paypleAuthResponseDto.getReturnUrl());
    PaypleLinkResponseDto paypleLinkResponseDto =
        payplePaymentService.processLinkPayment(payplePaymentLinkRequestDto, paypleAuthResponseDto);
    PaypleLinkResponse paypleLinkResponse =
        payplePaymentMapper.toPaypleLinkResponse(paypleLinkResponseDto);
    return ResponseEntity.ok(GrobleResponse.success(paypleLinkResponse));
  }
}

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
//
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import liaison.groble.api.model.payment.request.PaymentRequest;
// import liaison.groble.api.model.payment.response.PaymentCompleteResponse;
// import liaison.groble.api.model.payment.response.PaymentRequestResponse;
// import liaison.groble.application.payment.dto.PayplePaymentResultDto;
// import liaison.groble.application.payment.service.PayplePaymentService;
// import liaison.groble.security.annotations.CurrentUser;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//

// * 페이플 결제 플로우 설명:
// *
// * 1. 사용자가 콘텐츠 구매 요청
// *    - 쿠폰 적용 여부 확인
// *    - 최종 결제 금액 계산
// *
// * 2. 주문(Order) 생성
// *    - Order 엔티티에 사용자, 콘텐츠, 쿠폰 정보 저장
// *    - 상태: PENDING
// *
// * 3. 결제 요청 (/api/v1/payments/payple/request)
// *    - PayplePayment 엔티티 생성 (PENDING 상태)
// *    - 페이플 인증 정보 반환
// *
// * 4. 프론트엔드에서 페이플 결제창 호출
// *    - 페이플 JS SDK를 사용하여 결제 진행
// *
// * 5. 결제 완료 콜백 (/api/v1/payments/payple/complete)
// *    - 페이플로부터 결제 결과 수신
// *    - PayplePayment 상태 업데이트 (COMPLETED/FAILED)
// *    - Order 상태 업데이트 (PAID/FAILED)
// *    - Payment 엔티티 생성 및 상태 업데이트
// *    - Purchase 엔티티 생성 (구매 완료)
// *    - 쿠폰 사용 처리
// *
// * 6. 결제 실패 시
// *    - 모든 엔티티 상태를 FAILED로 업데이트
// *    - 쿠폰 사용 취소
// *
// * 7. 결제 취소 요청 시
// *    - 페이플 환불 API 호출
// *    - 모든 엔티티 상태를 CANCELLED로 업데이트
// *    - 쿠폰 사용 취소
// */

//
//    private final PayplePaymentService payplePaymentService;
//    private final OrderService orderService;
//    private final PaymentService paymentService;
//    private final PurchaseService purchaseService;
//
//    @Operation(summary = "결제 요청", description = "페이플 결제를 위한 초기 요청")
//    @PostMapping("/request")
//    public ResponseEntity<PaymentRequestResponse> requestPayment(
//            @CurrentUser Long userId,
//            @RequestBody PaymentRequest request) {
//
//        // 2. PayplePayment 생성 (PENDING 상태)
//        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
//            .orderId(order.getMerchantUid())
//            .amount(order.getFinalAmount())
//            .payMethod(request.getPayMethod())
//            .productName(request.getProductName())
//            .build();
//
//        PaymentRequestResponseDto response = payplePaymentService.processPayment(userId,
// paymentRequestDto);
//
//        // 3. 페이플 인증 정보 조회
//        PaypleAuthResponseDto authInfo =
// payplePaymentService.getPaymentAuth(request.getPayWork());
//
//        return ResponseEntity.ok(
//            PaymentRequestResponse.builder()
//                .orderId(order.getMerchantUid())
//                .amount(order.getFinalAmount())
//                .authKey(authInfo.getAuthKey())
//                .clientKey(authInfo.getClientKey())
//                .returnUrl(authInfo.getReturnUrl())
//                .build()
//        );
//    }
//
//    @Operation(summary = "결제 완료 처리", description = "페이플 결제 완료 후 콜백 처리")
//    @PostMapping("/complete")
//    public ResponseEntity<PaymentCompleteResponse> completePayment(
//            @CurrentUser Long userId,
//            @RequestBody PayplePaymentResultDto resultDto) {
//
//        // 1. PayplePayment 상태 업데이트
//        PaymentCompleteResponseDto paypleResponse =
// payplePaymentService.completePayment(resultDto);
//
//        // 2. Order 조회
//        Order order = orderService.findByMerchantUid(resultDto.getPayOid());
//
//        if ("SUCCESS".equals(paypleResponse.getStatus())) {
//            // 3. Payment 엔티티 생성 및 저장
//            Payment payment = Payment.builder()
//                .order(order)
//                .paymentKey(resultDto.getPayKey())
//                .paymentMethod(PaymentMethod.valueOf(resultDto.getPayMethod()))
//                .amount(order.getFinalAmount())
//                .selectedOptionType(order.getOrderItems().get(0).getOptionType())
//                .selectedOptionId(order.getOrderItems().get(0).getOptionId())
//                .customerName(resultDto.getPayerName())
//                .customerEmail(resultDto.getPayerEmail())
//                .customerMobilePhone(resultDto.getPayerHp())
//                .build();
//
//            payment.markAsPaid(
//                resultDto.getPayKey(),
//                resultDto.getPayTid(),
//                resultDto.toMap()
//            );
//
//            Payment savedPayment = paymentService.save(payment);
//
//            // 4. Order 상태 업데이트
//            order.completePayment();
//            orderService.save(order);
//
//            // 5. Purchase 생성
//            Purchase purchase = Purchase.createFromOrder(order);
//            purchase.complete();
//            purchaseService.save(purchase);
//
//            // 6. 쿠폰 사용 처리 (Order.completePayment()에서 처리됨)
//
//            return ResponseEntity.ok(
//                PaymentCompleteResponse.builder()
//                    .orderId(order.getMerchantUid())
//                    .status("SUCCESS")
//                    .message("결제가 완료되었습니다.")
//                    .purchaseId(purchase.getId())
//                    .build()
//            );
//        } else {
//            // 결제 실패 처리
//            order.failOrder("결제 실패: " + paypleResponse.getMessage());
//            orderService.save(order);
//
//            return ResponseEntity.ok(
//                PaymentCompleteResponse.builder()
//                    .orderId(order.getMerchantUid())
//                    .status("FAILED")
//                    .message(paypleResponse.getMessage())
//                    .build()
//            );
//        }
//    }
//
//    @Operation(summary = "결제 취소", description = "결제 취소 요청")
//    @PostMapping("/cancel")
//    public ResponseEntity<PaymentCancelResponse> cancelPayment(
//            @CurrentUser Long userId,
//            @RequestBody PaymentCancelRequest request) {
//
//        // 1. Order 조회
//        Order order = orderService.findByMerchantUid(request.getOrderId());
//
//        // 권한 확인
//        if (!order.getUser().getId().equals(userId)) {
//            throw new UnauthorizedException("결제 취소 권한이 없습니다.");
//        }
//
//        // 2. PayplePayment 취소
//        PaymentCancelResponseDto cancelResponse = payplePaymentService.cancelPayment(
//            request.getOrderId(),
//            request.getReason()
//        );
//
//        // 3. Payment 취소 처리
//        Payment payment = order.getPayment();
//        payment.cancel(request.getReason(), payment.getAmount());
//        paymentService.save(payment);
//
//        // 4. Order 취소 처리 (쿠폰 사용 취소 포함)
//        order.cancelOrder(request.getReason());
//        orderService.save(order);
//
//        // 5. Purchase 취소 처리
//        Purchase purchase = purchaseService.findByOrder(order);
//        purchase.cancel(request.getReason());
//        purchaseService.save(purchase);
//
//        return ResponseEntity.ok(
//            PaymentCancelResponse.builder()
//                .orderId(order.getMerchantUid())
//                .status("CANCELLED")
//                .message("결제가 취소되었습니다.")
//                .cancelledAt(LocalDateTime.now())
//                .build()
//        );
//    }
// }
