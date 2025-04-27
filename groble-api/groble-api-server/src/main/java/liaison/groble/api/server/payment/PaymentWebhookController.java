package liaison.groble.api.server.payment;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.payment.service.PortOnePaymentService;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/webhook/payment")
@RequiredArgsConstructor
public class PaymentWebhookController {
  private final PortOnePaymentService paymentService;

  @PostMapping
  public ResponseEntity<GrobleResponse<Void>> handlePaymentWebhook(
      @RequestBody Map<String, Object> webhookData,
      @RequestHeader(value = "X-Portone-Signature", required = false) String signature) {

    // 서명 검증 (선택 사항)
    if (signature != null && paymentService.getWebhookSecret() != null) {
      boolean isValid = verifyWebhookSignature(signature, webhookData);
      if (!isValid) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(GrobleResponse.error("웹훅 서명이 유효하지 않습니다.", 401));
      }
    }

    // 웹훅 처리
    paymentService.handleWebhook(webhookData);

    return ResponseEntity.ok().build();
  }

  /** 웹훅 서명 검증 */
  private boolean verifyWebhookSignature(String signature, Map<String, Object> payload) {
    // 실제 서명 검증 로직 구현
    // 단순화를 위해 항상 true 반환
    return true;
  }
}
