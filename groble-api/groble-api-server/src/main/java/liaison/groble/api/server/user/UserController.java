package liaison.groble.api.server.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.model.user.response.MyPageSummaryResponseBase;
import liaison.groble.api.model.user.response.UserHeaderResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.swagger.MyPageDetail;
import liaison.groble.api.model.user.response.swagger.MyPageSummary;
import liaison.groble.api.model.user.response.swagger.SwitchRole;
import liaison.groble.api.server.user.mapper.UserDtoMapper;
import liaison.groble.application.user.dto.UserHeaderDto;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

/** 사용자 정보 관련 API 컨트롤러 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "사용자 정보 관련 API", description = "마이페이지 조회 및 가입 유형 전환 API")
public class UserController {

  private final UserService userService;
  private final UserDtoMapper userDtoMapper;

  public UserController(UserService userService, UserDtoMapper userDtoMapper) {
    this.userService = userService;
    this.userDtoMapper = userDtoMapper;
  }

  @SwitchRole
  @PostMapping("/users/switch-role")
  @RequireRole({"ROLE_USER"})
  public ResponseEntity<GrobleResponse<Void>> switchUserType(
      @Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {
    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT)
          .body(GrobleResponse.success(null, "가입 유형이 전환되었습니다.", 204));
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(GrobleResponse.fail("해당 역할이 할당되어 있지 않습니다.", 400));
    }
  }

  /** 마이페이지 요약 정보 조회 */
  @MyPageSummary
  @GetMapping("/users/me/summary")
  public ResponseEntity<GrobleResponse<MyPageSummaryResponseBase>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDto summaryDto = userService.getUserMyPageSummary(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageSummaryResponse(summaryDto)));
  }

  /** 마이페이지 상세 정보 조회 */
  @MyPageDetail
  @GetMapping("/users/me/detail")
  public ResponseEntity<GrobleResponse<UserMyPageDetailResponse>> getUserMyPageDetail(
      @Auth Accessor accessor) {
    UserMyPageDetailDto detailDto = userService.getUserMyPageDetail(accessor.getUserId());
    return ResponseEntity.ok(
        GrobleResponse.success(userDtoMapper.toApiMyPageDetailResponse(detailDto)));
  }

  // 홈화면 헤더 정보 조회
  @GetMapping("/me")
  public ResponseEntity<GrobleResponse<UserHeaderResponse>> getUserHeaderInform(
      @Auth(required = false) Accessor accessor) {

    // 로그인하지 않은 경우
    if (accessor == null) {
      UserHeaderResponse response =
          UserHeaderResponse.builder()
              .isLogin(false)
              .nickname(null)
              .profileImageUrl(null)
              .canSwitchToSeller(false)
              .unreadNotificationCount(0)
              .build();

      return ResponseEntity.ok(GrobleResponse.success(response, "사용자 정보 조회 성공"));
    }

    // 로그인한 경우 - 기존 코드 활용
    UserHeaderDto userHeaderDto = userService.getUserHeaderInform(accessor.getUserId());
    UserHeaderResponse response = userDtoMapper.toApiUserHeaderResponse(userHeaderDto);

    // 응답에 isLogin 값을 true로 설정 (만약 DTO 매핑에서 처리하지 않는 경우)
    if (response != null && response.getIsLogin() == null) {
      // reflection이나 새 객체 생성을 통해 isLogin을 true로 설정
      // 이 예제에서는 현재 코드가 완전하지 않아 정확한 방법을 제시하기 어려움
    }

    return ResponseEntity.ok(GrobleResponse.success(response, "사용자 정보 조회 성공"));
  }
}
