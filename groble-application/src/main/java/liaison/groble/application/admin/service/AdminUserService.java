package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.common.response.PageResponse;
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

  public PageResponse<AdminUserSummaryInfoDTO> getAllUsers(Pageable pageable) {
    Page<FlatAdminUserSummaryInfoDTO> userPage = userCustomRepository.findUsersByPageable(pageable);

    List<AdminUserSummaryInfoDTO> items =
        userPage.getContent().stream().map(this::convertFlatDTOToInfoResponse).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(userPage, items, meta);
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
}
