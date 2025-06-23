package liaison.groble.application.market;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.application.user.service.MakerReader;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

  private final MakerReader makerReader;

  // SellerContact 조회를 위한 Reader 필요 (추가해야 할 의존성)
  // private final SellerContactReader sellerContactReader;
  // private final ContentReader contentReader;

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getViewerMakerIntroSection(String marketName) {
    // 마켓 이름으로 메이커 조회
    User user = makerReader.getUserByMarketName(marketName);

    // 연락처 정보 조회
    ContactInfoDTO contactInfo = getContactInfo(user);

    // 대표 콘텐츠 조회
    ContentCardDTO representativeContent = getRepresentativeContent(user);

    // 메이커 소개 섹션 빌드 결과
    return buildMarketIntroSectionResult(user, contactInfo, representativeContent);
  }

  private ContactInfoDTO getContactInfo(User user) {
    try {
      // TODO: SellerContactReader를 통해 연락처 정보 조회
      // List<SellerContact> contacts = sellerContactReader.getContactsByUser(user);
      // return ContactInfoDTO.fromSellerContacts(contacts);

      // 임시로 빈 ContactInfoDTO 반환
      return ContactInfoDTO.builder().build();

    } catch (Exception e) {
      log.error("Error occurred while getting contact info for user: {}", user.getId(), e);
      return ContactInfoDTO.builder().build();
    }
  }

  private ContentCardDTO getRepresentativeContent(User user) {
    try {
      // TODO: ContentReader를 통해 대표 콘텐츠 조회
      // return contentReader.getRepresentativeContentByUser(user);

      // 임시로 null 반환
      return null;

    } catch (Exception e) {
      log.error(
          "Error occurred while getting representative content for user: {}", user.getId(), e);
      return null;
    }
  }

  private MarketIntroSectionDTO buildMarketIntroSectionResult(
      User user, ContactInfoDTO contactInfo, ContentCardDTO contentCardDTO) {
    return MarketIntroSectionDTO.builder()
        .profileImageUrl(user.getProfileImageUrl())
        .marketName(getMarketNameSafely(user))
        .verificationStatus(getVerificationStatusSafely(user))
        .contactInfo(Optional.ofNullable(contactInfo).orElse(ContactInfoDTO.builder().build()))
        .representativeContent(contentCardDTO) // null 허용
        .build();
  }

  private String getMarketNameSafely(User user) {
    try {
      return Optional.ofNullable(user.getSellerInfo()).map(SellerInfo::getMarketName).orElse("");
    } catch (Exception e) {
      log.warn("Error getting market name for user: {}", user.getId(), e);
      return "";
    }
  }

  private String getVerificationStatusSafely(User user) {
    try {
      return Optional.ofNullable(user.getSellerInfo())
          .map(SellerInfo::getVerificationStatus)
          .map(Enum::name)
          .orElse("UNVERIFIED");
    } catch (Exception e) {
      log.warn("Error getting verification status for user: {}", user.getId(), e);
      return "UNVERIFIED";
    }
  }
}
