package liaison.groble.api.server.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.swagger.MyPageDetailResponse;
import liaison.groble.api.model.user.response.swagger.MyPageSummaryResponse;
import liaison.groble.api.server.user.mapper.UserDtoMapper;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/** 사용자 정보 관련 API 컨트롤러 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "사용자 정보 API", description = "마이페이지 조회 및 가입 유형 전환 API")
public class UserController {

  private final UserService userService;
  private final UserDtoMapper userDtoMapper;

  public UserController(UserService userService, UserDtoMapper userDtoMapper) {
    this.userService = userService;
    this.userDtoMapper = userDtoMapper;
  }

  /** 사용자 역할 전환 API - 판매자/구매자 모드 전환 */
  @Operation(summary = "가입 유형 전환", description = "판매자 또는 구매자로 가입 유형을 전환합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "역할 전환 성공"),
    @ApiResponse(responseCode = "400", description = "역할 전환 실패 - 해당 역할이 할당되어 있지 않음"),
    @ApiResponse(responseCode = "401", description = "인증 실패 (AccessToken 만료 또는 없음)"),
    @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  @PostMapping("/switch-role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RequireRole({"ROLE_USER"})
  public void switchUserType(@Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {
    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());
  }

  /** 마이페이지 요약 정보 조회 */
  @MyPageSummaryResponse
  @GetMapping("/me/summary")
  public ResponseEntity<GrobleResponse<MyPageSummaryResponseBase>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDto summaryDto = userService.getUserMyPageSummary(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageSummaryResponse(summaryDto)));
  }

  /** 마이페이지 상세 정보 조회 */
  @MyPageDetailResponse
  @GetMapping("/me/detail")
  public ResponseEntity<GrobleResponse<UserMyPageDetailResponse>> getUserMyPageDetail(
      @Auth Accessor accessor) {
    UserMyPageDetailDto detailDto = userService.getUserMyPageDetail(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageDetailResponse(detailDto)));
  }
}
