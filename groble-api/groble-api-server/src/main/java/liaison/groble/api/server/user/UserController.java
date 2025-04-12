package liaison.groble.api.server.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.NicknameRequest;
import liaison.groble.api.model.user.request.RoleTypeRequest;
import liaison.groble.api.model.user.response.NicknameDuplicateCheckResponse;
import liaison.groble.api.model.user.response.NicknameResponse;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 유저 관련 API 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  /** 역할 전환 API 판매자/구매자 모드 전환 */
  @PostMapping("/switch-role")
  @RequireRole({"ROLE_USER", "ROLE_SELLER"})
  @Logging(item = "User", action = "SWITCH_ROLE", includeParam = true)
  public ResponseEntity<GrobleResponse<Void>> switchRole(
      @Auth Accessor accessor, @Valid @RequestBody RoleTypeRequest request) {

    log.info("역할 전환 요청: {} -> {}", accessor.getEmail(), request.getUserType());

    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return ResponseEntity.ok()
          .body(GrobleResponse.success(null, "역할이 " + request.getUserType() + "로 전환되었습니다.", 200));
    } else {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("역할 전환에 실패했습니다. 해당 역할이 할당되어 있는지 확인하세요.", 400));
    }
  }

  @GetMapping("/users/nickname/check")
  @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "중복 여부 조회 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "닉네임 값 누락 또는 잘못된 형식")
  })
  public ResponseEntity<GrobleResponse<NicknameDuplicateCheckResponse>> checkNicknameDuplicate(
      @RequestParam("value") @NotBlank String nickname) {

    boolean exists = userService.isNickNameTaken(nickname);
    return ResponseEntity.ok(
        GrobleResponse.success(new NicknameDuplicateCheckResponse(nickname, exists)));
  }

  /**
   * 닉네임 생성/수정 API
   *
   * <p>닉네임을 생성하거나 수정합니다.
   *
   * @param request 닉네임 요청 정보
   * @return 닉네임/생성 수정 결과
   */
  @Operation(summary = "닉네임 생성/수정", description = "닉네임을 생성 또는 수정합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "닉네임 생성/수정 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임")
  })
  @PostMapping("/users/nickname")
  public ResponseEntity<GrobleResponse<NicknameResponse>> setNickname(
      @Auth Accessor accessor, @Valid @RequestBody NicknameRequest request) {

    String setOrUpdateNickname =
        userService.setOrUpdateNickname(accessor.getUserId(), request.getNickname());

    NicknameResponse response = NicknameResponse.of(setOrUpdateNickname);

    return ResponseEntity.ok(GrobleResponse.success(response, "닉네임이 설정되었습니다."));
  }
}
