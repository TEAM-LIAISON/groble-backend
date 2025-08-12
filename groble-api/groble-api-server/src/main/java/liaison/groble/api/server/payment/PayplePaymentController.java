package liaison.groble.api.server.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.api.model.payment.request.PaypleAuthResultRequest;
import liaison.groble.application.payment.dto.AppCardPayplePaymentResponse;
import liaison.groble.application.payment.dto.PaypleAuthResultDTO;
import liaison.groble.application.payment.dto.cancel.PaymentCancelResponse;
import liaison.groble.application.payment.service.PayplePaymentFacade;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.payment.PaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
  // Facade
  private final PayplePaymentFacade payplePaymentFacade;

  // Mapper
  private final PaymentMapper paymentMapper;

  @Operation(
      summary = "[✅ 앱카드 결제 승인] 페이플 앱카드 결제를 진행합니다.",
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
    log.info("앱카드 결제 요청 - userId: {}, merchantUid: {}", accessor.getUserId(), request.getPayOid());

    PaypleAuthResultDTO authResultDTO = paymentMapper.toPaypleAuthResultDTO(request);

    AppCardPayplePaymentResponse response =
        payplePaymentFacade.processAppCardPayment(accessor.getUserId(), authResultDTO);

    return ResponseEntity.ok(GrobleResponse.success(response));
  }

  @Operation(
      summary = "[❌ 결제 취소] 결제를 취소합니다.",
      description =
          """
          완료된 결제를 취소하고 환불 처리합니다.

          **취소 가능 조건:**
          - 주문 상태가 CANCEL_REQUEST인 경우만 가능
          - 본인의 주문만 취소 가능

          **처리 과정:**
          1. 주문 및 결제 정보 검증
          2. 페이플 환불 API 호출
          3. 성공 시 주문/결제/구매 상태 업데이트
          4. 환불 완료 이벤트 발행
          """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "결제 취소 성공",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentCancelResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "권한 없음",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "주문을 찾을 수 없음",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "취소 불가능한 상태",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class)))
  })
  @Logging(item = "Payment", action = "CancelPayment", includeParam = true, includeResult = true)
  @PostMapping("/{merchantUid}/cancel")
  public ResponseEntity<GrobleResponse<PaymentCancelResponse>> cancelPayment(
      @Auth Accessor accessor,
      @Parameter(description = "주문번호", required = true, example = "ORDER-20240101-000001")
          @PathVariable
          @NotBlank
          String merchantUid,
      @Valid @RequestBody PaymentCancelRequest request) {

    log.info(
        "결제 취소 요청 - userId: {}, merchantUid: {}, reason: {}",
        accessor.getUserId(),
        merchantUid,
        request.getDetailReason());

    PaymentCancelResponse response =
        payplePaymentFacade.cancelPayment(
            accessor.getUserId(), merchantUid, request.getDetailReason());

    return ResponseEntity.ok(GrobleResponse.success(response));
  }
}
