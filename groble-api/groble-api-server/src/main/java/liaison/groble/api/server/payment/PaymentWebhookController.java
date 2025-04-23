package liaison.groble.api.server.payment;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
      @RequestBody Map<String, Object> webhookData) {
    paymentService.handleWebhook(webhookData);
    return ResponseEntity.ok().build();
  }
}
