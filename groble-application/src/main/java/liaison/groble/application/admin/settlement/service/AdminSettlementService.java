package liaison.groble.application.admin.settlement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.settlement.dto.AdminSettlementDetailDTO;
import liaison.groble.application.admin.settlement.dto.AdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PaypleAccountRemainDTO;
import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.admin.settlement.dto.PerTransactionAdminSettlementOverviewDTO;
import liaison.groble.application.admin.settlement.dto.PgFeeAdjustmentDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.FailedSettlementDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalDTO.PaypleSettlementResultDTO;
import liaison.groble.application.admin.settlement.dto.SettlementApprovalRequestDTO;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.application.settlement.reader.SettlementReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.settlement.dto.FlatAdminSettlementsDTO;
import liaison.groble.domain.settlement.dto.FlatPerTransactionSettlement;
import liaison.groble.domain.settlement.dto.FlatPgFeeAdjustmentDTO;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.enums.SettlementType;
import liaison.groble.domain.settlement.repository.SettlementRepository;
import liaison.groble.domain.user.entity.User;
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
  private final PaypleAccountVerificationFactory paypleAccountVerificationFactory;
  private final KakaoNotificationService kakaoNotificationService;

  @Transactional(readOnly = true)
  public PageResponse<AdminSettlementOverviewDTO> getAllUsersSettlements(
      Long adminUserId, Pageable pageable) {
    Page<FlatAdminSettlementsDTO> page =
        settlementReader.findAdminSettlementsByUserId(adminUserId, pageable);

    List<AdminSettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToAdminSettlementsDTO).toList();

    return PageResponse.from(page, items, buildMetaData(pageable));
  }

  @Transactional(readOnly = true)
  public AdminSettlementDetailDTO getSettlementDetail(Long settlementId) {
    Settlement settlement = settlementReader.getSettlementById(settlementId);

    return AdminSettlementDetailDTO.builder()
        .settlementId(settlement.getId())
        .settlementStartDate(settlement.getSettlementStartDate())
        .settlementEndDate(settlement.getSettlementEndDate())
        .scheduledSettlementDate(settlement.getScheduledSettlementDate())
        .totalSalesAmount(settlement.getTotalSalesAmount())
        .totalRefundAmount(settlement.getTotalRefundAmount())
        .refundCount(settlement.getRefundCount())
        .totalFee(settlement.getTotalFee())
        .totalFeeDisplay(settlement.getTotalFeeDisplay())
        .settlementAmount(settlement.getSettlementAmount())
        .settlementAmountDisplay(settlement.getSettlementAmountDisplay())
        .pgFee(settlement.getPgFee())
        .pgFeeDisplay(settlement.getPgFeeDisplay())
        .pgFeeRefundExpected(settlement.getPgFeeRefundExpected())
        .platformFee(settlement.getPlatformFee())
        .platformFeeDisplay(settlement.getPlatformFeeDisplay())
        .platformFeeForgone(settlement.getPlatformFeeForgone())
        .vatAmount(settlement.getFeeVat())
        .feeVatDisplay(settlement.getFeeVatDisplay())
        .platformFeeRate(settlement.getPlatformFeeRate())
        .platformFeeRateDisplay(settlement.getPlatformFeeRateDisplay())
        .platformFeeRateBaseline(settlement.getPlatformFeeRateBaseline())
        .pgFeeRate(settlement.getPgFeeRate())
        .pgFeeRateDisplay(settlement.getPgFeeRateDisplay())
        .pgFeeRateBaseline(settlement.getPgFeeRateBaseline())
        .vatRate(settlement.getVatRate())
        .settlementNote(settlement.getSettlementNote())
        .build();
  }

  @Transactional(readOnly = true)
  public PageResponse<PerTransactionAdminSettlementOverviewDTO> getSalesList(
      Long settlementId, Pageable pageable) {
    Page<FlatPerTransactionSettlement> page =
        settlementReader.findSalesListBySettlementId(settlementId, pageable);

    List<PerTransactionAdminSettlementOverviewDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToPerTransactionDTO).toList();

    return PageResponse.from(page, items, buildMetaData(pageable));
  }

  @Transactional(readOnly = true)
  public PageResponse<PgFeeAdjustmentDTO> getPgFeeAdjustments(
      LocalDate startDate, LocalDate endDate, Long settlementId, Long sellerId, Pageable pageable) {

    Page<FlatPgFeeAdjustmentDTO> page =
        settlementReader.findPgFeeAdjustments(startDate, endDate, settlementId, sellerId, pageable);

    List<PgFeeAdjustmentDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToPgFeeAdjustmentDTO).toList();

    return PageResponse.from(page, items, buildMetaData(pageable));
  }

  @Transactional(readOnly = true)
  public PaypleAccountRemainDTO getPaypleAccountRemain() {
    PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();

    if (!authResult.isSuccess()) {
      throw new PaypleApiException(
          "페이플 파트너 인증 실패: " + authResult.getMessage() + " (code: " + authResult.getCode() + ")");
    }

    JSONObject remainResult =
        paypleSettlementService.requestAccountRemain(authResult.getAccessToken());
    BigDecimal cumulativeDifference =
        Optional.ofNullable(settlementRepository.sumPgFeeRefundExpectedForCompleted())
            .orElse(BigDecimal.ZERO);

    return PaypleAccountRemainDTO.builder()
        .result(getStringValue(remainResult, "result"))
        .message(getStringValue(remainResult, "message"))
        .code(getStringValue(remainResult, "code"))
        .totalAccountAmount(getDecimalValue(remainResult, "total_account_amt"))
        .totalTransferAmount(getDecimalValue(remainResult, "total_transfer_amt"))
        .remainAmount(getDecimalValue(remainResult, "remain_amt"))
        .apiTranDtm(getStringValue(remainResult, "api_tran_dtm"))
        .cumulativePgFeeRefundExpected(cumulativeDifference)
        .build();
  }

  /**
   * 정산 승인 및 실행 처리
   *
   * @param requestDTO 정산 승인 요청 정보
   * @return 정산 승인 및 실행 결과
   */
  public SettlementApprovalDTO approveAndExecuteSettlements(
      SettlementApprovalRequestDTO requestDTO) {

    log.info("정산 승인 및 실행 처리 시작 - 정산 수: {}", requestDTO.getSettlementIds().size());

    // 1. 정산 조회 및 검증
    List<Settlement> settlements = validateAndRetrieveSettlements(requestDTO.getSettlementIds());

    // 2. 정산들 사전 검증 (실제 승인은 페이플 성공 후)
    List<FailedSettlementDTO> failedSettlements = new ArrayList<>();
    List<Settlement> validSettlements = new ArrayList<>();
    List<ApprovalResult> approvalResults = new ArrayList<>();

    int totalApprovedItemCount = 0;
    int totalExcludedRefundedItemCount = 0;
    BigDecimal totalApprovedAmount = BigDecimal.ZERO;

    for (Settlement settlement : settlements) {
      try {
        ApprovalResult result = validateSettlementForApproval(settlement);

        validSettlements.add(settlement);
        approvalResults.add(result);
        totalApprovedItemCount += result.getApprovedItemCount();
        totalExcludedRefundedItemCount += result.getExcludedRefundedItemCount();
        totalApprovedAmount = totalApprovedAmount.add(result.getApprovedAmount());

      } catch (Exception e) {
        log.error("정산 검증 실패 - ID: {}", settlement.getId(), e);
        failedSettlements.add(
            FailedSettlementDTO.builder()
                .settlementId(settlement.getId())
                .failureReason(e.getMessage())
                .build());
      }
    }

    // 3. 페이플 정산 실행 먼저 시도
    PaypleSettlementResultDTO paypleResult = null;
    int actualApprovedSettlementCount = 0;

    if (!validSettlements.isEmpty()) {
      List<SettlementItem> validItemsForPayple = extractValidItemsForPayple(validSettlements);
      if (!validItemsForPayple.isEmpty()) {
        // 페이플 이체 실행 전 정산들을 처리중 상태로 변경
        for (Settlement settlement : validSettlements) {
          settlement.startProcessing();
          log.info("정산 {} 처리 시작 - 상태: PROCESSING", settlement.getId());
        }

        paypleResult = executePaypleGroupSettlementImmediately(validItemsForPayple);
        String paypleResponseNote = buildPaypleResponseNote(paypleResult);

        // 4. 페이플 정산 성공 시에만 DB 상태를 COMPLETED로 변경
        if (paypleResult != null && paypleResult.isSuccess()) {
          for (Settlement settlement : validSettlements) {
            settlement.completeSettlement(); // 웹훅에서도 호출되지만 즉시 실행이므로 여기서 완료 처리
            if (paypleResponseNote != null) {
              settlement.updateSettlementNote(paypleResponseNote);
            }
            actualApprovedSettlementCount++;
            log.info("정산 최종 승인 완료 - ID: {}", settlement.getId());
            sendSettlementCompletedNotification(settlement);
          }
        } else {
          log.error("페이플 정산 실패로 인한 정산 승인 취소 - 정산 수: {}", validSettlements.size());
          String failureNote =
              paypleResponseNote != null ? paypleResponseNote : "Payple FAILURE - 코드/메시지 없음";
          // 페이플 실패 시 검증된 정산들을 실패 처리
          for (Settlement settlement : validSettlements) {
            settlement.failSettlement(); // 보류 상태로 변경
            settlement.updateSettlementNote(failureNote);
            failedSettlements.add(
                FailedSettlementDTO.builder()
                    .settlementId(settlement.getId())
                    .failureReason(failureNote)
                    .build());
          }
          // 성공 카운트들을 0으로 재설정
          totalApprovedItemCount = 0;
          totalExcludedRefundedItemCount = 0;
          totalApprovedAmount = BigDecimal.ZERO;
        }
      }
    }

    // 5. 결과 반환
    return SettlementApprovalDTO.builder()
        .success(failedSettlements.isEmpty())
        .approvedSettlementCount(actualApprovedSettlementCount)
        .approvedItemCount(totalApprovedItemCount)
        .totalApprovedAmount(totalApprovedAmount)
        .approvedAt(LocalDateTime.now())
        .paypleResult(paypleResult)
        .failedSettlements(failedSettlements.isEmpty() ? null : failedSettlements)
        .excludedRefundedItemCount(totalExcludedRefundedItemCount)
        .build();
  }

  public AdminSettlementDetailDTO completeSettlementManually(Long settlementId) {
    Settlement settlement = settlementReader.getSettlementById(settlementId);

    settlement.completeManually("정산 수동 완료 처리 성공");
    sendSettlementCompletedNotification(settlement);

    log.info("정산 수동 완료 처리 - ID: {}, settledAt: {}", settlement.getId(), settlement.getSettledAt());

    return getSettlementDetail(settlementId);
  }

  private void sendSettlementCompletedNotification(Settlement settlement) {
    try {
      String phoneNumber =
          Optional.ofNullable(settlement.getUser()).map(User::getPhoneNumber).orElse(null);
      if (!hasText(phoneNumber)) {
        log.warn("정산 완료 알림톡 발송 스킵 - 수신번호 없음 settlementId: {}", settlement.getId());
        return;
      }

      LocalDate settlementDate =
          Optional.ofNullable(settlement.getSettledAt())
              .map(LocalDateTime::toLocalDate)
              .orElse(LocalDate.now());
      BigDecimal settlementAmount =
          Optional.ofNullable(settlement.getSettlementAmountDisplay())
              .orElse(settlement.getSettlementAmount());

      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.SETTLEMENT_COMPLETED)
              .phoneNumber(phoneNumber)
              .sellerName(resolveMakerName(settlement))
              .settlementDate(settlementDate)
              .contentTypeLabel(resolveContentTypeLabel(settlement))
              .settlementAmount(settlementAmount)
              .build());

      log.info("정산 완료 알림톡 발송 완료 - settlementId: {}", settlement.getId());
    } catch (Exception e) {
      log.error("정산 완료 알림톡 발송 실패 - settlementId: {}", settlement.getId(), e);
    }
  }

  private String resolveMakerName(Settlement settlement) {
    return Optional.ofNullable(settlement.getUser())
        .map(User::getNickname)
        .filter(this::hasText)
        .orElseGet(
            () -> {
              if (hasText(settlement.getAccountHolder())) {
                return settlement.getAccountHolder();
              }
              return "메이커";
            });
  }

  private String resolveContentTypeLabel(Settlement settlement) {
    SettlementType settlementType = settlement.getSettlementType();
    if (settlementType == null) {
      return "콘텐츠";
    }
    return switch (settlementType) {
      case COACHING -> "서비스";
      case DOCUMENT -> "자료";
      default -> "콘텐츠";
    };
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
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

  /** 정산 승인 가능 여부 검증 (실제 승인은 하지 않음) */
  private ApprovalResult validateSettlementForApproval(Settlement settlement) {
    if (settlement.getStatus() == Settlement.SettlementStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 정산입니다: " + settlement.getId());
    }
    if (settlement.getStatus() == Settlement.SettlementStatus.NOT_APPLICABLE) {
      throw new IllegalStateException("정산 미해당 상태는 승인 대상이 아닙니다: " + settlement.getId());
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

    if (validItems.isEmpty()) {
      throw new IllegalStateException("정산 가능한 항목이 없습니다: " + settlement.getId());
    }

    BigDecimal approvedAmount =
        validItems.stream()
            .map(SettlementItem::getSettlementAmountDisplay)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    log.info(
        "정산 검증 완료 - ID: {}, 정상 항목: {}개, 환불 제외 항목: {}개, 승인 예정 금액: {}",
        settlement.getId(),
        validItems.size(),
        refundedItems.size(),
        approvedAmount);

    return new ApprovalResult(validItems.size(), refundedItems.size(), approvedAmount);
  }

  /** 개별 정산 승인 처리 */
  private ApprovalResult approveSettlement(Settlement settlement) {
    if (settlement.getStatus() == Settlement.SettlementStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 정산입니다: " + settlement.getId());
    }
    if (settlement.getStatus() == Settlement.SettlementStatus.NOT_APPLICABLE) {
      throw new IllegalStateException("정산 미해당 상태는 승인할 수 없습니다: " + settlement.getId());
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
            .map(SettlementItem::getSettlementAmountDisplay)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Settlement 상태를 승인으로 변경
    settlement.approve();

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

  /** 페이플 그룹 정산 승인 후 즉시 실행 */
  private PaypleSettlementResultDTO executePaypleGroupSettlementImmediately(
      List<SettlementItem> validItems) {
    log.info("페이플 그룹 정산 승인 후 즉시 실행 시작 - 유효 항목 수: {}", validItems.size());

    try {
      // 1. 파트너 인증
      PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();

      if (!authResult.isSuccess()) {
        throw new PaypleApiException("페이플 파트너 인증 실패: " + authResult.getMessage());
      }

      // 2. 빌링키 확보 (저장된 정보 우선 사용, 없으면 새로 계좌 인증)
      String billingTranId =
          getOrCreateBillingTranId(validItems.get(0), authResult.getAccessToken());

      // 3. 각 정산 항목에 대해 이체 대기 요청
      String groupKey = null;
      for (SettlementItem item : validItems) {
        // 환경에 따른 이체 금액 결정 (운영은 1.7% 노출 수수료 기준)
        String transferAmount = resolveTransferAmount(item);

        log.info(
            "정산 항목 {} 이체 대기 요청 - 금액: {}원 (테스트모드: {})",
            item.getId(),
            transferAmount,
            paypleConfig.isTestMode());

        JSONObject transferResult =
            paypleSettlementService.requestTransfer(
                billingTranId, transferAmount, authResult.getAccessToken() // 액세스 토큰 전달
                );

        String result = getStringValue(transferResult, "result");
        log.info("정산 항목 {} 이체 대기 요청 완료: {}", item.getId(), result);

        // 이체 대기 요청 실패 시 즉시 중단
        if (!"A0000".equals(result)) {
          String errorMessage = getStringValue(transferResult, "message");
          log.error("이체 대기 요청 실패 - result: {}, message: {}", result, errorMessage);
          throw new PaypleApiException("이체 대기 요청 실패: " + errorMessage);
        }

        // 첫 번째 이체 대기 요청에서 group_key 추출
        if (groupKey == null) {
          groupKey = extractGroupKey(transferResult);
          if (groupKey == null) {
            throw new PaypleApiException("이체 대기 요청에서 group_key를 추출할 수 없습니다");
          }
        }
      }

      // 5. 이체 대기 성공 후 즉시 실행
      JSONObject executeResult =
          paypleSettlementService.requestTransferExecute(
              groupKey,
              "ALL", // 그룹의 모든 이체 대기 건 실행
              authResult.getAccessToken(),
              paypleConfig.getWebhookUrl() // 환경별 웹훅 URL
              );

      String executeResultCode = getStringValue(executeResult, "result");
      log.info("이체 즉시 실행 완료 - 결과: {}", executeResultCode);

      // 이체 실행 실패 시 중단
      if (!"A0000".equals(executeResultCode)) {
        String executeErrorMessage = getStringValue(executeResult, "message");
        log.error("이체 실행 실패 - result: {}, message: {}", executeResultCode, executeErrorMessage);
        throw new PaypleApiException("이체 실행 실패: " + executeErrorMessage);
      }

      // 이체 실행 완료 후 추가 처리 불필요 (웹훅에서 최종 결과 수신)

      return PaypleSettlementResultDTO.builder()
          .success(true)
          .responseCode(authResult.getResult())
          .responseMessage(authResult.getMessage())
          .accessToken(authResult.getAccessToken())
          .expiresIn(authResult.getExpiresIn())
          .build();

    } catch (Exception e) {
      log.error("페이플 그룹 정산 승인 후 즉시 실행 실패", e);
      return PaypleSettlementResultDTO.builder()
          .success(false)
          .responseCode("ERROR")
          .responseMessage("페이플 정산 승인 및 실행 실패: " + e.getMessage())
          .build();
    }
  }

  /** 계좌 인증 요청 생성 (SellerInfo에서 계좌 정보 가져오기) */
  private PaypleAccountVerificationRequest buildAccountVerificationRequest(
      SettlementItem settlementItem) {
    return paypleAccountVerificationFactory.buildForSettlementItem(settlementItem);
  }

  private String resolveTransferAmount(SettlementItem item) {
    if (paypleConfig.isTestMode()) {
      return paypleConfig.getTestTransferAmount();
    }

    BigDecimal settlementAmountDisplay =
        Optional.ofNullable(item.getSettlementAmountDisplay()).orElse(BigDecimal.ZERO);
    return settlementAmountDisplay.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * 빌링키 확보 - 저장된 정보 우선 사용, 없으면 새로 계좌 인증
   *
   * @param settlementItem 정산 항목 (사용자 정보를 위해)
   * @param accessToken 페이플 액세스 토큰
   * @return 빌링 거래 ID
   */
  private String getOrCreateBillingTranId(SettlementItem settlementItem, String accessToken) {
    Settlement settlement = settlementItem.getSettlement();

    // 1. 이미 저장된 빌링키가 있는지 확인
    if (settlement.isPaypleAccountVerified()) {
      log.info(
          "저장된 페이플 빌링키 사용 - Settlement ID: {}, billing_tran_id: {}",
          settlement.getId(),
          maskSensitiveData(settlement.getPaypleBillingTranId()));

      return settlement.getPaypleBillingTranId();
    }

    // 2. 저장된 빌링키가 없으면 새로 계좌 인증 수행
    log.info("저장된 빌링키가 없어 새로 계좌 인증 수행 - Settlement ID: {}", settlement.getId());

    try {
      PaypleAccountVerificationRequest accountRequest =
          buildAccountVerificationRequest(settlementItem);
      JSONObject accountResult =
          paypleSettlementService.requestAccountVerification(accountRequest, accessToken);

      // 3. 계좌 인증 성공 시 결과를 Settlement에 저장
      paypleSettlementService.saveAccountVerificationResult(settlement, accountResult);

      // 4. 빌링키 추출
      String billingTranId = extractBillingTranId(accountResult);
      if (billingTranId == null) {
        throw new PaypleApiException("계좌 인증에서 빌링키를 가져올 수 없습니다");
      }

      return billingTranId;

    } catch (Exception e) {
      log.error("빌링키 확보 실패 - Settlement ID: {}", settlement.getId(), e);
      throw new PaypleApiException("빌링키 확보 실패", e);
    }
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

    // 성공한 이체 대기 요청에서 그룹키만 추출 (실패 검증은 호출부에서 처리)
    Object groupKey = transferResult.get("group_key");
    return groupKey != null ? groupKey.toString() : null;
  }

  /** JSONObject에서 안전하게 문자열 추출 */
  private String getStringValue(JSONObject json, String key) {
    if (json == null) {
      return null;
    }
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }

  private BigDecimal getDecimalValue(JSONObject json, String key) {
    String value = getStringValue(json, key);
    if (value == null || value.trim().isEmpty()) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(value.trim());
    } catch (NumberFormatException ex) {
      log.warn("숫자 변환 실패 - key: {}, value: {}", key, value);
      return BigDecimal.ZERO;
    }
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

  private PageResponse.MetaData buildMetaData(Pageable pageable) {
    String sortBy = "createdAt";
    String sortDirection = Sort.Direction.DESC.name();

    if (pageable.getSort().isSorted()) {
      Sort.Order order = pageable.getSort().iterator().next();
      sortBy = order.getProperty();
      sortDirection = order.getDirection().name();
    }

    return PageResponse.MetaData.builder().sortBy(sortBy).sortDirection(sortDirection).build();
  }

  private String buildPaypleResponseNote(PaypleSettlementResultDTO paypleResult) {
    if (paypleResult == null) {
      return null;
    }

    String status = paypleResult.isSuccess() ? "SUCCESS" : "FAILURE";
    String responseCode =
        paypleResult.getResponseCode() != null ? paypleResult.getResponseCode() : "N/A";
    String responseMessage =
        paypleResult.getResponseMessage() != null ? paypleResult.getResponseMessage() : "N/A";

    return String.format(
        "Payple %s - code: %s, message: %s", status, responseCode, responseMessage);
  }

  private AdminSettlementOverviewDTO convertFlatDTOToAdminSettlementsDTO(
      FlatAdminSettlementsDTO flatAdminSettlementsDTO) {
    BigDecimal displayAmount = flatAdminSettlementsDTO.getSettlementAmountDisplay();
    return AdminSettlementOverviewDTO.builder()
        .settlementId(flatAdminSettlementsDTO.getSettlementId())
        .scheduledSettlementDate(flatAdminSettlementsDTO.getScheduledSettlementDate())
        .contentType(flatAdminSettlementsDTO.getContentType())
        .settlementAmount(displayAmount)
        .settlementStatus(
            resolveDisplayStatus(flatAdminSettlementsDTO.getSettlementStatus(), displayAmount))
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

  private PgFeeAdjustmentDTO convertFlatDTOToPgFeeAdjustmentDTO(FlatPgFeeAdjustmentDTO flat) {
    return PgFeeAdjustmentDTO.builder()
        .settlementId(flat.getSettlementId())
        .settlementItemId(flat.getSettlementItemId())
        .purchaseId(flat.getPurchaseId())
        .sellerId(flat.getSellerId())
        .sellerNickname(flat.getSellerNickname())
        .merchantUid(flat.getMerchantUid())
        .contentTitle(flat.getContentTitle())
        .salesAmount(flat.getSalesAmount())
        .pgFeeApplied(flat.getPgFeeApplied())
        .pgFeeDisplay(flat.getPgFeeDisplay())
        .pgFeeDifference(flat.getPgFeeDifference())
        .feeVat(flat.getFeeVat())
        .feeVatDisplay(flat.getFeeVatDisplay())
        .feeVatDifference(flat.getFeeVatDifference())
        .pgFeeRefundExpected(flat.getPgFeeRefundExpected())
        .totalFee(flat.getTotalFee())
        .totalFeeDisplay(flat.getTotalFeeDisplay())
        .settlementAmount(flat.getSettlementAmount())
        .settlementAmountDisplay(flat.getSettlementAmountDisplay())
        .purchasedAt(flat.getPurchasedAt())
        .orderStatus(flat.getOrderStatus())
        .capturedPgFeeRate(flat.getCapturedPgFeeRate())
        .capturedPgFeeRateDisplay(flat.getCapturedPgFeeRateDisplay())
        .capturedPgFeeRateBaseline(flat.getCapturedPgFeeRateBaseline())
        .capturedVatRate(flat.getCapturedVatRate())
        .build();
  }

  private PerTransactionAdminSettlementOverviewDTO convertFlatDTOToPerTransactionDTO(
      FlatPerTransactionSettlement flat) {
    return PerTransactionAdminSettlementOverviewDTO.builder()
        .contentTitle(flat.getContentTitle())
        .settlementAmount(flat.getSettlementAmountDisplay())
        .orderStatus(flat.getOrderStatus())
        .purchasedAt(flat.getPurchasedAt())
        .build();
  }

  private String resolveDisplayStatus(String originalStatus, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
      return Settlement.SettlementStatus.NOT_APPLICABLE.name();
    }
    return originalStatus;
  }
}
