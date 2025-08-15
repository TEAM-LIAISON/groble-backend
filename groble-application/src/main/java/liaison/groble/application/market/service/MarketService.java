package liaison.groble.application.market.service;

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
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateMarketLinkException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.content.dto.FlatContentPreviewDTO;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.user.entity.SellerContact;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.ContactType;
import liaison.groble.domain.user.repository.SellerContactRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
  // Reader
  private final SellerContactReader sellerContactReader;
  private final ContentReader contentReader;
  private final UserReader userReader;
  private final SellerContactRepository sellerContactRepository;

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getEditIntroSection(Long userId) {
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    Market market = userReader.getMarket(userId);
    User user = sellerInfo.getUser();
    log.info("Get edit intro section");
    // 문의하기 정보 조회
    ContactInfoDTO contactInfo = getContactInfo(user);

    // 대표 콘텐츠 조회
    FlatContentPreviewDTO representativeContent = getRepresentativeContent(userId);
    log.info("Get representative content for user: {}", userId);

    // 나의 모든 콘텐츠 조회
    List<FlatContentPreviewDTO> myContents = contentReader.findAllMarketContentsByUserId(userId);
    log.info("Get market content for user: {}", userId);
    List<ContentCardDTO> items = myContents.stream().map(this::convertFlatDTOToCardDTO).toList();
    log.info("Get market content cards for user: {}", userId);
    // 메이커 소개 섹션 빌드 결과
    return buildEditMarketIntroSectionResult(
        user, sellerInfo, market, items, contactInfo, representativeContent);
  }

  @Transactional(readOnly = true)
  public MarketIntroSectionDTO getViewerMakerIntroSection(String marketLinkUrl) {
    // 마켓 이름으로 메이커 조회
    Market market = userReader.getMarketWithUser(marketLinkUrl);

    User user = market.getUser();

    SellerInfo sellerInfo = userReader.getSellerInfo(user.getId());
    // 문의하기 정보 조회
    ContactInfoDTO contactInfo = getContactInfo(user);

    // 대표 콘텐츠 조회
    FlatContentPreviewDTO representativeContent = getRepresentativeContent(user.getId());

    // 메이커 소개 섹션 빌드 결과
    return buildMarketIntroSectionResult(
        user, sellerInfo, market, contactInfo, representativeContent);
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentCardDTO> getMarketContents(String marketLinkUrl, Pageable pageable) {
    // 마켓 이름으로 메이커 조회
    Market market = userReader.getMarketWithUser(marketLinkUrl);

    Page<FlatContentPreviewDTO> page =
        contentReader.findAllMarketContentsByUserIdWithPaging(market.getUser().getId(), pageable);

    List<ContentCardDTO> items =
        page.getContent().stream().map(this::convertFlatDTOToCardDTO).toList();

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
    SellerInfo sellerInfo = userReader.getSellerInfoWithUser(userId);
    Market market = userReader.getMarket(userId);
    User user = sellerInfo.getUser();

    try {
      // 3. 변경 사항 추적을 위한 플래그
      boolean hasChanges = false;

      // 4. 각 필드별 null 체크 후 업데이트
      if (marketEditDTO.getMarketName() != null && !marketEditDTO.getMarketName().isBlank()) {
        market.changeMarketName(marketEditDTO.getMarketName());
        hasChanges = true;
      }

      if (marketEditDTO.getProfileImageUrl() != null
          && !marketEditDTO.getProfileImageUrl().isBlank()) {
        user.updateProfileImageUrl(marketEditDTO.getProfileImageUrl());
        hasChanges = true;
      }

      if (marketEditDTO.getMarketLinkUrl() != null && !marketEditDTO.getMarketLinkUrl().isBlank()) {
        market.changeMarketLinkUrl(marketEditDTO.getMarketLinkUrl());
        hasChanges = true;
      }

      if (marketEditDTO.getContactInfo() != null) {
        updateSellerContacts(user, marketEditDTO.getContactInfo());
        hasChanges = true;
      }

      if (marketEditDTO.getRepresentativeContentId() != null) {
        updateRepresentativeContent(user, marketEditDTO.getRepresentativeContentId());
        hasChanges = true;
      } else {
        clearRepresentativeContent(user); // 또는 null로 설정
      }
    } catch (Exception e) {
      log.error("Error occurred while editing market for user: {}", user.getId(), e);
      throw new RuntimeException("마켓 수정 중 오류가 발생했습니다.", e);
    }
  }

  public void checkMarketLink(String marketLinkUrl) {
    if (userReader.existsByMarketLinkUrl(marketLinkUrl)) {
      throw new DuplicateMarketLinkException("이미 사용 중인 링크입니다.");
    }
  }

  private void updateSellerContacts(User user, ContactInfoDTO contactInfo) {
    // 기존 연락처 모두 삭제
    sellerContactRepository.deleteAllByUser(user);

    // Instagram 처리
    if (contactInfo.getInstagram() != null && !contactInfo.getInstagram().isBlank()) {
      saveSellerContact(user, ContactType.INSTAGRAM, contactInfo.getInstagram());
    }

    // Email 처리
    if (contactInfo.getEmail() != null && !contactInfo.getEmail().isBlank()) {
      saveSellerContact(user, ContactType.EMAIL, contactInfo.getEmail());
    }

    // OpenChat 처리
    if (contactInfo.getOpenChat() != null && !contactInfo.getOpenChat().isBlank()) {
      saveSellerContact(user, ContactType.OPENCHAT, contactInfo.getOpenChat());
    }

    // Etc 처리
    if (contactInfo.getEtc() != null && !contactInfo.getEtc().isBlank()) {
      saveSellerContact(user, ContactType.ETC, contactInfo.getEtc());
    }
  }

  private void saveSellerContact(User user, ContactType contactType, String contactValue) {
    SellerContact sellerContact =
        SellerContact.builder()
            .user(user)
            .contactType(contactType)
            .contactValue(contactValue)
            .build();
    sellerContactRepository.save(sellerContact);
  }

  // TODO : 2회 이상 재사용되는 메서드 MarketService, PurchaseService 2곳에서 사용
  private ContactInfoDTO getContactInfo(User user) {
    List<SellerContact> contacts = sellerContactReader.getContactsByUser(user);
    return ContactInfoDTO.from(contacts);
  }

  private FlatContentPreviewDTO getRepresentativeContent(Long userId) {
    return contentReader.getRepresentativeContentByUser(userId);
  }

  private MarketIntroSectionDTO buildEditMarketIntroSectionResult(
      User user,
      SellerInfo sellerInfo,
      Market market,
      List<ContentCardDTO> contentCards,
      ContactInfoDTO contactInfo,
      FlatContentPreviewDTO flatContentPreviewDTO) {
    return MarketIntroSectionDTO.builder()
        .profileImageUrl(user.getProfileImageUrl())
        .marketName(getMarketNameSafely(market))
        .marketLinkUrl(getMarketLinkUrlSafely(market))
        .verificationStatus(getVerificationStatusSafely(sellerInfo))
        .contactInfo(Optional.ofNullable(contactInfo).orElse(ContactInfoDTO.builder().build()))
        .representativeContent(flatContentPreviewDTO)
        .contentCardList(contentCards)
        .build();
  }

  private MarketIntroSectionDTO buildMarketIntroSectionResult(
      User user,
      SellerInfo sellerInfo,
      Market market,
      ContactInfoDTO contactInfo,
      FlatContentPreviewDTO flatContentPreviewDTO) {
    return MarketIntroSectionDTO.builder()
        .profileImageUrl(user.getProfileImageUrl())
        .marketName(getMarketNameSafely(market))
        .marketLinkUrl(getMarketLinkUrlSafely(market))
        .verificationStatus(getVerificationStatusSafely(sellerInfo))
        .contactInfo(Optional.ofNullable(contactInfo).orElse(ContactInfoDTO.builder().build()))
        .representativeContent(flatContentPreviewDTO)
        .build();
  }

  private String getMarketNameSafely(Market market) {
    return Optional.ofNullable(market).map(Market::getMarketName).orElse("");
  }

  private String getMarketLinkUrlSafely(Market market) {
    return Optional.ofNullable(market).map(Market::getMarketLinkUrl).orElse("");
  }

  private String getVerificationStatusSafely(SellerInfo sellerInfo) {
    return Optional.ofNullable(sellerInfo)
        .map(SellerInfo::getVerificationStatus)
        .map(Enum::name)
        .orElse("UNVERIFIED");
  }

  private ContentCardDTO convertFlatDTOToCardDTO(FlatContentPreviewDTO flat) {
    return ContentCardDTO.builder()
        .contentId(flat.getContentId())
        .createdAt(flat.getCreatedAt())
        .title(flat.getTitle())
        .thumbnailUrl(flat.getThumbnailUrl())
        .sellerName(flat.getSellerName())
        .lowestPrice(flat.getLowestPrice())
        .priceOptionLength(flat.getPriceOptionLength())
        .isAvailableForSale(flat.getIsAvailableForSale())
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

  private void clearRepresentativeContent(User user) {
    Optional<Content> currentRepresentativeContent =
        contentReader.findByUserAndIsRepresentativeTrue(user);
    // 2. 새로운 대표 콘텐츠 ID가 null인 경우 (대표 콘텐츠 해제)
    currentRepresentativeContent.ifPresent(
        content -> {
          content.setRepresentative(false);
          log.info("대표 콘텐츠가 해제되었습니다. User ID: {}, Content ID: {}", user.getId(), content.getId());
        });
    return;
  }
}
