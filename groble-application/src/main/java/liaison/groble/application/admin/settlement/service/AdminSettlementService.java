package liaison.groble.application.admin.settlement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.FailedSettlementDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.PaypleSettlementResultDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementRepository;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 정산 서비스
 *
 * <p>관리자가 정산 승인을 처리하는 비즈니스 로직을 담당합니다. Settlement 기반으로 승인하며, 환불된 SettlementItem은 자동으로 제외됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminSettlementService {

  private final SettlementRepository settlementRepository;
  private final PaypleSettlementService paypleSettlementService;
  private final PaypleConfig paypleConfig;

  /**
   * 정산 승인 처리
   *
   * @param requestDTO 정산 승인 요청 정보
   * @return 정산 승인 결과
   */
  public SettlementApprovalDTO approveSettlements(SettlementApprovalRequestDTO requestDTO) {

    log.info(
        "정산 승인 처리 시작 - 정산 수: {}, 관리자: {}, 페이플 실행: {}",
        requestDTO.getSettlementIds().size(),
        requestDTO.getAdminUserId(),
        requestDTO.isExecutePaypleSettlement());

    // 1. 정산 조회 및 검증
    List<Settlement> settlements = validateAndRetrieveSettlements(requestDTO.getSettlementIds());

    // 2. 정산들 승인 처리
    List<FailedSettlementDTO> failedSettlements = new ArrayList<>();
    List<Settlement> approvedSettlements = new ArrayList<>();

    int totalApprovedItemCount = 0;
    int totalExcludedRefundedItemCount = 0;
    BigDecimal totalApprovedAmount = BigDecimal.ZERO;

    for (Settlement settlement : settlements) {
      try {
        ApprovalResult result =
            approveSettlement(
                settlement, requestDTO.getAdminUserId(), requestDTO.getApprovalReason());

        approvedSettlements.add(settlement);
        totalApprovedItemCount += result.getApprovedItemCount();
        totalExcludedRefundedItemCount += result.getExcludedRefundedItemCount();
        totalApprovedAmount = totalApprovedAmount.add(result.getApprovedAmount());

      } catch (Exception e) {
        log.error("정산 승인 실패 - ID: {}", settlement.getId(), e);
        failedSettlements.add(
            FailedSettlementDTO.builder()
                .settlementId(settlement.getId())
                .failureReason(e.getMessage())
                .build());
      }
    }

    // 3. 페이플 정산 실행 (옵션) - 승인된 정산의 정상 항목들만
    PaypleSettlementResultDTO paypleResult = null;
    if (requestDTO.isExecutePaypleSettlement() && !approvedSettlements.isEmpty()) {
      List<SettlementItem> validItemsForPayple = extractValidItemsForPayple(approvedSettlements);
      if (!validItemsForPayple.isEmpty()) {
        paypleResult = executePaypleGroupSettlement(validItemsForPayple);
      }
    }

    // 4. 결과 반환
    return SettlementApprovalDTO.builder()
        .success(failedSettlements.isEmpty())
        .approvedSettlementCount(approvedSettlements.size())
        .approvedItemCount(totalApprovedItemCount)
        .totalApprovedAmount(totalApprovedAmount)
        .approvedAt(LocalDateTime.now())
        .paypleResult(paypleResult)
        .failedSettlements(failedSettlements.isEmpty() ? null : failedSettlements)
        .excludedRefundedItemCount(totalExcludedRefundedItemCount)
        .build();
  }

  /** 정산 조회 및 검증 */
  private List<Settlement> validateAndRetrieveSettlements(List<Long> settlementIds) {
    List<Settlement> settlements = settlementRepository.findByIdIn(settlementIds);

    if (settlements.size() != settlementIds.size()) {
      List<Long> foundIds =
          settlements.stream().map(Settlement::getId).collect(Collectors.toList());
      List<Long> missingIds =
          settlementIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());

      throw new IllegalArgumentException(String.format("존재하지 않는 정산들: %s", missingIds));
    }

    return settlements;
  }

  /** 개별 정산 승인 처리 */
  private ApprovalResult approveSettlement(
      Settlement settlement, Long adminUserId, String approvalReason) {
    if (settlement.getStatus() == Settlement.SettlementStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 정산입니다: " + settlement.getId());
    }

    // 환불되지 않은 정산 항목들만 계산
    List<SettlementItem> validItems =
        settlement.getSettlementItems().stream()
            .filter(item -> !item.isRefundedSafe())
            .collect(Collectors.toList());

    List<SettlementItem> refundedItems =
        settlement.getSettlementItems().stream()
            .filter(SettlementItem::isRefundedSafe)
            .collect(Collectors.toList());

    BigDecimal approvedAmount =
        validItems.stream()
            .map(SettlementItem::getSettlementAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Settlement 상태를 승인으로 변경
    settlement.approve(adminUserId, approvalReason);

    log.info(
        "정산 승인 완료 - ID: {}, 정상 항목: {}개, 환불 제외 항목: {}개, 승인 금액: {}",
        settlement.getId(),
        validItems.size(),
        refundedItems.size(),
        approvedAmount);

    return new ApprovalResult(validItems.size(), refundedItems.size(), approvedAmount);
  }

  /** 페이플 정산용 유효 항목 추출 */
  private List<SettlementItem> extractValidItemsForPayple(List<Settlement> approvedSettlements) {
    return approvedSettlements.stream()
        .flatMap(settlement -> settlement.getSettlementItems().stream())
        .filter(item -> !item.isRefundedSafe())
        .collect(Collectors.toList());
  }

  /** 페이플 그룹 정산 실행 */
  private PaypleSettlementResultDTO executePaypleGroupSettlement(List<SettlementItem> validItems) {
    log.info("페이플 그룹 정산 실행 시작 - 유효 항목 수: {}", validItems.size());

    try {
      // 1. 파트너 인증
      PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();

      if (!authResult.isSuccess()) {
        throw new PaypleApiException("페이플 파트너 인증 실패: " + authResult.getMessage());
      }

      // 2. 계좌 인증 (설정에서 계좌 정보 가져오기)
      PaypleAccountVerificationRequest accountRequest = buildAccountVerificationRequest();
      JSONObject accountResult =
          paypleSettlementService.requestAccountVerification(
              accountRequest, authResult.getAccessToken());

      // 3. 그룹 정산 요청
      JSONObject settlementResult =
          paypleSettlementService.requestGroupSettlement(validItems, authResult.getAccessToken());

      return PaypleSettlementResultDTO.builder()
          .success(true)
          .responseCode(authResult.getResult())
          .responseMessage(authResult.getMessage())
          .accessToken(authResult.getAccessToken())
          .expiresIn(authResult.getExpiresIn())
          .build();

    } catch (Exception e) {
      log.error("페이플 그룹 정산 실행 실패", e);
      return PaypleSettlementResultDTO.builder()
          .success(false)
          .responseCode("ERROR")
          .responseMessage("페이플 정산 실행 실패: " + e.getMessage())
          .build();
    }
  }

  /** 계좌 인증 요청 생성 (설정에서 기본값 사용) */
  private PaypleAccountVerificationRequest buildAccountVerificationRequest() {
    return PaypleAccountVerificationRequest.builder()
        .cstId(paypleConfig.getCstId())
        .custKey(paypleConfig.getCustKey())
        .bankCodeStd("020") // TODO: 설정에서 가져오기
        .accountNum("1234567890123456") // TODO: 설정에서 가져오기
        .accountHolderInfoType("0") // TODO: 설정에서 가져오기
        .accountHolderInfo("880212") // TODO: 설정에서 가져오기
        .subId("groble_sub") // TODO: 설정에서 가져오기
        .build();
  }

  /** 승인 결과를 담는 내부 클래스 */
  private static class ApprovalResult {
    private final int approvedItemCount;
    private final int excludedRefundedItemCount;
    private final BigDecimal approvedAmount;

    public ApprovalResult(
        int approvedItemCount, int excludedRefundedItemCount, BigDecimal approvedAmount) {
      this.approvedItemCount = approvedItemCount;
      this.excludedRefundedItemCount = excludedRefundedItemCount;
      this.approvedAmount = approvedAmount;
    }

    public int getApprovedItemCount() {
      return approvedItemCount;
    }

    public int getExcludedRefundedItemCount() {
      return excludedRefundedItemCount;
    }

    public BigDecimal getApprovedAmount() {
      return approvedAmount;
    }
  }
}
