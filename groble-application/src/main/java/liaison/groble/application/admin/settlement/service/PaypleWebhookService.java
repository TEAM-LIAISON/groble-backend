package liaison.groble.application.admin.settlement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.settlement.dto.PaypleWebhookRequest;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이플 웹훅 처리 서비스
 *
 * <p>페이플에서 전송하는 이체 실행 결과 웹훅을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaypleWebhookService {

  private final SettlementRepository settlementRepository;

  /**
   * 페이플 이체 실행 결과 웹훅 처리
   *
   * @param webhookRequest 웹훅 요청 데이터
   */
  @Transactional
  public void processTransferResultWebhook(PaypleWebhookRequest webhookRequest) {
    log.info(
        "페이플 이체 결과 웹훅 수신 - 그룹키: {}, 결과: {}, 이체금액: {}원",
        maskSensitiveData(webhookRequest.getGroupKey()),
        webhookRequest.getResult(),
        webhookRequest.getTranAmt());

    try {
      // 빌링키로 해당 정산 찾기
      List<Settlement> settlements =
          findSettlementsByBillingTranId(webhookRequest.getBillingTranId());

      if (settlements.isEmpty()) {
        log.warn(
            "빌링키에 해당하는 정산을 찾을 수 없습니다 - 빌링키: {}",
            maskSensitiveData(webhookRequest.getBillingTranId()));
        return;
      }

      // 각 정산에 대해 이체 결과 처리
      for (Settlement settlement : settlements) {
        processSettlementTransferResult(settlement, webhookRequest);
      }

      log.info("페이플 이체 결과 웹훅 처리 완료 - 처리된 정산 수: {}", settlements.size());

    } catch (Exception e) {
      log.error("페이플 이체 결과 웹훅 처리 중 오류 발생", e);
      // 웹훅 처리 실패 시에도 200 OK를 반환해야 페이플에서 재시도하지 않음
      throw new RuntimeException("웹훅 처리 실패", e);
    }
  }

  /**
   * 특정 정산의 이체 결과 처리
   *
   * @param settlement 정산 엔티티
   * @param webhookRequest 웹훅 요청 데이터
   */
  private void processSettlementTransferResult(
      Settlement settlement, PaypleWebhookRequest webhookRequest) {

    log.info(
        "정산 {} 이체 결과 처리 - 결과: {}, API거래ID: {}",
        settlement.getId(),
        webhookRequest.getResult(),
        maskSensitiveData(webhookRequest.getApiTranId()));

    if (webhookRequest.isSuccess()) {
      // 이체 성공 처리
      handleTransferSuccess(settlement, webhookRequest);
    } else {
      // 이체 실패 처리
      handleTransferFailure(settlement, webhookRequest);
    }
  }

  /**
   * 이체 성공 처리
   *
   * @param settlement 정산 엔티티
   * @param webhookRequest 웹훅 요청 데이터
   */
  private void handleTransferSuccess(Settlement settlement, PaypleWebhookRequest webhookRequest) {
    log.info("정산 {} 이체 성공 처리", settlement.getId());

    // 정산 상태를 완료로 업데이트
    settlement.completeSettlement();

    // 정산 항목들의 상태도 완료로 업데이트
    for (SettlementItem item : settlement.getSettlementItems()) {
      if (!item.isRefundedSafe()) {
        item.completeSettlement();
        log.info("정산 항목 {} 이체 완료 처리", item.getId());
      }
    }

    // 페이플 이체 결과 정보 저장
    settlement.updatePaypleTransferResult(
        webhookRequest.getApiTranId(),
        webhookRequest.getApiTranDtm(),
        webhookRequest.getBankTranId(),
        webhookRequest.getBankTranDate(),
        webhookRequest.getBankRspCode(),
        null); // 실제 웹훅에는 bank_rsp_msg가 없으므로 null

    settlementRepository.save(settlement);
    log.info("정산 {} 이체 성공 처리 완료", settlement.getId());
  }

  /**
   * 이체 실패 처리
   *
   * @param settlement 정산 엔티티
   * @param webhookRequest 웹훅 요청 데이터
   */
  private void handleTransferFailure(Settlement settlement, PaypleWebhookRequest webhookRequest) {
    log.error(
        "정산 {} 이체 실패 - 결과: {}, 메시지: {}, 은행응답코드: {}",
        settlement.getId(),
        webhookRequest.getResult(),
        webhookRequest.getMessage(),
        webhookRequest.getBankRspCode());

    // 정산 상태를 실패로 업데이트
    settlement.failSettlement();

    // 정산 항목들의 상태도 실패로 업데이트
    for (SettlementItem item : settlement.getSettlementItems()) {
      if (!item.isRefundedSafe()) {
        item.failSettlement();
        log.info("정산 항목 {} 이체 실패 처리", item.getId());
      }
    }

    // 실패 정보 저장 (은행 응답 정보 포함)
    settlement.updatePaypleTransferResult(
        webhookRequest.getApiTranId(),
        webhookRequest.getApiTranDtm(),
        webhookRequest.getBankTranId(),
        webhookRequest.getBankTranDate(),
        webhookRequest.getBankRspCode(),
        webhookRequest.getMessage()); // 실패 시 메시지를 bank_rsp_msg에 저장

    settlementRepository.save(settlement);
    log.error("정산 {} 이체 실패 처리 완료", settlement.getId());
  }

  /**
   * 빌링키로 정산 엔티티 검색
   *
   * @param billingTranId 페이플 빌링키
   * @return 해당 빌링키를 가진 정산 목록
   */
  private List<Settlement> findSettlementsByBillingTranId(String billingTranId) {
    return settlementRepository.findByPaypleBillingTranId(billingTranId);
  }

  /**
   * 민감한 데이터 마스킹 (로깅용)
   *
   * @param sensitiveData 마스킹할 데이터
   * @return 마스킹된 데이터
   */
  private String maskSensitiveData(String sensitiveData) {
    if (sensitiveData == null || sensitiveData.length() <= 8) {
      return "****";
    }
    return sensitiveData.substring(0, 4)
        + "****"
        + sensitiveData.substring(sensitiveData.length() - 4);
  }
}
