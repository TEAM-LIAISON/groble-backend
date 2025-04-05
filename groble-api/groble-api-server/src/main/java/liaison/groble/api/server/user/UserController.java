package liaison.groble.api.server.user;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.RoleTypeRequest;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 유저 관련 API 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  /** 역할 전환 API 판매자/구매자 모드 전환 */
  @PostMapping("/switch-role")
  @RequireRole({"ROLE_USER", "ROLE_SELLER"})
  @Logging(item = "User", action = "SWITCH_ROLE", includeParam = true)
  public ResponseEntity<ApiResponse<Void>> switchRole(
      @Auth Accessor accessor, @Valid @RequestBody RoleTypeRequest request) {

    log.info("역할 전환 요청: {} -> {}", accessor.getEmail(), request.getUserType());

    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return ResponseEntity.ok()
          .body(ApiResponse.success(null, "역할이 " + request.getUserType() + "로 전환되었습니다.", 200));
    } else {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("역할 전환에 실패했습니다. 해당 역할이 할당되어 있는지 확인하세요.", 400));
    }
  }
}
