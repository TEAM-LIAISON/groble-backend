package liaison.groble.application.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import liaison.groble.application.admin.dto.AdminUserSummaryInfoDto;
import liaison.groble.common.response.PageResponse;
import liaison.groble.domain.user.dto.FlatAdminUserSummaryInfoDTO;
import liaison.groble.domain.user.repository.UserCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserCustomRepository userCustomRepository;

  public PageResponse<AdminUserSummaryInfoDto> getAllUsers(Pageable pageable) {
    Page<FlatAdminUserSummaryInfoDTO> userPage = userCustomRepository.findUsersByPageable(pageable);

    List<AdminUserSummaryInfoDto> items =
        userPage.getContent().stream().map(this::convertFlatDtoToInfoResponse).toList();

    PageResponse.MetaData meta =
        PageResponse.MetaData.builder()
            .sortBy(pageable.getSort().iterator().next().getProperty())
            .sortDirection(pageable.getSort().iterator().next().getDirection().name())
            .build();

    return PageResponse.from(userPage, items, meta);
  }

  private AdminUserSummaryInfoDto convertFlatDtoToInfoResponse(FlatAdminUserSummaryInfoDTO flat) {
    return AdminUserSummaryInfoDto.builder()
        .createdAt(flat.getCreatedAt())
        .isSellerTermsAgreed(flat.isSellerTermsAgreed())
        .nickname(flat.getNickname())
        .email(flat.getEmail())
        .phoneNumber(flat.getPhoneNumber())
        .isMarketingAgreed(flat.isMarketingAgreed())
        .isSellerInfo(flat.isSellerInfo())
        .verificationStatus(flat.getVerificationStatus())
        .isBusinessSeller(flat.isBusinessSeller())
        .build();
  }
}
