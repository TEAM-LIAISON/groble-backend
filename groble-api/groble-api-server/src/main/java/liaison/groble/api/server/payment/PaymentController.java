package liaison.groble.api.server.payment;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.payment.request.PaymentCancelRequest;
import liaison.groble.application.payment.dto.PaymentCancelDTO;
import liaison.groble.application.payment.service.PaymentService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.payment.PaymentMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(
    name = "[💰 결제 취소 요청] 구매자가 코칭 상품에 한해 결제 취소 요청을 할 수 있는 API",
    description = "구매자에게 결제 취소 요청 기능을 제공합니다")
public class PaymentController {

  // API 경로 상수화
  private static final String PAYMENT_CANCEL_PATH = "/{merchantUid}/cancel/request";

  // 응답 메시지 상수화
  private static final String PAYMENT_CANCEL_SUCCESS_MESSAGE = "결제 취소 요청이 성공적으로 처리되었습니다.";

  // Mapper
  private final PaymentMapper paymentMapper;

  // Service
  private final PaymentService paymentService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(summary = "구매자의 결제 취소 요청", description = "콘텐츠(코칭) 구매자가 결제 취소를 요청합니다.")
  @Logging(item = "Payment", action = "Cancel", includeParam = true, includeResult = true)
  @PostMapping(PAYMENT_CANCEL_PATH)
  public ResponseEntity<GrobleResponse<Void>> requestPaymentCancel(
      @Auth Accessor accessor,
      @Valid @PathVariable("merchantUid") String merchantUid,
      @Valid @RequestBody PaymentCancelRequest request) {
    PaymentCancelDTO paymentCancelDTO = paymentMapper.toPaymentCancelDTO(request);

    // 결제 취소 요청 처리
    paymentService.requestPaymentCancel(accessor.getUserId(), merchantUid, paymentCancelDTO);

    // 성공 응답 반환
    return responseHelper.success(null, PAYMENT_CANCEL_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
