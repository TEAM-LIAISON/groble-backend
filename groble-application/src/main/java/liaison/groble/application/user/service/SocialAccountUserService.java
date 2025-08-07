package liaison.groble.application.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.auth.helper.TermsHelper;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.market.repository.MarketRepository;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.repository.SellerInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountUserService {
  // Repository
  private final UserReader userReader;
  private final SellerInfoRepository sellerInfoRepository;
  private final MarketRepository marketRepository;
  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;

  @Transactional
  public void setSocialAccountBasicInfo(Long userId, SocialBasicInfoDTO socialBasicInfoDTO) {
    User user = userReader.getUserById(userId);
    UserType userType =
        authValidationHelper.validateAndParseUserType(socialBasicInfoDTO.getUserType());

    // 약관 유형 변환 및 필수 약관 검증
    List<TermsType> agreedTermsTypes =
        termsHelper.convertToTermsTypes(socialBasicInfoDTO.getTermsTypeStrings());
    termsHelper.validateRequiredTermsAgreement(agreedTermsTypes, userType);
    termsHelper.processTermsAgreements(user, agreedTermsTypes);
    // 사용자 정보 업데이트
    user.updateLastUserType(userType);
    if (userType == UserType.SELLER) {
      user.setSeller(true);
      SellerInfo sellerInfo = SellerInfo.createForUser(user);
      sellerInfoRepository.save(sellerInfo);
      Market market = Market.createForUser(user);
      marketRepository.save(market);
    } else {
      user.setSeller(false);
    }

    user.getUserStatusInfo().updateStatus(UserStatus.ACTIVE);

    termsHelper.processTermsAgreements(user, agreedTermsTypes);
  }
}
