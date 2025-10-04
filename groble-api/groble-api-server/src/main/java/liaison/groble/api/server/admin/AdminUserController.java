package liaison.groble.api.server.admin;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.admin.request.AdminBusinessInfoUpdateRequest;
import liaison.groble.api.model.admin.response.AdminAccountVerificationResponse;
import liaison.groble.api.model.admin.response.AdminUserSummaryInfoResponse;
import liaison.groble.api.model.admin.response.swagger.AdminUserSummaryInfo;
import liaison.groble.api.server.common.ApiPaths;
import liaison.groble.api.server.common.BaseController;
import liaison.groble.api.server.common.ResponseMessages;
import liaison.groble.api.server.common.swagger.SwaggerTags;
import liaison.groble.application.admin.dto.AdminAccountVerificationResultDTO;
import liaison.groble.application.admin.dto.AdminUserSummaryInfoDTO;
import liaison.groble.application.admin.service.AdminAccountVerificationService;
import liaison.groble.application.admin.service.AdminUserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.PageResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.common.utils.PageUtils;
import liaison.groble.mapping.admin.AdminUserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.Admin.BASE)
@Tag(name = SwaggerTags.Admin.USER, description = SwaggerTags.Admin.USER_DESC)
public class AdminUserController extends BaseController {

  private final AdminUserService adminUserService;
  private final AdminUserMapper adminUserMapper;
  private final AdminAccountVerificationService adminAccountVerificationService;

  public AdminUserController(
      ResponseHelper responseHelper,
      AdminUserService adminUserService,
      AdminUserMapper adminUserMapper,
      AdminAccountVerificationService adminAccountVerificationService) {
    super(responseHelper);
    this.adminUserService = adminUserService;
    this.adminUserMapper = adminUserMapper;
    this.adminAccountVerificationService = adminAccountVerificationService;
  }

  @AdminUserSummaryInfo
  @RequireRole("ROLE_ADMIN")
  @GetMapping(ApiPaths.Admin.ADMIN_USER_SUMMARY_INFO)
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

    return success(responsePage, ResponseMessages.Admin.USER_SUMMARY_INFO_RETRIEVED);
  }

  @Logging(
      item = "AdminUser",
      action = "verifyUserAccount",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.ADMIN_USER_ACCOUNT_VERIFICATION)
  @Operation(summary = "관리자 계좌 인증", description = "관리자가 Payple 계좌 인증을 즉시 실행합니다.")
  public ResponseEntity<GrobleResponse<AdminAccountVerificationResponse>> verifyUserAccount(
      @Auth Accessor accessor,
      @Parameter(description = "계좌 인증 대상 사용자 닉네임", example = "groble_maker")
          @PathVariable("nickname")
          String nickname) {

    AdminAccountVerificationResultDTO resultDTO =
        adminAccountVerificationService.verifyAccount(nickname);

    AdminAccountVerificationResponse response =
        adminUserMapper.toAdminAccountVerificationResponse(resultDTO);

    String message =
        resultDTO.isSuccess()
            ? ResponseMessages.Admin.USER_ACCOUNT_VERIFICATION_SUCCESS
            : ResponseMessages.Admin.USER_ACCOUNT_VERIFICATION_FAILED;

    return success(response, message);
  }

  @Logging(
      item = "AdminUser",
      action = "updateBusinessInfo",
      includeParam = true,
      includeResult = true)
  @RequireRole("ROLE_ADMIN")
  @PostMapping(ApiPaths.Admin.ADMIN_USER_BUSINESS_INFO)
  @Operation(summary = "사업자 정보 수정", description = "관리자가 사업자 정보를 업데이트합니다.")
  @ApiResponse(responseCode = "200", description = "사업자 정보 수정 성공")
  public ResponseEntity<GrobleResponse<Void>> updateBusinessInfo(
      @Auth Accessor accessor,
      @PathVariable("userId") Long userId,
      @Valid @RequestBody AdminBusinessInfoUpdateRequest request) {

    adminAccountVerificationService.updateBusinessInfo(
        userId, adminUserMapper.toAdminBusinessInfoUpdateDTO(request));

    return successVoid(ResponseMessages.Admin.USER_BUSINESS_INFO_UPDATED);
  }
}
