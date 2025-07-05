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
import liaison.groble.application.market.dto.MarketLinkCheckDTO;
import liaison.groble.application.sell.SellerContactReader;
import liaison.groble.application.user.service.MakerReader;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateMarketLinkException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
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

  // Reader
  private final MakerReader makerReader;
  private final SellerContactReader sellerContactReader;
  private final ContentReader contentReader;
  private final UserReader userReader;
  private final SellerContactRepository sellerContactRepository;

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getEditIntroSection(Long userId) {
    User user = userReader.getUserById(userId);
    // 문의하기 정보 조회
    ContactInfoDTO contactInfo = getContactInfo(user);

    // 대표 콘텐츠 조회
    FlatContentPreviewDTO representativeContent = getRepresentativeContent(user);

    // 메이커 소개 섹션 빌드 결과
    return buildMarketIntroSectionResult(user, contactInfo, representativeContent);
  }

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getViewerMakerIntroSection(String marketName) {
    // 마켓 이름으로 메이커 조회
    User user = makerReader.getUserByMarketName(marketName);

    // 문의하기 정보 조회
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
    log.info("userId: {}, marketName: {}", user.getId(), marketName);
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

      if (marketEditDTO.getRepresentativeContentId() != null) {
        updateRepresentativeContent(user, marketEditDTO.getRepresentativeContentId());
        hasChanges = true;
      }
    } catch (Exception e) {
      log.error("Error occurred while editing market for user: {}", user.getId(), e);
      throw new RuntimeException("마켓 수정 중 오류가 발생했습니다.", e);
    }
  }

  public void checkMarketLink(MarketLinkCheckDTO marketLinkCheckDTO) {
    if (userReader.existsByMarketLinkUrl(marketLinkCheckDTO.getMarketLinkUrl())) {
      throw new DuplicateMarketLinkException("이미 사용 중인 링크입니다.");
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

  // TODO : 2회 이상 재사용되는 메서드 MarketService, PurchaseService 2곳에서 사용
  private ContactInfoDTO getContactInfo(User user) {
    try {
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

  /**
   * 사용자의 대표 콘텐츠를 업데이트합니다.
   *
   * @param user 사용자 엔티티
   * @param representativeContentId 새로운 대표 콘텐츠 ID (null일 경우 대표 콘텐츠 해제)
   */
  private void updateRepresentativeContent(User user, Long representativeContentId) {
    // 1. 현재 사용자의 기존 대표 콘텐츠 조회
    Optional<Content> currentRepresentativeContent =
        contentReader.findByUserAndIsRepresentativeTrue(user);

    // 2. 새로운 대표 콘텐츠 ID가 null인 경우 (대표 콘텐츠 해제)
    if (representativeContentId == null) {
      currentRepresentativeContent.ifPresent(
          content -> {
            content.setRepresentative(false);
            log.info("대표 콘텐츠가 해제되었습니다. User ID: {}, Content ID: {}", user.getId(), content.getId());
          });
      return;
    }

    // 3. 기존 대표 콘텐츠가 있는 경우
    if (currentRepresentativeContent.isPresent()) {
      Content currentContent = currentRepresentativeContent.get();

      // 3-1. 기존 대표 콘텐츠와 새로운 대표 콘텐츠가 같은 경우 - 변경 없음
      if (currentContent.getId().equals(representativeContentId)) {
        log.info("대표 콘텐츠가 동일합니다. 변경하지 않습니다. Content ID: {}", representativeContentId);
        return;
      }

      // 3-2. 다른 경우 - 기존 대표 콘텐츠 해제
      currentContent.setRepresentative(false);
      log.info(
          "기존 대표 콘텐츠를 해제합니다. User ID: {}, Old Content ID: {}",
          user.getId(),
          currentContent.getId());
    }

    // 4. 새로운 대표 콘텐츠 설정
    Content newRepresentativeContent = contentReader.findByIdAndUser(representativeContentId, user);

    newRepresentativeContent.setRepresentative(true);
    log.info(
        "새로운 대표 콘텐츠가 설정되었습니다. User ID: {}, New Content ID: {}",
        user.getId(),
        representativeContentId);
  }
}
