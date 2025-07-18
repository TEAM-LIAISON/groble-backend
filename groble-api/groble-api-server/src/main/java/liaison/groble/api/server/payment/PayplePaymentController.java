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
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.api.model.payment.response.PaymentCancelResponse;
import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.exception.PayplePaymentAuthException;
import liaison.groble.application.payment.service.PayplePaymentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.payment.PaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/payple")
@RequiredArgsConstructor
@Tag(
    name = "[💰 페이플 결제] 앱카드 결제 진행 및 결제 취소 기능 API",
    description = "앱카드를 활용하여 결제를 진행하고, 결제 취소 기능을 제공합니다.")
public class PayplePaymentController {

  // API 경로 상수화
  private static final String APP_CARD_REQUEST_PATH = "/app-card/request";
  private static final String PAYMENT_CANCEL_PATH = "/{merchantUid}/cancel";

  // 응답 메시지 상수화
  private static final String APP_CARD_SUCCESS_MESSAGE = "페이플 앱카드 결제가 성공적으로 완료되었습니다.";

  // Mapper
  private final PaymentMapper paymentMapper;

  // Service
  private final PayplePaymentService payplePaymentService;

  // Helper
  private final ResponseHelper responseHelper;

  // 앱카드 결제 인증 결과를 수신하고 결제 승인 요청을 페이플 서버에 보낸다.
  @Operation(
      summary = "[❌ 앱카드 결제 승인] 페이플 앱카드 결제를 진행합니다.",
      description = "앱카드 결제 인증 결과를 수신하고, Payple 서버에 승인 요청을 보냅니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AppCardPayplePaymentResponse.class)))
      })
  @Logging(
      item = "Payment",
      action = "AppCardPaymentRequest",
      includeParam = true,
      includeResult = true)
  @PostMapping(APP_CARD_REQUEST_PATH)
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> requestAppCardPayment(
      @Auth Accessor accessor,
      @Valid @RequestBody PaypleAuthResultRequest paypleAuthResultRequest) {

    log.info(
        "페이플 인증 결과 수신 - 결과: {}, 코드: {}, 메시지: {}, 주문번호: {}",
        paypleAuthResultRequest.getPayRst(),
        paypleAuthResultRequest.getPayCode(),
        paypleAuthResultRequest.getPayMsg(),
        paypleAuthResultRequest.getPayOid());

    if (paypleAuthResultRequest.isError()) {
      log.error(
          "페이플 인증 실패 - 코드: {}, 메시지: {}",
          paypleAuthResultRequest.getPayCode(),
          paypleAuthResultRequest.getPayMsg());
      throw new PayplePaymentAuthException("페이플 인증 실패: " + paypleAuthResultRequest.getPayMsg());
    }

    if (paypleAuthResultRequest.isClosed()) {
      log.warn("페이플 인증 취소 - 사용자가 결제창을 닫음");
      return ResponseEntity.ok(
          GrobleResponse.success(AppCardPayplePaymentResponse.builder().build()));
    }

    PaypleAuthResultDTO authResultDTO =
        paymentMapper.toPaypleAuthResultDTO(paypleAuthResultRequest);

    payplePaymentService.saveAppCardAuthResponse(accessor.getUserId(), authResultDTO);

    try {
      // 인증 성공에 대한 결제 승인 요청 처리
      JSONObject approvalResult = payplePaymentService.processAppCardApproval(authResultDTO);

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
      summary = "[❌ 결제 취소] 결제를 취소합니다.",
      description = "결제를 취소하고 환불 처리합니다.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 취소 성공",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PaymentCancelResponse.class)))
      })
  @PostMapping(PAYMENT_CANCEL_PATH)
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @Auth Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid,
      @Valid @RequestBody PaymentCancelRequest request) {

    try {
      PaypleAuthResponseDTO paypleAuthResponseDTO = payplePaymentService.getPaymentAuthForCancel();

      // 결제 취소 처리
      JSONObject approvalResult =
          payplePaymentService.cancelPayment(
              paypleAuthResponseDTO, merchantUid, request.getDetailReason());

      // 취소 성공 응답 생성
      PaymentCancelResponse response =
          PaymentCancelResponse.builder()
              .merchantUid(merchantUid)
              .status("CANCELLED")
              .canceledAt(LocalDateTime.now())
              .cancelReason(request.getDetailReason())
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
}
