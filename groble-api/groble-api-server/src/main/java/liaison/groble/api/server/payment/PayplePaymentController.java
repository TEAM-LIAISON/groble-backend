package liaison.groble.api.server.payment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleLinkResendRequest;
import liaison.groble.api.model.payment.response.PaymentCancelResponse;
import liaison.groble.api.server.payment.mapper.PayplePaymentMapper;
import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResponseDto;
import liaison.groble.application.payment.dto.PaypleAuthResultDto;
import liaison.groble.application.payment.dto.link.PaypleLinkResendResponse;
import liaison.groble.application.payment.dto.link.PaypleLinkResponse;
import liaison.groble.application.payment.dto.link.PaypleLinkResponseDto;
import liaison.groble.application.payment.dto.link.PaypleLinkStatusResponse;
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
  private final PayplePaymentMapper payplePaymentMapper;

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
      summary = "페이플 링크 결제 요청",
      description = "링크 결제를 요청하고 결제 링크를 받아옵니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "링크 생성 성공",
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
                      "linkRst": "success",
                      "linkMsg": "링크가 생성되었습니다.",
                      "linkKey": "LINK_1234567890",
                      "linkUrl": "https://link.payple.kr/pay/LINK_1234567890",
                      "linkOid": "ORDER_1234",
                      "linkGoods": "상품명",
                      "linkTotal": "10000",
                      "linkTime": "20250605123045",
                      "linkExpire": "20250612123045"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/link-payment/{merchantUid}")
  public ResponseEntity<GrobleResponse<PaypleLinkResponse>> requestPaypleLinkPayment(
      @Auth Accessor accessor, @Valid @PathVariable("merchantUid") String merchantUid) {

    try {
      // 1. 파트너 인증 요청
      PaypleAuthResponseDto paypleAuthResponseDto = payplePaymentService.getPaymentAuth("LINKREG");

      PaypleLinkResponseDto paypleLinkResponseDto =
          payplePaymentService.processLinkPayment(merchantUid, paypleAuthResponseDto);

      // 3. 응답 매핑
      PaypleLinkResponse paypleLinkResponse =
          payplePaymentMapper.toPaypleLinkResponse(paypleLinkResponseDto);

      return ResponseEntity.ok(GrobleResponse.success(paypleLinkResponse));

    } catch (IllegalArgumentException e) {
      log.error("링크 결제 요청 실패 - 잘못된 요청: {}", e.getMessage());
      throw new PayplePaymentAuthException("잘못된 요청: " + e.getMessage());
    } catch (IllegalStateException e) {
      log.error("링크 결제 요청 실패 - 상태 오류: {}", e.getMessage());
      throw new PayplePaymentAuthException("요청 처리 불가: " + e.getMessage());
    } catch (Exception e) {
      log.error("링크 결제 요청 중 오류 발생", e);
      throw new PayplePaymentAuthException("링크 결제 요청 처리 중 오류가 발생했습니다.");
    }
  }

  @Operation(
      summary = "링크 결제 완료 콜백",
      description = "페이플에서 링크 결제 완료 후 호출하는 콜백 엔드포인트입니다.",
      responses = {
        @ApiResponse(responseCode = "200", description = "콜백 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/link-payment/complete")
  public ResponseEntity<GrobleResponse<String>> completeLinkPayment(
      @RequestBody PaypleAuthResultDto resultDto) {

    log.info("링크 결제 완료 콜백 수신 - 주문번호: {}, 결과: {}", resultDto.getPayOid(), resultDto.getPayRst());

    try {
      // 결제 실패 처리
      if (resultDto.isError() || resultDto.isClosed()) {
        payplePaymentService.handleLinkPaymentFailure(resultDto);
        return ResponseEntity.ok(GrobleResponse.success("FAILED"));
      }

      // 결제 성공 처리
      payplePaymentService.handleLinkPaymentSuccess(resultDto);
      return ResponseEntity.ok(GrobleResponse.success("SUCCESS"));

    } catch (Exception e) {
      log.error("링크 결제 콜백 처리 중 오류 발생", e);
      return ResponseEntity.ok(GrobleResponse.success("ERROR"));
    }
  }

  @Operation(
      summary = "링크 결제 상태 조회",
      description = "생성된 링크 결제의 현재 상태를 조회합니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "상태 조회 성공",
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
                      "merchantUid": "ORDER_1234",
                      "status": "LINK_CREATED",
                      "linkUrl": "https://link.payple.kr/pay/LINK_1234567890",
                      "createdAt": "2025-06-05T12:30:45",
                      "expireAt": "2025-06-12T12:30:45",
                      "paymentStatus": null
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "404", description = "링크 결제 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
      })
  @GetMapping("/link-payment/{merchantUid}/status")
  public ResponseEntity<GrobleResponse<PaypleLinkStatusResponse>> getLinkPaymentStatus(
      @Auth Accessor accessor, @PathVariable String merchantUid) {

    log.info("링크 결제 상태 조회 - merchantUid: {}, userId: {}", merchantUid, accessor.getUserId());

    try {
      PaypleLinkStatusResponse status =
          payplePaymentService.getLinkPaymentStatus(merchantUid, accessor.getUserId());
      return ResponseEntity.ok(GrobleResponse.success(status));
    } catch (IllegalArgumentException e) {
      log.error("링크 결제 상태 조회 실패 - 결제 정보 없음: {}", merchantUid);
      throw new PayplePaymentAuthException("링크 결제 정보를 찾을 수 없습니다: " + merchantUid);
    } catch (Exception e) {
      log.error("링크 결제 상태 조회 중 오류 발생", e);
      throw new PayplePaymentAuthException("상태 조회 중 오류가 발생했습니다.");
    }
  }

  @Operation(
      summary = "링크 결제 재전송",
      description = "기존에 생성된 링크 결제를 재전송합니다. (SMS, 이메일 등)",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "재전송 성공",
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
                      "merchantUid": "ORDER_1234",
                      "linkUrl": "https://link.payple.kr/pay/LINK_1234567890",
                      "sentAt": "2025-06-08T15:30:45",
                      "method": "SMS"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "404", description = "링크 결제 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "재전송 불가능한 상태"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
      })
  @PostMapping("/link-payment/{merchantUid}/resend")
  public ResponseEntity<GrobleResponse<PaypleLinkResendResponse>> resendLinkPayment(
      @Auth Accessor accessor,
      @PathVariable String merchantUid,
      @RequestBody PaypleLinkResendRequest request) {

    log.info(
        "링크 결제 재전송 요청 - merchantUid: {}, method: {}, userId: {}",
        merchantUid,
        request.getMethod(),
        accessor.getUserId());

    try {
      PaypleLinkResendResponse response =
          payplePaymentService.resendLinkPayment(
              merchantUid, accessor.getUserId(), request.getMethod());

      return ResponseEntity.ok(GrobleResponse.success(response));
    } catch (IllegalArgumentException e) {
      log.error("링크 결제 재전송 실패 - 결제 정보 없음: {}", merchantUid);
      throw new PayplePaymentAuthException("링크 결제 정보를 찾을 수 없습니다: " + merchantUid);
    } catch (IllegalStateException e) {
      log.error("링크 결제 재전송 실패 - 재전송 불가 상태: {}", e.getMessage());
      throw new PayplePaymentAuthException("재전송 불가능한 상태입니다: " + e.getMessage());
    } catch (Exception e) {
      log.error("링크 결제 재전송 중 오류 발생", e);
      throw new PayplePaymentAuthException("재전송 처리 중 오류가 발생했습니다.");
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
  @PostMapping("/{merchantUid}/cancel")
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @Auth Accessor accessor,
      @PathVariable String merchantUid,
      @Valid @RequestBody PaymentCancelRequest request) {

    log.info(
        "결제 취소 요청 - 주문번호: {}, 사유: {}, userId: {}",
        merchantUid,
        request.getReason(),
        accessor.getUserId());

    try {
      // 결제 취소 처리
      payplePaymentService.cancelPayment(merchantUid, request.getReason());

      // 취소 성공 응답 생성
      PaymentCancelResponse response =
          PaymentCancelResponse.builder()
              .merchantUid(merchantUid)
              .status("CANCELLED")
              .canceledAt(LocalDateTime.now())
              .cancelReason(request.getReason())
              .build();

      log.info("결제 취소 완료 - 주문번호: {}", merchantUid);
      return ResponseEntity.ok(GrobleResponse.success(response));

    } catch (IllegalArgumentException e) {
      log.error("결제 취소 실패 - 주문을 찾을 수 없음: {}", merchantUid, e);
      throw new PayplePaymentAuthException("주문을 찾을 수 없습니다: " + merchantUid);
    } catch (IllegalStateException e) {
      log.error("결제 취소 실패 - 취소할 수 없는 상태: {}", merchantUid, e);
      throw new PayplePaymentAuthException("취소할 수 없는 상태입니다: " + e.getMessage());
    } catch (RuntimeException e) {
      log.error("결제 취소 실패 - 환불 처리 오류: {}", merchantUid, e);
      throw new PayplePaymentAuthException("환불 처리 중 오류가 발생했습니다: " + e.getMessage());
    } catch (Exception e) {
      log.error("결제 취소 중 예상치 못한 오류 발생: {}", merchantUid, e);
      throw new PayplePaymentAuthException("결제 취소 처리 중 오류가 발생했습니다.");
    }
  }

  @Operation(
      summary = "빌링 카드 등록",
      description = "정기결제를 위한 빌링 카드를 등록합니다. 등록 성공 시 빌링키가 발급됩니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "카드 등록 성공",
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
                      "payMsg": "빌링키 등록이 완료되었습니다.",
                      "payerId": "PAYER_1234567890",
                      "cardName": "Samsung Card",
                      "cardNum": "1234-****-****-5678",
                      "registeredAt": "2025-06-05T12:30:45"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/billing/register")
  public ResponseEntity<GrobleResponse<Map<String, Object>>> registerBillingCard(
      @Auth Accessor accessor, @Valid @RequestBody PaypleAuthResultDto authResultDto) {

    log.info(
        "빌링 카드 등록 요청 - userId: {}, 결과: {}, 코드: {}, 메시지: {}",
        accessor.getUserId(),
        authResultDto.getPayRst(),
        authResultDto.getPayCode(),
        authResultDto.getPayMsg());

    if (authResultDto.isError()) {
      log.error(
          "빌링 카드 등록 실패 - 코드: {}, 메시지: {}", authResultDto.getPayCode(), authResultDto.getPayMsg());
      throw new PayplePaymentAuthException("빌링 카드 등록 실패: " + authResultDto.getPayMsg());
    }

    if (authResultDto.isClosed()) {
      log.warn("빌링 카드 등록 취소 - 사용자가 결제창을 닫음");
      Map<String, Object> response = new HashMap<>();
      response.put("status", "cancelled");
      response.put("message", "카드 등록이 취소되었습니다.");
      return ResponseEntity.ok(GrobleResponse.success(response));
    }

    try {
      // 빌링 카드 등록 처리
      Map<String, Object> result =
          payplePaymentService.registerBillingCard(accessor.getUserId(), authResultDto);

      log.info(
          "빌링 카드 등록 성공 - userId: {}, payerId: {}", accessor.getUserId(), result.get("payerId"));
      return ResponseEntity.ok(GrobleResponse.success(result));

    } catch (Exception e) {
      log.error("빌링 카드 등록 중 오류 발생", e);
      throw new PayplePaymentAuthException("빌링 카드 등록 처리 중 오류가 발생했습니다.");
    }
  }

  @Operation(
      summary = "빌링 결제 실행",
      description = "등록된 빌링키로 정기결제를 실행합니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 성공",
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
                      "payCardAuthNo": "12345678",
                      "payCardReceipt": "https://receipt.payple.kr/receipt/abcd1234"
                    }
                  }
                  """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "빌링키를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
      })
  @PostMapping("/billing/payment/{merchantUid}")
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> executeBillingPayment(
      @Auth Accessor accessor, @PathVariable String merchantUid) {

    log.info("빌링 결제 실행 요청 - merchantUid: {}, userId: {}", merchantUid, accessor.getUserId());

    try {
      // 빌링 결제 실행
      JSONObject paymentResult =
          payplePaymentService.executeBillingPayment(merchantUid, accessor.getUserId());

      // 결제 결과 확인
      String payRst = (String) paymentResult.get("PCD_PAY_RST");
      if (!"success".equalsIgnoreCase(payRst)) {
        String errorMsg = (String) paymentResult.get("PCD_PAY_MSG");
        log.error("빌링 결제 실패 - 메시지: {}", errorMsg);
        throw new PayplePaymentAuthException("빌링 결제 실패: " + errorMsg);
      }

      // 결제 성공 응답 생성
      AppCardPayplePaymentResponse response =
          AppCardPayplePaymentResponse.builder()
              .payRst(payRst)
              .payCode((String) paymentResult.get("PCD_PAY_CODE"))
              .payMsg((String) paymentResult.get("PCD_PAY_MSG"))
              .payOid((String) paymentResult.get("PCD_PAY_OID"))
              .payType((String) paymentResult.get("PCD_PAY_TYPE"))
              .payTime((String) paymentResult.get("PCD_PAY_TIME"))
              .payTotal((String) paymentResult.get("PCD_PAY_TOTAL"))
              .payCardName((String) paymentResult.get("PCD_PAY_CARDNAME"))
              .payCardNum((String) paymentResult.get("PCD_PAY_CARDNUM"))
              .payCardQuota((String) paymentResult.get("PCD_PAY_CARDQUOTA"))
              .payCardTradeNum((String) paymentResult.get("PCD_PAY_CARDTRADENUM"))
              .payCardAuthNo((String) paymentResult.get("PCD_PAY_CARDAUTHNO"))
              .payCardReceipt((String) paymentResult.get("PCD_CARD_RECEIPT"))
              .build();

      log.info("빌링 결제 성공 - orderId: {}", response.getPayOid());
      return ResponseEntity.ok(GrobleResponse.success(response));

    } catch (IllegalArgumentException e) {
      log.error("빌링 결제 실패 - 주문 또는 빌링키를 찾을 수 없음: {}", merchantUid);
      throw new PayplePaymentAuthException("주문 또는 빌링키를 찾을 수 없습니다: " + merchantUid);
    } catch (IllegalStateException e) {
      log.error("빌링 결제 실패 - 결제할 수 없는 상태: {}", e.getMessage());
      throw new PayplePaymentAuthException("결제할 수 없는 상태입니다: " + e.getMessage());
    } catch (Exception e) {
      log.error("빌링 결제 처리 중 오류 발생", e);
      throw new PayplePaymentAuthException("빌링 결제 처리 중 오류가 발생했습니다.");
    }
  }
}
