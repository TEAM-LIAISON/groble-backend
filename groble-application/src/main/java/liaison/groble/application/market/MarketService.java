package liaison.groble.application.market;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.content.dto.ContentCardDTO;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketEditDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.application.user.service.MakerReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.ContactType;
import liaison.groble.domain.user.repository.SellerContactRepository;
import liaison.groble.domain.user.vo.SellerInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

  private final MakerReader makerReader;
  private final SellerContactReader sellerContactReader;
  private final SellerContactRepository sellerContactRepository;
  private final ContentReader contentReader;
  private final UserReader userReader;

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getViewerMakerIntroSection(String marketName) {
    // 마켓 이름으로 메이커 조회
    User user = makerReader.getUserByMarketName(marketName);

    // 연락처 정보 조회
    ContactInfoDTO contactInfo = getContactInfo(user);

    // 대표 콘텐츠 조회
    FlatContentPreviewDTO representativeContent = getRepresentativeContent(user);

    // 메이커 소개 섹션 빌드 결과
    return buildMarketIntroSectionResult(user, contactInfo, representativeContent);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentCardDTO> getMarketContents(String marketName, Pageable pageable) {
    // 마켓 이름으로 메이커 조회
    User user = makerReader.getUserByMarketName(marketName);
    Page<FlatContentPreviewDTO> page =
        contentReader.findAllMarketContentsByUserId(user.getId(), pageable);
    List<ContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDtoToCardDto).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(page, items, meta);
  }

  @Transactional
  public void editMarket(Long userId, MarketEditDTO marketEditDTO) {
    // userId로 사용자 조회
    User user = userReader.getUserById(userId);

    try {
      // 3. 변경 사항 추적을 위한 플래그
      boolean hasChanges = false;

      // 4. 각 필드별 null 체크 후 업데이트
      if (marketEditDTO.getMarketName() != null && !marketEditDTO.getMarketName().isBlank()) {
        user.updateMarketName(marketEditDTO.getMarketName());
        hasChanges = true;
      }

      if (marketEditDTO.getProfileImageUrl() != null
          && !marketEditDTO.getProfileImageUrl().isBlank()) {
        user.updateProfileImageUrl(marketEditDTO.getProfileImageUrl());
        hasChanges = true;
      }

      if (marketEditDTO.getMarketLinkUrl() != null && !marketEditDTO.getMarketLinkUrl().isBlank()) {
        user.updateMarketLinkUrl(marketEditDTO.getMarketLinkUrl());
        hasChanges = true;
      }

      if (marketEditDTO.getContactInfo() != null) {
        updateSellerContacts(user, marketEditDTO.getContactInfo());
        hasChanges = true;
      }
    } catch (Exception e) {
      log.error("Error occurred while editing market for user: {}", user.getId(), e);
      throw new RuntimeException("마켓 수정 중 오류가 발생했습니다.", e);
    }
  }

  private void updateSellerContacts(User user, ContactInfoDTO contactInfo) {
    // Instagram 처리
    if (contactInfo.getInstagram() != null && !contactInfo.getInstagram().isBlank()) {
      updateOrCreateSellerContact(user, ContactType.INSTAGRAM, contactInfo.getInstagram());
    }

    // Email 처리
    if (contactInfo.getEmail() != null && !contactInfo.getEmail().isBlank()) {
      updateOrCreateSellerContact(user, ContactType.EMAIL, contactInfo.getEmail());
    }

    // OpenChat 처리
    if (contactInfo.getOpenChat() != null && !contactInfo.getOpenChat().isBlank()) {
      updateOrCreateSellerContact(user, ContactType.OPENCHAT, contactInfo.getOpenChat());
    }

    // Etc 처리
    if (contactInfo.getEtc() != null && !contactInfo.getEtc().isBlank()) {
      updateOrCreateSellerContact(user, ContactType.ETC, contactInfo.getEtc());
    }
  }

  private void updateOrCreateSellerContact(
      User user, ContactType contactType, String contactValue) {
    SellerContact sellerContact =
        sellerContactReader.findByUserAndContactType(user, contactType).orElse(null);

    if (sellerContact != null) {
      sellerContact.updateContactValue(contactValue);
    } else {
      // 없으면 새로 생성
      sellerContact =
          SellerContact.builder()
              .user(user)
              .contactType(contactType)
              .contactValue(contactValue)
              .build();
      sellerContactRepository.save(sellerContact);
    }
  }

  private ContactInfoDTO getContactInfo(User user) {
    try {
      // TODO: SellerContactReader를 통해 연락처 정보 조회
      List<SellerContact> contacts = sellerContactReader.getContactsByUser(user);
      return ContactInfoDTO.from(contacts);
    } catch (Exception e) {
      return ContactInfoDTO.builder().build();
    }
  }

  private FlatContentPreviewDTO getRepresentativeContent(User user) {
    try {
      return contentReader.getRepresentativeContentByUser(user);
    } catch (Exception e) {
      return FlatContentPreviewDTO.builder().build();
    }
  }

  private MarketIntroSectionDTO buildMarketIntroSectionResult(
      User user, ContactInfoDTO contactInfo, FlatContentPreviewDTO flatContentPreviewDTO) {
    return MarketIntroSectionDTO.builder()
        .profileImageUrl(user.getProfileImageUrl())
        .marketName(getMarketNameSafely(user))
        .verificationStatus(getVerificationStatusSafely(user))
        .contactInfo(Optional.ofNullable(contactInfo).orElse(ContactInfoDTO.builder().build()))
        .representativeContent(flatContentPreviewDTO)
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

  /** FlatPreviewContentDTO를 ContentCardDto로 변환합니다. */
  private ContentCardDTO convertFlatDtoToCardDto(FlatContentPreviewDTO flat) {
    return ContentCardDTO.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .lowestPrice(flat.getLowestPrice())
        .priceOptionLength(flat.getPriceOptionLength())
        .status(flat.getStatus())
        .build();
  }
}
