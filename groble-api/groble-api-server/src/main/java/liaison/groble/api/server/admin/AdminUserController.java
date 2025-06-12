package liaison.groble.api.server.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminUserSummaryInfo;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDto;
import liaison.groble.application.admin.service.AdminUserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "관리자 사용자 기능 관련 API", description = "관리자 사용자 조회 API")
public class AdminUserController {
  private final AdminUserService adminUserService;

  @AdminUserSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping("/users")
  public ResponseEntity<GrobleResponse<PageResponse<AdminUserSummaryInfoResponse>>> getAllUsers(
      @Auth Accessor accessor,
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "페이지당 사용자 수", example = "12")
          @RequestParam(value = "size", defaultValue = "12")
          int size,
      @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
          @RequestParam(value = "sort", defaultValue = "createdAt")
          String sort) {
    Pageable pageable = createPageable(page, size, sort);
    PageResponse<AdminUserSummaryInfoDto> response = adminUserService.getAllUsers(pageable);
    PageResponse<AdminUserSummaryInfoResponse> responsePage =
        toAdminUserSummaryInfoResponsePage(response);

    return ResponseEntity.ok(GrobleResponse.success(responsePage));
  }

  private Pageable createPageable(int page, int size, String sort) {
    // sort 파라미터가 없거나 빈 문자열인 경우 기본값 설정
    if (sort == null || sort.isBlank()) {
      sort = "createdAt";
    }

    // "property,direction" 형태로 분리
    String[] parts = sort.split(",");
    String property = parts[0].trim();
    Sort.Direction direction = Sort.Direction.DESC; // 기본 방향

    // direction 지정이 있으면 파싱 시도
    if (parts.length > 1 && !parts[1].isBlank()) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        log.warn("잘못된 정렬 방향: {}. DESC로 설정합니다.", parts[1].trim());
      }
    }

    return PageRequest.of(page, size, Sort.by(direction, property));
  }

  private PageResponse<AdminUserSummaryInfoResponse> toAdminUserSummaryInfoResponsePage(
      PageResponse<AdminUserSummaryInfoDto> dtoPage) {
    List<AdminUserSummaryInfoResponse> items =
        dtoPage.getItems().stream()
            .map(this::toAdminUserSummaryInfoResponseFromDto)
            .collect(Collectors.toList());

    return PageResponse.<AdminUserSummaryInfoResponse>builder()
        .items(items)
        .pageInfo(dtoPage.getPageInfo())
        .meta(dtoPage.getMeta())
        .build();
  }

  private AdminUserSummaryInfoResponse toAdminUserSummaryInfoResponseFromDto(
      AdminUserSummaryInfoDto infoDto) {
    return AdminUserSummaryInfoResponse.builder()
        .createdAt(infoDto.getCreatedAt())
        .isSellerInfo(infoDto.isSellerInfo())
        .nickname(infoDto.getNickname())
        .email(infoDto.getEmail())
        .isMarketingAgreed(infoDto.isMarketingAgreed())
        .phoneNumber(infoDto.getPhoneNumber())
        .isSellerInfo(infoDto.isSellerInfo())
        .verificationStatus(infoDto.getVerificationStatus())
        .isBusinessSeller(infoDto.isBusinessSeller())
        .build();
  }
}
