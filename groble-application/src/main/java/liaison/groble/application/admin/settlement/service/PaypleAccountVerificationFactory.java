package liaison.groble.application.admin.settlement.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.groble.application.admin.settlement.dto.PaypleAccountVerificationRequest;
import liaison.groble.domain.settlement.entity.Settlement;
import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.repository.SellerInfoRepository;
import liaison.groble.external.config.PaypleConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payple 계좌 인증 요청 생성을 담당하는 팩토리.
 *
 * <p>Settlement 실행 로직과 관리자 전용 계좌 인증 API에서 모두 재사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaypleAccountVerificationFactory {

  private final SellerInfoRepository sellerInfoRepository;
  private final PaypleConfig paypleConfig;

  /** SettlementItem 기반으로 Payple 계좌 인증 요청을 생성 */
  public PaypleAccountVerificationRequest buildForSettlementItem(SettlementItem settlementItem) {
    Settlement settlement = settlementItem.getSettlement();
    Long userId = settlement.getUser().getId();
    SellerInfo sellerInfo = getSellerInfo(userId);

    return buildRequest(userId, sellerInfo);
  }

  /** userId 기반으로 Payple 계좌 인증 요청을 생성 */
  public PaypleAccountVerificationRequest buildForUser(Long userId) {
    SellerInfo sellerInfo = getSellerInfo(userId);
    return buildRequest(userId, sellerInfo);
  }

  private SellerInfo getSellerInfo(Long userId) {
    return sellerInfoRepository
        .findByUserId(userId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "정산 대상 사용자의 SellerInfo를 찾을 수 없습니다. userId: " + userId));
  }

  private PaypleAccountVerificationRequest buildRequest(Long userId, SellerInfo sellerInfo) {
    validateSellerInfo(userId, sellerInfo);

    String accountHolderInfoType = determineAccountHolderInfoType(sellerInfo);
    String accountHolderInfo = determineAccountHolderInfo(sellerInfo);

    log.info(
        "계좌 인증 요청 생성 - userId: {}, bankCode: {}, accountOwner: {}, holderInfoType: {}, holderInfo: {}",
        userId,
        sellerInfo.getBankCode(),
        sellerInfo.getBankAccountOwner(),
        accountHolderInfoType,
        maskSensitiveData(accountHolderInfo));

    return PaypleAccountVerificationRequest.builder()
        .cstId(paypleConfig.getCstId())
        .custKey(paypleConfig.getCustKey())
        .bankCodeStd(sellerInfo.getBankCode())
        .accountNum(sellerInfo.getBankAccountNumber())
        .accountHolderInfoType(accountHolderInfoType)
        .accountHolderInfo(accountHolderInfo)
        .subId("groble_sub_" + userId)
        .build();
  }

  private void validateSellerInfo(Long userId, SellerInfo sellerInfo) {
    if (!StringUtils.hasText(sellerInfo.getBankCode())) {
      throw new IllegalArgumentException("은행 코드가 설정되지 않았습니다. userId: " + userId);
    }
    if (!StringUtils.hasText(sellerInfo.getBankAccountNumber())) {
      throw new IllegalArgumentException("계좌번호가 설정되지 않았습니다. userId: " + userId);
    }
    if (!StringUtils.hasText(sellerInfo.getBankAccountOwner())) {
      throw new IllegalArgumentException("예금주명이 설정되지 않았습니다. userId: " + userId);
    }
  }

  private String determineAccountHolderInfoType(SellerInfo sellerInfo) {
    Boolean businessSellerRequest = sellerInfo.getBusinessSellerRequest();
    Boolean isBusinessSeller = sellerInfo.getIsBusinessSeller();

    if (businessSellerRequest == null || !businessSellerRequest) {
      return "0";
    }

    if (isBusinessSeller == null || !isBusinessSeller) {
      return "0";
    }

    return "6";
  }

  private String determineAccountHolderInfo(SellerInfo sellerInfo) {
    Boolean businessSellerRequest = sellerInfo.getBusinessSellerRequest();
    Boolean isBusinessSeller = sellerInfo.getIsBusinessSeller();

    if (businessSellerRequest == null || !businessSellerRequest) {
      String birthDate = sellerInfo.getBirthDate();
      if (!StringUtils.hasText(birthDate)) {
        throw new IllegalArgumentException("개인 판매자의 생년월일이 설정되지 않았습니다.");
      }
      return birthDate;
    }

    if (isBusinessSeller == null || !isBusinessSeller) {
      String birthDate = sellerInfo.getBirthDate();
      if (!StringUtils.hasText(birthDate)) {
        throw new IllegalArgumentException("사업자 신청 중인 판매자의 생년월일이 설정되지 않았습니다.");
      }
      return birthDate;
    }

    String businessNumber = sellerInfo.getBusinessNumber();
    if (!StringUtils.hasText(businessNumber)) {
      throw new IllegalArgumentException("사업자 판매자의 사업자등록번호가 설정되지 않았습니다.");
    }
    return businessNumber;
  }

  private String maskSensitiveData(String value) {
    if (!StringUtils.hasText(value) || value.length() <= 8) {
      return "****";
    }
    return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
  }
}
