package liaison.groble.application.admin.service;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.admin.dto.AdminAccountVerificationResultDTO;
import liaison.groble.application.admin.dto.AdminBusinessInfoUpdateDTO;
import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.application.admin.settlement.dto.PayplePartnerAuthResult;
import liaison.groble.application.admin.settlement.service.PaypleAccountVerificationFactory;
import liaison.groble.application.admin.settlement.service.PaypleSettlementService;
import liaison.groble.application.payment.exception.PaypleApiException;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.user.entity.SellerInfo;

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
  private final UserReader userReader;

  public AdminAccountVerificationResultDTO verifyAccount(String targetNickname) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(targetNickname);
    Long targetUserId = sellerInfo.getUser().getId();

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
        "관리자 계좌 인증 처리 완료 - nickname: {}, userId: {}, success: {}, resultCode: {}",
        targetNickname,
        targetUserId,
        resultDTO.isSuccess(),
        resultDTO.getResultCode());

    return resultDTO;
  }

  @Transactional
  public void updateBusinessInfo(Long targetUserId, AdminBusinessInfoUpdateDTO dto) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(targetUserId);

    sellerInfo.updateBusinessInfo(
        dto.getBusinessType(),
        sellerInfo.getBusinessNumber(),
        sellerInfo.getBusinessCategory(),
        sellerInfo.getBusinessSector(),
        dto.getBusinessName(),
        dto.getRepresentativeName(),
        dto.getBusinessAddress(),
        sellerInfo.getBusinessLicenseFileUrl(),
        sellerInfo.getTaxInvoiceEmail());

    log.info(
        "관리자 사업자 정보 수정 완료 - userId: {}, businessType: {}, businessName: {}",
        targetUserId,
        dto.getBusinessType(),
        dto.getBusinessName());
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
