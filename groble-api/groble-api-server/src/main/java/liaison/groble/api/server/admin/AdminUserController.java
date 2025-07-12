package liaison.groble.api.server.admin;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminUserSummaryInfo;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminUserService;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminUserMapper;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name = "[✅ 관리자] 관리자 전체 사용자 목록 조회 API", description = "DB에 저장된 모든 사용자 정보를 조회합니다.")
public class AdminUserController {
  // API 경로 상수화
  private static final String ADMIN_USER_SUMMARY_INFO_PATH = "/users";
  // 응답 메시지 상수화
  private static final String ADMIN_USER_SUMMARY_INFO_SUCCESS_MESSAGE = "관리자 전체 사용자 목록 조회에 성공했습니다.";
  // Service
  private final AdminUserService adminUserService;

  // Mapper
  private final AdminUserMapper adminUserMapper;

  // Helper
  private final ResponseHelper responseHelper;

  @AdminUserSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ADMIN_USER_SUMMARY_INFO_PATH)
  public ResponseEntity<GrobleResponse<PageResponse<AdminUserSummaryInfoResponse>>> getAllUsers(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "페이지당 사용자 수", example = "12")
          @RequestParam(value = "size", defaultValue = "12")
          int size,
      @Parameter(description = "정렬 기준 (property,direction)", example = "createdAt,desc")
          @RequestParam(value = "sort", defaultValue = "createdAt")
          String sort) {
    Pageable pageable = PageUtils.createPageable(page, size, sort);
    PageResponse<AdminUserSummaryInfoDTO> response = adminUserService.getAllUsers(pageable);
    PageResponse<AdminUserSummaryInfoResponse> responsePage =
        adminUserMapper.toAdminUserSummaryInfoResponsePage(response);

    return responseHelper.success(responsePage, ADMIN_USER_SUMMARY_INFO_SUCCESS_MESSAGE, OK);
  }
}
