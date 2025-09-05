package liaison.groble.application.admin.settlement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.settlement.dto.AdminSettlementDetailDTO;
import liaison.groble.application.admin.settlement.dto.AdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.FailedSettlementDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.PaypleSettlementResultDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.settlement.dto.FlatAdminSettlementsDTO;
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
  private final SettlementReader settlementReader;
  private final PaypleConfig paypleConfig;

  @Transactional(readOnly = true)
  public PageResponse<AdminSettlementOverviewDTO> getAllUsersSettlements(
      Long adminUserId, Pageable pageable) {
    Page<FlatAdminSettlementsDTO> page =
        settlementReader.findAdminSettlementsByUserId(adminUserId, pageable);

    List<AdminSettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToAdminSettlementsDTO).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional(readOnly = true)
  public AdminSettlementDetailDTO getSettlementDetail(Long settlementId) {
    Settlement settlement = settlementReader.getSettlementById(settlementId);

    return AdminSettlementDetailDTO.builder()
        .settlementId(settlement.getId())
        .settlementStartDate(settlement.getSettlementStartDate())
        .settlementEndDate(settlement.getSettlementEndDate())
        .scheduledSettlementDate(settlement.getScheduledSettlementDate())
        .settlementAmount(settlement.getSettlementAmount())
        .pgFee(settlement.getPgFee())
        .platformFee(settlement.getPlatformFee())
        .vatAmount(settlement.getFeeVat())
        .build();
  }

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

      // 3. 계좌 인증 성공 시 빌링키 추출
      String billingTranId = extractBillingTranId(accountResult);
      if (billingTranId == null) {
        throw new PaypleApiException("계좌 인증에서 빌링키를 가져올 수 없습니다");
      }

      // 4. 각 정산 항목에 대해 이체 대기 요청 (테스트용 1000원 고정)
      String groupKey = null;
      for (SettlementItem item : validItems) {
        JSONObject transferResult =
            paypleSettlementService.requestTransfer(
                billingTranId,
                "1000", // 테스트 시 1000원 고정
                null, // sub_id
                "정산" // 거래 내역 표시 문구
                );

        // 첫 번째 이체 대기 요청에서 group_key 추출
        if (groupKey == null) {
          groupKey = extractGroupKey(transferResult);
        }

        log.info(
            "정산 항목 {} 이체 대기 요청 완료: {}",
            item.getId(),
            transferResult.getOrDefault("result", "UNKNOWN"));
      }

      // 5. 이체 대기 성공 후 처리 (즉시 실행 vs 대기)
      if (groupKey != null) {
        // TODO: 실제 운영에서는 이 부분을 설정으로 제어할 수 있음
        boolean executeImmediately = false; // false로 변경하여 검토 단계 추가

        if (executeImmediately) {
          JSONObject executeResult =
              paypleSettlementService.requestTransferExecute(
                  groupKey,
                  "ALL", // 그룹의 모든 이체 대기 건 실행
                  authResult.getAccessToken(),
                  "http://your-test-domain.com" // 테스트 웹훅 URL
                  );

          log.info("이체 실행 요청 완료 - 결과: {}", executeResult.getOrDefault("result", "UNKNOWN"));
        } else {
          log.info("이체 대기 완료 - 관리자 검토 대기 중 (그룹키: {})", maskSensitiveData(groupKey));
          // 실제 운영에서는 여기서 관리자에게 알림을 보내거나 대시보드에서 확인할 수 있도록 함
        }
      } else {
        log.warn("이체 대기 요청에서 group_key를 추출할 수 없습니다");
      }

      // 6. 그룹 정산 요청 (기존 로직 유지)
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

  /**
   * 이체 대기 상태인 정산을 실제 실행
   *
   * @param groupKey 이체 대기 시 받은 그룹키
   * @param billingTranId 실행할 빌링키 ("ALL" 또는 특정 빌링키)
   * @return 이체 실행 결과
   */
  public PaypleSettlementResultDTO executeTransfer(String groupKey, String billingTranId) {
    log.info("이체 실행 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    try {
      // 1. 파트너 인증
      PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();
      if (!authResult.isSuccess()) {
        throw new PaypleApiException("페이플 파트너 인증 실패: " + authResult.getMessage());
      }

      // 2. 이체 실행 요청
      JSONObject executeResult =
          paypleSettlementService.requestTransferExecute(
              groupKey,
              billingTranId,
              authResult.getAccessToken(),
              "http://your-test-domain.com" // 테스트 웹훅 URL
              );

      log.info("이체 실행 완료 - 결과: {}", executeResult.getOrDefault("result", "UNKNOWN"));

      return PaypleSettlementResultDTO.builder()
          .success(true)
          .responseCode("A0000")
          .responseMessage("이체 실행 성공")
          .build();

    } catch (Exception e) {
      log.error("이체 실행 실패", e);
      return PaypleSettlementResultDTO.builder()
          .success(false)
          .responseCode("ERROR")
          .responseMessage("이체 실행 실패: " + e.getMessage())
          .build();
    }
  }

  /**
   * 이체 대기 상태인 정산을 취소
   *
   * @param groupKey 이체 대기 시 받은 그룹키
   * @param billingTranId 취소할 빌링키 ("ALL" 또는 특정 빌링키)
   * @param cancelReason 취소 사유
   * @return 이체 취소 결과
   */
  public PaypleSettlementResultDTO cancelTransfer(
      String groupKey, String billingTranId, String cancelReason) {
    log.info("이체 취소 요청 - 그룹키: {}, 빌링키: {}", maskSensitiveData(groupKey), billingTranId);

    try {
      // 1. 파트너 인증
      PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();
      if (!authResult.isSuccess()) {
        throw new PaypleApiException("페이플 파트너 인증 실패: " + authResult.getMessage());
      }

      // 2. 이체 취소 요청
      JSONObject cancelResult =
          paypleSettlementService.requestTransferCancel(
              groupKey, billingTranId, authResult.getAccessToken(), cancelReason);

      log.info("이체 취소 완료 - 결과: {}", cancelResult.getOrDefault("result", "UNKNOWN"));

      return PaypleSettlementResultDTO.builder()
          .success(true)
          .responseCode("A0000")
          .responseMessage("이체 취소 성공")
          .build();

    } catch (Exception e) {
      log.error("이체 취소 실패", e);
      return PaypleSettlementResultDTO.builder()
          .success(false)
          .responseCode("ERROR")
          .responseMessage("이체 취소 실패: " + e.getMessage())
          .build();
    }
  }

  /**
   * 계좌 인증 결과에서 빌링키 추출
   *
   * @param accountResult 계좌 인증 API 응답
   * @return 빌링키 또는 null
   */
  private String extractBillingTranId(JSONObject accountResult) {
    if (accountResult == null) {
      return null;
    }

    // 페이플 계좌 인증 응답에서 빌링키 추출
    String result =
        accountResult.get("result") != null ? accountResult.get("result").toString() : null;

    if (!"A0000".equals(result)) {
      log.warn("계좌 인증 실패 - result: {}, message: {}", result, accountResult.get("message"));
      return null;
    }

    Object billingTranId = accountResult.get("billing_tran_id");
    return billingTranId != null ? billingTranId.toString() : null;
  }

  /**
   * 이체 대기 요청 결과에서 그룹키 추출
   *
   * @param transferResult 이체 대기 요청 API 응답
   * @return 그룹키 또는 null
   */
  private String extractGroupKey(JSONObject transferResult) {
    if (transferResult == null) {
      return null;
    }

    // 페이플 이체 대기 요청 응답에서 그룹키 추출
    String result =
        transferResult.get("result") != null ? transferResult.get("result").toString() : null;

    if (!"A0000".equals(result)) {
      log.warn("이체 대기 요청 실패 - result: {}, message: {}", result, transferResult.get("message"));
      return null;
    }

    Object groupKey = transferResult.get("group_key");
    return groupKey != null ? groupKey.toString() : null;
  }

  /** 민감한 데이터 마스킹 (그룹키 등) */
  private String maskSensitiveData(String sensitiveData) {
    if (sensitiveData == null || sensitiveData.length() <= 8) {
      return "****";
    }
    return sensitiveData.substring(0, 4)
        + "****"
        + sensitiveData.substring(sensitiveData.length() - 4);
  }

  private AdminSettlementOverviewDTO convertFlatDTOToAdminSettlementsDTO(
      FlatAdminSettlementsDTO flatAdminSettlementsDTO) {
    return AdminSettlementOverviewDTO.builder()
        .settlementId(flatAdminSettlementsDTO.getSettlementId())
        .scheduledSettlementDate(flatAdminSettlementsDTO.getScheduledSettlementDate())
        .contentType(flatAdminSettlementsDTO.getContentType())
        .settlementAmount(flatAdminSettlementsDTO.getSettlementAmount())
        .settlementStatus(flatAdminSettlementsDTO.getSettlementStatus())
        .verificationStatus(flatAdminSettlementsDTO.getVerificationStatus())
        .isBusinessSeller(flatAdminSettlementsDTO.getIsBusinessSeller())
        .businessType(flatAdminSettlementsDTO.getBusinessType())
        .bankAccountOwner(flatAdminSettlementsDTO.getBankAccountOwner())
        .bankName(flatAdminSettlementsDTO.getBankName())
        .bankAccountNumber(flatAdminSettlementsDTO.getBankAccountNumber())
        .copyOfBankbookUrl(flatAdminSettlementsDTO.getCopyOfBankbookUrl())
        .businessLicenseFileUrl(flatAdminSettlementsDTO.getBusinessLicenseFileUrl())
        .build();
  }
}
