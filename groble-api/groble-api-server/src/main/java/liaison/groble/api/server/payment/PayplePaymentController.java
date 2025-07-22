package liaison.groble.api.server.payment;

import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/payments/payple")
@RequiredArgsConstructor
@Tag(
    name = "[💰 페이플 결제] 앱카드 결제 진행 및 결제 취소 기능 API",
    description = "앱카드를 활용하여 결제를 진행하고, 결제 취소 기능을 제공합니다.")
public class PayplePaymentController {

  private static final String PAYMENT_CANCEL_PATH = "/{merchantUid}/cancel";

  // 응답 메시지 상수화
  private static final String APP_CARD_SUCCESS_MESSAGE = "페이플 앱카드 결제가 성공적으로 완료되었습니다.";

  // Mapper
  private final PaymentMapper paymentMapper;

  // Service
  private final PayplePaymentService payplePaymentService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[❌ 앱카드 결제 승인] 페이플 앱카드 결제를 진행합니다.",
      description =
          """
          앱카드 결제 인증 결과를 수신하고, Payple 서버에 승인 요청을 보냅니다.

          **주의사항:**
          - 인증 실패 시 400 에러가 발생합니다
          - 결제창이 닫힌 경우 빈 응답을 반환합니다
          - 결제 승인은 비동기로 처리되며, 완료 시 이벤트가 발행됩니다
          """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "결제 승인 요청 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AppCardPayplePaymentResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (인증 실패, 금액 불일치 등)",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "권한 없음 (다른 사용자의 주문)",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "충돌 (이미 처리된 주문)",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  @Logging(
      item = "Payment",
      action = "requestAppCardPayment",
      includeParam = true,
      includeResult = true)
  @PostMapping("/app-card/request")
  public ResponseEntity<GrobleResponse<AppCardPayplePaymentResponse>> requestAppCardPayment(
      @Auth Accessor accessor, @Valid @RequestBody PaypleAuthResultRequest request) {
    log.info(
        "앱카드 결제 승인 요청 - userId: {}, merchantUid: {}", accessor.getUserId(), request.getPayOid());

    PaypleAuthResultDTO authResultDTO = paymentMapper.toPaypleAuthResultDTO(request);

    payplePaymentService.saveAppCardAuthResponse(accessor.getUserId(), authResultDTO);

    try {
      // 인증 성공에 대한 결제 승인 요청 처리
      JSONObject approvalResult = payplePaymentService.processAppCardApproval(authResultDTO);

      // 승인 결과 확인
      String payRst = (String) approvalResult.get("PCD_PAY_RST");
      if (!"success".equalsIgnoreCase(payRst)) {
        String errorMsg = (String) approvalResult.get("PCD_PAY_MSG");
        throw new PayplePaymentAuthException("페이플 결제 승인 실패: " + errorMsg);
      }

      // 승인 성공 응답 생성
      AppCardPayplePaymentResponse response = buildPaymentResponse(approvalResult);
      return responseHelper.success(response, APP_CARD_SUCCESS_MESSAGE, HttpStatus.OK);
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

  private AppCardPayplePaymentResponse buildPaymentResponse(JSONObject approvalResult) {
    return AppCardPayplePaymentResponse.builder()
        .payRst((String) approvalResult.get("PCD_PAY_RST"))
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
  }
}
