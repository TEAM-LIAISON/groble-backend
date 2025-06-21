package liaison.groble.application.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.auth.helper.TermsHelper;
import liaison.groble.application.auth.helper.TokenHelper;
import liaison.groble.application.auth.helper.UserHelper;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.service.UserStatusService;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountUserService {
  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;

  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;
  private final TokenHelper tokenHelper;
  private final UserHelper userHelper;

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
      user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.PENDING));
    } else {
      user.setSeller(false);
    }

    UserStatusService userStatusService = new UserStatusService();
    userStatusService.activate(user);

    termsHelper.processTermsAgreements(user, agreedTermsTypes);
  }
}
