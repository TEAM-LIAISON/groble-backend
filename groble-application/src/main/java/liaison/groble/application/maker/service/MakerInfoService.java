package liaison.groble.application.maker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.maker.dto.BusinessMakerInfoDTO;
import liaison.groble.application.maker.dto.MakerInfoDTO;
import liaison.groble.application.maker.dto.MakerType;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MakerInfoService {

  private static final String DEFAULT_UNVERIFIED_LABEL = "메이커 인증 전";
  private static final String VERIFIED_LABEL = "인증 완료 ✅";
  private static final String HYPHEN = "-";

  private final ContentReader contentReader;
  private final UserReader userReader;

  public MakerInfoDTO getMakerInfoByContentId(Long contentId) {
    Content content = contentReader.getContentWithSeller(contentId);
    User user = content.getUser();
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(user.getId());
    return buildMakerInfo(user, sellerInfo);
  }

  public MakerInfoDTO getMakerInfoByMarketLink(String marketLinkUrl) {
    Market market = userReader.getMarketWithUser(marketLinkUrl);
    User user = market.getUser();
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(user.getId());
    return buildMakerInfo(user, sellerInfo);
  }

  private MakerInfoDTO buildMakerInfo(User user, SellerInfo sellerInfo) {
    SellerVerificationStatus status = sellerInfo.getVerificationStatus();
    boolean verified = status == SellerVerificationStatus.VERIFIED;
    boolean businessMaker = Boolean.TRUE.equals(sellerInfo.getIsBusinessSeller());

    MakerType makerType = resolveMakerType(verified, businessMaker);
    String verificationStatusLabel = resolveVerificationLabel(status, verified);
    String name = resolveName(makerType, sellerInfo, user);
    String email = resolveEmail(user, sellerInfo);
    String phoneNumber = resolvePhoneNumber(user);

    BusinessMakerInfoDTO businessInfo = null;
    if (makerType == MakerType.BUSINESS) {
      businessInfo =
          BusinessMakerInfoDTO.builder()
              .businessName(optionalOrHyphen(sellerInfo.getBusinessName()))
              .representativeName(optionalOrHyphen(sellerInfo.getRepresentativeName()))
              .businessNumber(optionalOrHyphen(sellerInfo.getBusinessNumber()))
              .businessAddress(optionalOrHyphen(sellerInfo.getBusinessAddress()))
              .build();
    }

    return MakerInfoDTO.builder()
        .makerType(makerType)
        .verified(verified)
        .verificationStatus(status.name())
        .verificationStatusLabel(verificationStatusLabel)
        .name(name)
        .email(email)
        .phoneNumber(phoneNumber)
        .businessInfo(businessInfo)
        .build();
  }

  private MakerType resolveMakerType(boolean verified, boolean businessMaker) {
    if (!verified) {
      return MakerType.UNVERIFIED;
    }
    return businessMaker ? MakerType.BUSINESS : MakerType.PERSONAL;
  }

  private String resolveVerificationLabel(SellerVerificationStatus status, boolean verified) {
    if (verified) {
      return VERIFIED_LABEL;
    }
    if (status == SellerVerificationStatus.PENDING) {
      return DEFAULT_UNVERIFIED_LABEL;
    }
    return Optional.ofNullable(status.getDisplayName()).orElse(DEFAULT_UNVERIFIED_LABEL);
  }

  private String resolveName(MakerType makerType, SellerInfo sellerInfo, User user) {
    return switch (makerType) {
      case PERSONAL -> optionalOrHyphen(sellerInfo.getBankAccountOwner());
      case BUSINESS -> optionalOrHyphen(sellerInfo.getRepresentativeName());
      default -> HYPHEN;
    };
  }

  private String resolveEmail(User user, SellerInfo sellerInfo) {
    String email = user.getEmail();
    if (StringUtils.hasText(email)) {
      return email;
    }
    return optionalOrHyphen(sellerInfo.getTaxInvoiceEmail());
  }

  private String resolvePhoneNumber(User user) {
    return optionalOrHyphen(user.getPhoneNumber());
  }

  private String optionalOrHyphen(String value) {
    return StringUtils.hasText(value) ? value : HYPHEN;
  }
}
