package liaison.groble.application.payment.service;

import org.springframework.stereotype.Component;

import liaison.groble.application.payment.dto.PaypleAuthResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 개선된 PaypleApiClient 사용법 예시
 *
 * <p>다양한 인증 방식과 정산지급대행 기능을 보여주는 예시 클래스입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaypleApiClientUsageExample {

  private final PaypleApiClient paypleApiClient;

  /** 1. 일반 결제 인증 (기존 방식, 호환성 유지) */
  public void demonstrateGeneralPaymentAuth() {
    log.info("=== 일반 결제 인증 예시 ===");

    try {
      // LINKREG: 링크 결제 등록
      PaypleAuthResponseDTO linkregAuth = paypleApiClient.requestAuth("LINKREG");
      log.info("링크결제 등록 인증 성공 - AuthKey: {}", linkregAuth.getAuthKey());

      // AUTH: 일반 인증
      PaypleAuthResponseDTO generalAuth = paypleApiClient.requestAuth("AUTH");
      log.info("일반 인증 성공 - AuthKey: {}", generalAuth.getAuthKey());

    } catch (Exception e) {
      log.error("일반 결제 인증 실패", e);
    }
  }

  /** 2. 결제 취소 전용 인증 (새로운 방식) */
  public void demonstrateCancelAuth() {
    log.info("=== 결제 취소 전용 인증 예시 ===");

    try {
      // PCD_PAYCANCEL_FLAG: Y 파라미터가 자동으로 설정됨
      PaypleAuthResponseDTO cancelAuth = paypleApiClient.requestAuthForCancel();
      log.info("결제 취소 인증 성공 - AuthKey: {}", cancelAuth.getAuthKey());

    } catch (Exception e) {
      log.error("결제 취소 인증 실패", e);
    }
  }

  /** 3. 정산지급대행 계정 인증 (상점별 고유 코드) */
  public void demonstrateSettlementAccountAuth() {
    log.info("=== 정산지급대행 계정 인증 예시 ===");

    try {
      // 방법 1: 커스텀 코드 사용 (상점 고유 코드)
      String merchantCode = "MERCHANT001"; // 실제로는 상점별 고유 코드
      PaypleAuthResponseDTO accountAuth =
          paypleApiClient.requestAuthForSettlementAccount(merchantCode);
      log.info("정산지급대행 계정 인증 성공 - AuthKey: {}", accountAuth.getAuthKey());

      // 방법 2: 자동 생성 코드 사용
      PaypleAuthResponseDTO autoAccountAuth = paypleApiClient.requestAuthForSettlementAccount(null);
      log.info("자동생성 코드로 계정 인증 성공 - AuthKey: {}", autoAccountAuth.getAuthKey());

    } catch (Exception e) {
      log.error("정산지급대행 계정 인증 실패", e);
    }
  }

  /** 4. 정산지급대행 계좌 인증 (타임스탬프 기반) */
  public void demonstrateSettlementBankAuth() {
    log.info("=== 정산지급대행 계좌 인증 예시 ===");

    try {
      // 타임스탬프 기반 코드로 계좌 인증
      PaypleAuthResponseDTO bankAuth = paypleApiClient.requestAuthForSettlementBank();
      log.info("정산지급대행 계좌 인증 성공 - AuthKey: {}", bankAuth.getAuthKey());

    } catch (Exception e) {
      log.error("정산지급대행 계좌 인증 실패", e);
    }
  }

  /** 5. 고수준 API - 계좌 검증 */
  public void demonstrateAccountVerification() {
    log.info("=== 계좌 검증 예시 ===");

    try {
      PaypleAuthResponseDTO verificationResult = paypleApiClient.requestAccountVerification();
      log.info("계좌 검증 성공 - AuthKey: {}", verificationResult.getAuthKey());

    } catch (Exception e) {
      log.error("계좌 검증 실패", e);
    }
  }

  /** 6. 고수준 API - 계정 등록 */
  public void demonstrateAccountRegistration() {
    log.info("=== 계정 등록 예시 ===");

    try {
      String merchantId = "GROBLE_MERCHANT_" + System.currentTimeMillis() % 10000;
      PaypleAuthResponseDTO registrationResult =
          paypleApiClient.requestAccountRegistration(merchantId);
      log.info("계정 등록 성공 - AuthKey: {}", registrationResult.getAuthKey());

    } catch (Exception e) {
      log.error("계정 등록 실패", e);
    }
  }

  /** 전체 기능 테스트 */
  public void demonstrateAllFeatures() {
    log.info("🚀 PaypleApiClient 전체 기능 테스트 시작");

    demonstrateGeneralPaymentAuth();
    demonstrateCancelAuth();
    demonstrateSettlementAccountAuth();
    demonstrateSettlementBankAuth();
    demonstrateAccountVerification();
    demonstrateAccountRegistration();

    log.info("✅ PaypleApiClient 전체 기능 테스트 완료");
  }
}
