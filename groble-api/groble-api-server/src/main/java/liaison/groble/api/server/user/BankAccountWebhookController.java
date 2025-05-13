package liaison.groble.api.server.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.user.service.PortOneBankVerificationService;
import liaison.groble.common.response.GrobleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/webhook/bank-account")
@RequiredArgsConstructor
public class BankAccountWebhookController {

  private final PortOneBankVerificationService bankVerificationService;

  @PostMapping
  public ResponseEntity<GrobleResponse<Void>> handleBankAccountWebhook(
      @RequestBody Map<String, Object> webhookData) {
    bankVerificationService.handleWebhook(webhookData);
    return ResponseEntity.ok().build();
  }
}
