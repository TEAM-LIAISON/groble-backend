package liaison.groble.application.admin.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import liaison.groble.application.admin.dto.AdminGuestUserSummaryDTO;
import liaison.groble.application.admin.dto.AdminHomeTestContactDTO;
import liaison.groble.application.admin.dto.AdminUserStatisticsDTO;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;
import liaison.groble.domain.hometest.entity.HomeTestContact;
import liaison.groble.domain.hometest.repository.HomeTestContactRepository;
import liaison.groble.domain.user.dto.AdminUserStatisticsAggregate;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.repository.UserCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserCustomRepository userCustomRepository;
  private final GuestUserRepository guestUserRepository;
  private final HomeTestContactRepository homeTestContactRepository;

  public PageResponse<AdminUserSummaryInfoDTO> getAllUsers(Pageable pageable) {
    Page<FlatAdminUserSummaryInfoDTO> userPage = userCustomRepository.findUsersByPageable(pageable);

    List<AdminUserSummaryInfoDTO> items =
        userPage.getContent().stream().map(this::convertFlatDTOToInfoResponse).toList();

    return PageResponse.from(userPage, items, resolveSortMeta(pageable));
  }

  public AdminUserSummaryInfoDTO getUserByNickname(String nickname) {
    String normalizedNickname = nickname == null ? "" : nickname.trim();

    if (!StringUtils.hasText(normalizedNickname)) {
      throw new IllegalArgumentException("검색할 닉네임을 입력해주세요.");
    }

    FlatAdminUserSummaryInfoDTO flatUser =
        userCustomRepository
            .findUserByNickname(normalizedNickname)
            .orElseThrow(() -> new EntityNotFoundException("해당 닉네임의 회원을 찾을 수 없습니다."));

    return convertFlatDTOToInfoResponse(flatUser);
  }

  public PageResponse<AdminGuestUserSummaryDTO> getAllGuestUsers(Pageable pageable) {
    Page<GuestUser> guestPage = guestUserRepository.findAll(pageable);

    List<AdminGuestUserSummaryDTO> items =
        guestPage.getContent().stream().map(this::convertGuestUserToSummary).toList();

    return PageResponse.from(guestPage, items, resolveSortMeta(pageable));
  }

  public PageResponse<AdminHomeTestContactDTO> getAllHomeTestContacts(Pageable pageable) {
    Page<HomeTestContact> contactPage = homeTestContactRepository.findAll(pageable);

    List<AdminHomeTestContactDTO> items =
        contactPage.getContent().stream().map(this::convertContactToSummary).toList();

    return PageResponse.from(contactPage, items, resolveSortMeta(pageable));
  }

  public AdminUserStatisticsDTO getUserStatistics() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sevenDaysAgo = now.minusDays(7);
    LocalDateTime thirtyDaysAgo = now.minusDays(30);

    AdminUserStatisticsAggregate aggregate =
        userCustomRepository.fetchUserStatistics(sevenDaysAgo, thirtyDaysAgo);

    long totalUsers = aggregate.totalActiveUsers();
    long withdrawnUsers = aggregate.withdrawnUsers();
    long newUsers7Days = aggregate.newUsers7Days();
    long newUsers30Days = aggregate.newUsers30Days();
    long buyerOnlyCount = aggregate.buyerOnlyCount();
    long buyerAndSellerCount = aggregate.buyerAndSellerCount();
    long marketingAgreedCount = aggregate.marketingAgreedCount();
    long phoneNumberProvidedCount = aggregate.phoneNumberProvidedCount();
    long phoneNumberNotProvidedCount = Math.max(0, totalUsers - phoneNumberProvidedCount);
    long sellerTermsAgreedCount = aggregate.sellerTermsAgreedCount();

    long verificationVerified = aggregate.verificationVerifiedCount();
    long verificationPending = aggregate.verificationPendingCount();
    long verificationInProgress = aggregate.verificationInProgressCount();
    long verificationFailed = aggregate.verificationFailedCount();
    long verificationNone = aggregate.verificationNoneCount();

    long verificationBase = verificationVerified + verificationFailed + verificationInProgress;

    AdminUserStatisticsDTO.VerificationStats verificationStats =
        AdminUserStatisticsDTO.VerificationStats.builder()
            .verified(verificationVerified)
            .pending(verificationPending)
            .inProgress(verificationInProgress)
            .failed(verificationFailed)
            .none(verificationNone)
            .build();

    long businessSimplified = aggregate.businessTypeIndividualSimplifiedCount();
    long businessNormal = aggregate.businessTypeIndividualNormalCount();
    long businessCorporate = aggregate.businessTypeCorporateCount();
    long individualMakerCount = aggregate.businessTypeIndividualMakerCount();
    long businessNone = aggregate.businessTypeNoneCount();

    AdminUserStatisticsDTO.BusinessTypeStats businessTypeStats =
        AdminUserStatisticsDTO.BusinessTypeStats.builder()
            .individualSimplified(businessSimplified)
            .individualNormal(businessNormal)
            .corporate(businessCorporate)
            .individualMaker(individualMakerCount)
            .none(businessNone)
            .build();

    return AdminUserStatisticsDTO.builder()
        .totalUsers(totalUsers)
        .withdrawnUsers(withdrawnUsers)
        .newUsers7Days(newUsers7Days)
        .newUsers30Days(newUsers30Days)
        .buyerOnlyCount(buyerOnlyCount)
        .buyerAndSellerCount(buyerAndSellerCount)
        .buyerOnlyPercentage(calculatePercentage(buyerOnlyCount, totalUsers))
        .buyerAndSellerPercentage(calculatePercentage(buyerAndSellerCount, totalUsers))
        .marketingAgreedCount(marketingAgreedCount)
        .marketingAgreedPercentage(calculatePercentage(marketingAgreedCount, totalUsers))
        .phoneNumberProvidedCount(phoneNumberProvidedCount)
        .phoneNumberProvidedPercentage(calculatePercentage(phoneNumberProvidedCount, totalUsers))
        .phoneNumberNotProvidedCount(phoneNumberNotProvidedCount)
        .phoneNumberNotProvidedPercentage(
            calculatePercentage(phoneNumberNotProvidedCount, totalUsers))
        .sellerTermsAgreedCount(sellerTermsAgreedCount)
        .sellerTermsAgreedPercentage(calculatePercentage(sellerTermsAgreedCount, totalUsers))
        .verificationStats(verificationStats)
        .verificationSuccessRate(calculatePercentage(verificationVerified, verificationBase))
        .businessTypeStats(businessTypeStats)
        .build();
  }

  private AdminUserSummaryInfoDTO convertFlatDTOToInfoResponse(FlatAdminUserSummaryInfoDTO flat) {
    return AdminUserSummaryInfoDTO.builder()
        .createdAt(flat.getCreatedAt())
        .isSellerTermsAgreed(flat.isSellerTermsAgreed())
        .nickname(flat.getNickname())
        .email(flat.getEmail())
        .phoneNumber(flat.getPhoneNumber())
        .userStatus(flat.getUserStatus())
        .isMarketingAgreed(flat.isMarketingAgreed())
        .isSellerInfo(flat.isSellerInfo())
        .verificationStatus(flat.getVerificationStatus())
        .isBusinessSeller(flat.isBusinessSeller())
        .businessType(flat.getBusinessType())
        .withdrawalReason(
            resolveWithdrawalReason(
                flat.getUserStatus(),
                flat.getWithdrawalReason(),
                flat.getWithdrawalAdditionalComment()))
        .build();
  }

  private String resolveWithdrawalReason(
      String userStatus, String reasonCode, String additionalComment) {
    if (!"WITHDRAWN".equalsIgnoreCase(userStatus)) {
      return null;
    }

    String formattedReason = null;

    if (StringUtils.hasText(reasonCode)) {
      try {
        formattedReason = WithdrawalReason.valueOf(reasonCode).getDescription();
      } catch (IllegalArgumentException e) {
        formattedReason = reasonCode;
      }
    }

    if (!StringUtils.hasText(additionalComment)) {
      return formattedReason;
    }

    if (!StringUtils.hasText(formattedReason)) {
      return additionalComment;
    }

    return formattedReason + " - " + additionalComment;
  }

  private AdminGuestUserSummaryDTO convertGuestUserToSummary(GuestUser guestUser) {
    return AdminGuestUserSummaryDTO.builder()
        .id(guestUser.getId())
        .createdAt(guestUser.getCreatedAt())
        .username(guestUser.getUsername())
        .phoneNumber(guestUser.getPhoneNumber())
        .email(guestUser.getEmail())
        .phoneVerificationStatus(guestUser.getPhoneVerificationStatus().name())
        .phoneVerifiedAt(guestUser.getPhoneVerifiedAt())
        .verificationExpiresAt(guestUser.getVerificationExpiresAt())
        .build();
  }

  private AdminHomeTestContactDTO convertContactToSummary(HomeTestContact contact) {
    return AdminHomeTestContactDTO.builder()
        .id(contact.getId())
        .createdAt(contact.getCreatedAt())
        .updatedAt(contact.getUpdatedAt())
        .phoneNumber(contact.getPhoneNumber())
        .email(contact.getEmail())
        .nickname(contact.getNickname())
        .build();
  }

  private PageResponse.MetaData resolveSortMeta(Pageable pageable) {
    if (pageable.getSort().isEmpty()) {
      return null;
    }

    var order = pageable.getSort().iterator().next();
    return PageResponse.MetaData.builder()
        .sortBy(order.getProperty())
        .sortDirection(order.getDirection().name())
        .build();
  }

  private double calculatePercentage(long numerator, long denominator) {
    if (denominator <= 0 || numerator <= 0) {
      return 0.0;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP)
        .doubleValue();
  }
}
