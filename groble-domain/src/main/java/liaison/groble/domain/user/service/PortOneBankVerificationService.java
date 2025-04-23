package liaison.groble.domain.user.service;

import java.util.Map;

import liaison.groble.domain.user.entity.BankAccount;
import liaison.groble.domain.user.entity.BankAccountVerification;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.BankAccountVerificationStatus;

public interface PortOneBankVerificationService {

  // 계좌 등록
  BankAccount registerBankAccount(
      User user, String bankCode, String accountNumber, String holderName);

  // 계좌 소유주 확인
  BankAccountVerification verifyAccountOwner(
      String bankCode, String accountNumber, String holderName);

  // 1원 입금 인증 시작
  BankAccountVerification startOneCentVerification(BankAccount bankAccount);

  // 1원 인증 확인
  BankAccountVerification confirmOneCentVerification(String verificationKey, String code);

  // 실시간 계좌 인증
  BankAccountVerification verifyAccountInstantly(
      String bankCode, String accountNumber, String holderName, String birthDate);

  // 인증 상태 조회
  BankAccountVerificationStatus getVerificationStatus(String verificationKey);

  // 웹훅 처리
  void handleWebhook(Map<String, Object> webhookData);
}
