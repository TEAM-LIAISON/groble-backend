package liaison.groble.application.admin.service;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import liaison.groble.application.admin.dto.AdminAccountVerificationResultDTO;
import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.admin.settlement.service.PaypleAccountVerificationFactory;
import liaison.groble.application.admin.settlement.service.PaypleSettlementService;
import liaison.groble.application.payment.exception.PaypleApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 관리자 전용 계좌 인증 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAccountVerificationService {

  private static final String PAYPLE_SUCCESS_CODE = "A0000";

  private final PaypleSettlementService paypleSettlementService;
  private final PaypleAccountVerificationFactory paypleAccountVerificationFactory;

  public AdminAccountVerificationResultDTO verifyAccount(Long targetUserId) {
    PayplePartnerAuthResult authResult = paypleSettlementService.requestPartnerAuth();

    if (!authResult.isSuccess()) {
      throw new PaypleApiException("페이플 파트너 인증 실패: " + authResult.getMessage());
    }

    PaypleAccountVerificationRequest request =
        paypleAccountVerificationFactory.buildForUser(targetUserId);

    JSONObject verificationResult =
        paypleSettlementService.requestAccountVerification(request, authResult.getAccessToken());

    AdminAccountVerificationResultDTO resultDTO = buildResultDTO(verificationResult);

    log.info(
        "관리자 계좌 인증 처리 완료 - userId: {}, success: {}, resultCode: {}",
        targetUserId,
        resultDTO.isSuccess(),
        resultDTO.getResultCode());

    return resultDTO;
  }

  private AdminAccountVerificationResultDTO buildResultDTO(JSONObject verificationResult) {
    if (verificationResult == null) {
      return AdminAccountVerificationResultDTO.builder()
          .success(false)
          .resultCode("UNKNOWN")
          .message("계좌 인증 결과가 존재하지 않습니다.")
          .build();
    }

    String resultCode = getString(verificationResult, "result");
    String message = getString(verificationResult, "message");

    boolean success = PAYPLE_SUCCESS_CODE.equals(resultCode);

    return AdminAccountVerificationResultDTO.builder()
        .success(success)
        .resultCode(resultCode)
        .message(message)
        .billingTranId(getString(verificationResult, "billing_tran_id"))
        .apiTranDtm(getString(verificationResult, "api_tran_dtm"))
        .bankTranId(getString(verificationResult, "bank_tran_id"))
        .bankTranDate(getString(verificationResult, "bank_tran_date"))
        .bankRspCode(getString(verificationResult, "bank_rsp_code"))
        .bankCodeStd(getString(verificationResult, "bank_code_std"))
        .bankCodeSub(getString(verificationResult, "bank_code_sub"))
        .build();
  }

  private String getString(JSONObject json, String key) {
    if (json == null) {
      return null;
    }
    Object value = json.get(key);
    return value != null ? value.toString() : null;
  }
}
