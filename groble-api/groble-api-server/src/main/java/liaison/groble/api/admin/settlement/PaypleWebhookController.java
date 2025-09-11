package liaison.groble.api.admin.settlement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.admin.settlement.dto.PaypleWebhookRequest;
import liaison.groble.application.admin.settlement.service.PaypleWebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 웹훅 수신 컨트롤러
 *
 * <p>페이플에서 전송하는 이체 실행 결과 웹훅을 수신하여 처리합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks/payple")
@RequiredArgsConstructor
@Tag(name = "Payple Webhook", description = "페이플 웹훅 API")
public class PaypleWebhookController {

  private final PaypleWebhookService paypleWebhookService;

  /**
   * 페이플 이체 실행 결과 웹훅 수신
   *
   * @param webhookRequest 웹훅 요청 데이터
   * @return 처리 결과
   */
  @Operation(summary = "페이플 이체 결과 웹훅 수신", description = "페이플에서 전송하는 이체 실행 결과를 수신하여 정산 상태를 업데이트합니다.")
  @PostMapping("/transfer-result")
  public ResponseEntity<String> receiveTransferResult(
      @RequestBody PaypleWebhookRequest webhookRequest) {

    log.info(
        "페이플 이체 결과 웹훅 수신 - 결과: {}, 이체금액: {}원, API거래ID: {}",
        webhookRequest.getResult(),
        webhookRequest.getTranAmt(),
        webhookRequest.getApiTranId() != null && webhookRequest.getApiTranId().length() > 8
            ? webhookRequest.getApiTranId().substring(0, 8) + "****"
            : "****");

    try {
      // 필수 파라미터 검증
      if (webhookRequest.getResult() == null || webhookRequest.getBillingTranId() == null) {
        log.warn(
            "필수 파라미터 누락 - result: {}, billing_tran_id: {}",
            webhookRequest.getResult(),
            webhookRequest.getBillingTranId() != null ? "존재" : "null");
        return ResponseEntity.ok("INVALID_PARAMETERS");
      }

      // 이체 결과 처리
      paypleWebhookService.processTransferResultWebhook(webhookRequest);

      log.info("페이플 이체 결과 웹훅 처리 완료");
      return ResponseEntity.ok("SUCCESS");

    } catch (Exception e) {
      log.error("페이플 이체 결과 웹훅 처리 실패", e);

      // 웹훅 처리 실패 시에도 200 OK를 반환하여 페이플에서 재시도하지 않도록 함
      // 실제로는 웹훅 재시도가 필요한 경우와 아닌 경우를 구분해서 처리해야 함
      return ResponseEntity.ok("ERROR");
    }
  }

  /**
   * 웹훅 검증용 엔드포인트 (선택사항)
   *
   * <p>페이플에서 웹훅 URL 검증을 위해 사용할 수 있습니다.
   */
  @Operation(summary = "웹훅 URL 검증", description = "페이플 웹훅 URL이 유효한지 검증합니다.")
  @PostMapping("/verify")
  public ResponseEntity<String> verifyWebhook() {
    log.info("페이플 웹훅 URL 검증 요청 수신");
    return ResponseEntity.ok("WEBHOOK_VERIFIED");
  }
}
