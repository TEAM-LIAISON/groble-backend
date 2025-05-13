package liaison.groble.application.user.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.service.PortOneBankVerificationService;
import liaison.groble.domain.user.entity.BankAccount;
import liaison.groble.domain.user.entity.BankAccountVerification;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.BankAccountStatus;
import liaison.groble.domain.user.enums.BankAccountVerificationStatus;
import liaison.groble.domain.user.enums.BankAccountVerificationType;
import liaison.groble.domain.user.repository.BankAccountRepository;
import liaison.groble.domain.user.repository.BankAccountVerificationRepository;
import liaison.groble.external.config.PortOneProperties;
import liaison.groble.external.payment.PortOneApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneBankVerificationServiceImpl implements PortOneBankVerificationService {

  private final PortOneApiClient portOneClient;
  private final PortOneProperties portOneProperties;
  private final BankAccountRepository bankAccountRepository;
  private final BankAccountVerificationRepository verificationRepository;

  // 은행 코드-이름 매핑 (실제로는 더 많은 은행이 포함됨)
  private static final Map<String, String> BANK_CODE_MAP =
      Map.of(
          "004", "국민은행",
          "020", "우리은행",
          "088", "신한은행",
          "081", "하나은행",
          "011", "농협은행",
          "003", "기업은행",
          "045", "새마을금고",
          "071", "우체국");

  @Override
  @Transactional
  public BankAccount registerBankAccount(
      User user, String bankCode, String accountNumber, String holderName) {
    log.info("Registering bank account for user: {}, bank: {}", user.getId(), bankCode);

    // 은행명 조회
    String bankName = BANK_CODE_MAP.getOrDefault(bankCode, "알 수 없는 은행");

    // 계좌 엔티티 생성
    BankAccount bankAccount =
        BankAccount.builder()
            .user(user)
            .bankCode(bankCode)
            .bankName(bankName)
            .accountNumber(accountNumber)
            .accountHolderName(holderName)
            .status(BankAccountStatus.REGISTERED)
            .build();

    return bankAccountRepository.save(bankAccount);
  }

  @Override
  @Transactional
  public BankAccountVerification verifyAccountOwner(
      String bankCode, String accountNumber, String holderName) {
    log.info(
        "Verifying account owner: bank={}, account={}, holder={}",
        bankCode,
        accountNumber,
        holderName);

    // V2 API용 요청 데이터 준비
    Map<String, Object> requestData = new HashMap<>();

    // 예금주 조회 요청 본문 구성
    Map<String, Object> bankHolderRequest = new HashMap<>();
    bankHolderRequest.put("bankCode", bankCode);
    bankHolderRequest.put("bankAccountNumber", accountNumber);

    requestData.put("bankHolderRequest", bankHolderRequest);

    // 고유 검증 키 생성
    String verificationKey = "BANK_OWNER_" + UUID.randomUUID().toString().substring(0, 8);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi("/banking/bank-holder", HttpMethod.POST, requestData, Map.class);

      // 응답에서 예금주 정보 확인
      Map<String, Object> bankHolderResult = (Map<String, Object>) response.get("bankHolderResult");
      String responseHolderName = (String) bankHolderResult.get("bankHolderName");
      boolean matched = holderName.equals(responseHolderName);

      log.info(
          "Account owner verification result: {}, expected: {}, actual: {}",
          matched,
          holderName,
          responseHolderName);

      // 검증 결과 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.ACCOUNT_OWNER)
              .status(
                  matched
                      ? BankAccountVerificationStatus.COMPLETED
                      : BankAccountVerificationStatus.FAILED)
              .requestData(requestData)
              .build();

      if (matched) {
        verification.complete(response);
      } else {
        verification.fail(response);
      }

      return verificationRepository.save(verification);

    } catch (Exception e) {
      log.error("Failed to verify account owner", e);

      // 검증 실패 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.ACCOUNT_OWNER)
              .status(BankAccountVerificationStatus.FAILED)
              .requestData(requestData)
              .build();

      Map<String, Object> errorResponse = Map.of("error", e.getMessage());
      verification.fail(errorResponse);

      return verificationRepository.save(verification);
    }
  }

  @Override
  @Transactional
  public BankAccountVerification startOneCentVerification(BankAccount bankAccount) {
    log.info("Starting 1-cent verification for bank account: {}", bankAccount.getId());

    // V2 API용 요청 데이터 준비
    Map<String, Object> requestData = new HashMap<>();

    // 고유 검증 키 생성
    String verificationKey =
        "BANK_1CENT_" + bankAccount.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);

    // 1원 인증 요청 본문 구성
    Map<String, Object> microTransferRequest = new HashMap<>();
    microTransferRequest.put("bankCode", bankAccount.getBankCode());
    microTransferRequest.put("bankAccountNumber", bankAccount.getAccountNumber());
    microTransferRequest.put("holderName", bankAccount.getAccountHolderName());
    microTransferRequest.put("verificationKey", verificationKey);

    // 콜백 URL 설정
    Map<String, String> callbackUrls = new HashMap<>();
    callbackUrls.put("successUrl", portOneProperties.getBankCallbackUrl() + "/success");
    callbackUrls.put("failUrl", portOneProperties.getBankCallbackUrl() + "/fail");
    microTransferRequest.put("callbackUrls", callbackUrls);

    requestData.put("microTransferRequest", microTransferRequest);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi("/banking/micro-transfer", HttpMethod.POST, requestData, Map.class);

      // 응답에서 필요한 정보 추출
      Map<String, Object> microTransferResult =
          (Map<String, Object>) response.get("microTransferResult");
      String depositBank = (String) microTransferResult.get("bankName");
      String depositAccountNumber = (String) microTransferResult.get("accountNumber");
      BigDecimal depositAmount = new BigDecimal(microTransferResult.get("amount").toString());
      LocalDateTime expiredAt =
          LocalDateTime.parse(
              (String) microTransferResult.get("expiryAt"), DateTimeFormatter.ISO_DATE_TIME);

      log.info("1-cent verification initiated: key={}, expires={}", verificationKey, expiredAt);

      // 검증 정보 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .bankAccount(bankAccount)
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.ONE_CENT_DEPOSIT)
              .status(BankAccountVerificationStatus.REQUESTED)
              .requestData(requestData)
              .build();

      verification.setOneCentDepositInfo(
          depositAmount, depositBank, depositAccountNumber, expiredAt);
      verification.startProcess();

      return verificationRepository.save(verification);

    } catch (Exception e) {
      log.error("Failed to start 1-cent verification", e);

      // 검증 실패 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .bankAccount(bankAccount)
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.ONE_CENT_DEPOSIT)
              .status(BankAccountVerificationStatus.FAILED)
              .requestData(requestData)
              .build();

      Map<String, Object> errorResponse = Map.of("error", e.getMessage());
      verification.fail(errorResponse);

      return verificationRepository.save(verification);
    }
  }

  @Override
  @Transactional
  public BankAccountVerification confirmOneCentVerification(String verificationKey, String code) {
    log.info("Confirming 1-cent verification: key={}, code={}", verificationKey, "****");

    // 검증 정보 조회
    BankAccountVerification verification =
        verificationRepository
            .findByVerificationKey(verificationKey)
            .orElseThrow(
                () -> new IllegalArgumentException("검증 키에 해당하는 정보를 찾을 수 없습니다: " + verificationKey));

    // V2 API용 요청 데이터 준비
    Map<String, Object> requestData = new HashMap<>();

    // 1원 인증 확인 요청 본문 구성
    Map<String, Object> microTransferConfirmRequest = new HashMap<>();
    microTransferConfirmRequest.put("verificationKey", verificationKey);
    microTransferConfirmRequest.put("confirmationCode", code);

    requestData.put("microTransferConfirmRequest", microTransferConfirmRequest);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi(
              "/banking/micro-transfer/confirm", HttpMethod.POST, requestData, Map.class);

      // 응답에서 검증 결과 확인
      Map<String, Object> result = (Map<String, Object>) response.get("microTransferConfirmResult");
      boolean success = Boolean.TRUE.equals(result.get("verified"));

      log.info("1-cent verification result: {}", success ? "SUCCESS" : "FAILED");

      if (success) {
        verification.complete(response);

        // 계좌 상태 업데이트
        BankAccount bankAccount = verification.getBankAccount();
        bankAccount.verify();
        bankAccountRepository.save(bankAccount);
      } else {
        verification.fail(response);
      }

      return verificationRepository.save(verification);

    } catch (Exception e) {
      log.error("Failed to confirm 1-cent verification", e);

      // 검증 실패 저장
      verification.fail(Map.of("error", e.getMessage()));
      return verificationRepository.save(verification);
    }
  }

  @Override
  @Transactional
  public BankAccountVerification verifyAccountInstantly(
      String bankCode, String accountNumber, String holderName, String birthDate) {
    log.info("Verifying account instantly: bank={}, account={}", bankCode, accountNumber);

    // V2 API용 요청 데이터 준비
    Map<String, Object> requestData = new HashMap<>();

    // 고유 검증 키 생성
    String verificationKey = "BANK_INSTANT_" + UUID.randomUUID().toString().substring(0, 8);

    // 즉시 인증 요청 본문 구성
    Map<String, Object> instantVerificationRequest = new HashMap<>();
    instantVerificationRequest.put("bankCode", bankCode);
    instantVerificationRequest.put("bankAccountNumber", accountNumber);
    instantVerificationRequest.put("holderName", holderName);
    instantVerificationRequest.put("identityNumber", birthDate); // 생년월일 또는 사업자번호
    instantVerificationRequest.put("verificationKey", verificationKey);

    requestData.put("instantVerificationRequest", instantVerificationRequest);

    try {
      // 포트원 V2 API 호출
      Map<String, Object> response =
          portOneClient.callApi(
              "/banking/instant-verification", HttpMethod.POST, requestData, Map.class);

      // 응답에서 검증 결과 확인
      Map<String, Object> result = (Map<String, Object>) response.get("instantVerificationResult");
      boolean verified = Boolean.TRUE.equals(result.get("verified"));

      log.info("Instant account verification result: {}", verified ? "SUCCESS" : "FAILED");

      // 검증 결과 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.INSTANT)
              .status(
                  verified
                      ? BankAccountVerificationStatus.COMPLETED
                      : BankAccountVerificationStatus.FAILED)
              .requestData(requestData)
              .build();

      if (verified) {
        verification.complete(response);
      } else {
        verification.fail(response);
      }

      return verificationRepository.save(verification);

    } catch (Exception e) {
      log.error("Failed to verify account instantly", e);

      // 검증 실패 저장
      BankAccountVerification verification =
          BankAccountVerification.builder()
              .verificationKey(verificationKey)
              .type(BankAccountVerificationType.INSTANT)
              .status(BankAccountVerificationStatus.FAILED)
              .requestData(requestData)
              .build();

      Map<String, Object> errorResponse = Map.of("error", e.getMessage());
      verification.fail(errorResponse);

      return verificationRepository.save(verification);
    }
  }

  @Override
  public BankAccountVerificationStatus getVerificationStatus(String verificationKey) {
    log.debug("Getting verification status for key: {}", verificationKey);

    return verificationRepository
        .findByVerificationKey(verificationKey)
        .map(BankAccountVerification::getStatus)
        .orElse(BankAccountVerificationStatus.PENDING);
  }

  @Override
  @Transactional
  public void handleWebhook(Map<String, Object> webhookData) {
    log.info("Handling bank account verification webhook: {}", webhookData);

    // V2 API 웹훅 처리
    String eventType = (String) webhookData.get("eventType");

    // 은행 계좌 검증 관련 이벤트만 처리
    if (!eventType.startsWith("BANKING_")) {
      log.debug("Ignoring non-banking webhook: {}", eventType);
      return;
    }

    // 데이터 추출
    Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
    String verificationKey = null;

    // 이벤트 유형에 따라 필요한 정보 추출
    if (eventType.contains("MICRO_TRANSFER")) {
      Map<String, Object> microTransferResult =
          (Map<String, Object>) data.get("microTransferResult");
      if (microTransferResult != null) {
        verificationKey = (String) microTransferResult.get("verificationKey");
      }
    } else if (eventType.contains("INSTANT_VERIFICATION")) {
      Map<String, Object> instantVerificationResult =
          (Map<String, Object>) data.get("instantVerificationResult");
      if (instantVerificationResult != null) {
        verificationKey = (String) instantVerificationResult.get("verificationKey");
      }
    }

    if (verificationKey == null) {
      log.warn("No verification key found in webhook data: {}", webhookData);
      return;
    }

    BankAccountVerification verification =
        verificationRepository.findByVerificationKey(verificationKey).orElse(null);

    if (verification == null) {
      log.warn("No verification found for key: {}", verificationKey);
      return;
    }

    // 상태 정보 추출
    String status = (String) webhookData.get("status");

    // 상태에 따른 처리
    switch (status) {
      case "READY":
        log.debug("Webhook: Verification ready: {}", verificationKey);
        verification.startProcess();
        break;

      case "IN_PROGRESS":
        log.debug("Webhook: Verification processing: {}", verificationKey);
        verification.startProcess();
        break;

      case "COMPLETED":
      case "DONE":
        log.info("Webhook: Verification completed: {}", verificationKey);
        verification.complete(webhookData);

        // 계좌 상태 업데이트 (1원 인증인 경우)
        if (verification.getType() == BankAccountVerificationType.ONE_CENT_DEPOSIT
            && verification.getBankAccount() != null) {
          BankAccount bankAccount = verification.getBankAccount();
          bankAccount.verify();
          bankAccountRepository.save(bankAccount);
        }
        break;

      case "FAILED":
        log.warn("Webhook: Verification failed: {}", verificationKey);
        verification.fail(webhookData);
        break;

      case "EXPIRED":
        log.warn("Webhook: Verification expired: {}", verificationKey);
        verification.expire();
        break;

      default:
        log.warn("Webhook: Unknown status: {}", status);
    }

    verificationRepository.save(verification);
  }
}
