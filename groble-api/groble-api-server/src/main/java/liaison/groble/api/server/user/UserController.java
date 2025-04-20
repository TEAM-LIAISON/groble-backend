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
import liaison.groble.api.model.user.request.UserTypeRequest;
import liaison.groble.api.model.user.response.NicknameDuplicateCheckResponse;
import liaison.groble.api.model.user.response.NicknameResponse;
import liaison.groble.api.model.user.response.UserMyPageDetailResponse;
import liaison.groble.api.model.user.response.UserMyPageSummaryResponse;
import liaison.groble.api.server.user.mapper.UserDtoMapper;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.RequireRole;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 유저 관련 API 컨트롤러 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "사용자 정보 API", description = "닉네임, 비밀번호 설정 및 계정 전환 API")
public class UserController {
  private final UserService userService;
  private final UserDtoMapper userDtoMapper;

  /** 역할 전환 API 판매자/구매자 모드 전환 */
  @Operation(summary = "가입 유형 전환", description = "판매자 또는 구매자로 가입 유형을 전환합니다.")
  @PostMapping("/users/switch-role")
  @RequireRole({"ROLE_USER"})
  public ResponseEntity<GrobleResponse<Void>> switchUserType(
      @Auth Accessor accessor, @Valid @RequestBody UserTypeRequest request) {

    boolean success = userService.switchUserType(accessor.getUserId(), request.getUserType());

    if (success) {
      return ResponseEntity.ok()
          .body(GrobleResponse.success(null, "역할이 " + request.getUserType() + "로 전환되었습니다.", 200));
    } else {
      return ResponseEntity.badRequest()
          .body(GrobleResponse.error("역할 전환에 실패했습니다. 해당 역할이 할당되어 있는지 확인하세요.", 400));
    }
  }

  @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다. 회원가입 및 닉네임 수정 시 사용됩니다.")
  @GetMapping("/users/nickname/check")
  public ResponseEntity<GrobleResponse<NicknameDuplicateCheckResponse>> checkNicknameDuplicate(
      @RequestParam("value") @NotBlank String nickname) {

    boolean exists = userService.isNickNameTaken(nickname);
    return ResponseEntity.ok(
        GrobleResponse.success(new NicknameDuplicateCheckResponse(nickname, exists)));
  }

  @Operation(summary = "닉네임 수정", description = "닉네임을 수정합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "닉네임 수정 성공",
        content = @Content(schema = @Schema(implementation = GrobleResponse.class))),
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

  @Operation(summary = "마이페이지 요약 정보 조회", description = "마이페이지 첫 화면에서 요약 정보를 조회합니다.")
  @GetMapping("/users/me/summary")
  public ResponseEntity<GrobleResponse<UserMyPageSummaryResponse>> getUserMyPageSummary(
      @Auth Accessor accessor) {
    UserMyPageSummaryDto userMyPageSummaryDto =
        userService.getUserMyPageSummary(accessor.getUserId());

    UserMyPageSummaryResponse response =
        userDtoMapper.toApiMyPageSummaryResponse(userMyPageSummaryDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "마이페이지 조회 성공"));
  }

  @Operation(summary = "마이페이지 상세 정보 조회", description = "마이페이지에서 사용자 상세 정보를 조회합니다.")
  @GetMapping("/users/me/detail")
  public ResponseEntity<GrobleResponse<UserMyPageDetailResponse>> getUserMyPageDetail(
      @Auth Accessor accessor) {
    UserMyPageDetailDto userMyPageDetailDto = userService.getUserMyPageDetail(accessor.getUserId());

    UserMyPageDetailResponse response =
        userDtoMapper.toApiMyPageDetailResponse(userMyPageDetailDto);

    return ResponseEntity.ok(GrobleResponse.success(response, "마이페이지 상세 조회 성공"));
  }
}
