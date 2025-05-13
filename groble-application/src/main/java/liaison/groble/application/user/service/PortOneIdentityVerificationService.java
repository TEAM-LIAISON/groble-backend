package liaison.groble.application.user.service;

import java.util.Map;

import liaison.groble.domain.user.entity.IdentityVerification;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.IdentityVerificationStatus;

public interface PortOneIdentityVerificationService {

  // 본인인증 요청 URL 생성
  String createVerificationRequest(
      User user, IdentityVerification.VerificationMethod type, String returnUrl);

  // 본인인증 결과 처리
  IdentityVerification processVerificationResult(String authCode, String state);

  // CI/DI 정보로 사용자 검증
  boolean validateUserByCiDi(String ci, String di, User user);

  // 인증 상태 조회
  IdentityVerificationStatus getVerificationStatus(String requestId);

  // 웹훅 처리
  void handleWebhook(Map<String, Object> webhookData);
}
