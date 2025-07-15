package liaison.groble.api.server.user;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.response.NicknameDuplicateCheckResponse;
import liaison.groble.api.model.user.response.SetNicknameResponse;
import liaison.groble.application.auth.service.UserInfoService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.annotation.Logging;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/info")
@Tag(
    name = "[👨‍💻 마이페이지] 닉네임 설정 및 수정, 닉네임 중복 확인 API",
    description = "닉네임 설정 및 수정, 닉네임 중복 확인 API입니다.")
public class UserInfoController {

  // API 경로 상수화
  private static final String SET_NICKNAME_PATH = "/set-nickname";
  private static final String NICKNAME_DUPLICATE_CHECK_PATH = "/nickname/duplicate-check";

  // 응답 메시지 상수화
  private static final String SET_NICKNAME_SUCCESS_MESSAGE = "닉네임 설정이 성공적으로 완료되었습니다.";
  private static final String NICKNAME_DUPLICATE_CHECK_SUCCESS_MESSAGE = "닉네임 중복 확인이 완료되었습니다.";

  // Service
  private final UserInfoService userInfoService;

  // Helper
  private final ResponseHelper responseHelper;

  @Operation(summary = "닉네임 설정 및 수정")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = SetNicknameResponse.class)))
  @Logging(item = "UserInfo", action = "setNickname", includeParam = true, includeResult = true)
  @PostMapping(SET_NICKNAME_PATH)
  public ResponseEntity<GrobleResponse<SetNicknameResponse>> setNickname(
      @Auth Accessor accessor, @RequestParam("nickname") @NotBlank String nickname) {

    String newNickname = userInfoService.setNickname(accessor.getUserId(), nickname);

    return responseHelper.success(
        new SetNicknameResponse(newNickname), SET_NICKNAME_SUCCESS_MESSAGE, HttpStatus.OK);
  }

  @Operation(summary = "닉네임 중복 확인")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = NicknameDuplicateCheckResponse.class)))
  @Logging(
      item = "UserInfo",
      action = "checkNicknameDuplicate",
      includeParam = true,
      includeResult = true)
  @GetMapping(NICKNAME_DUPLICATE_CHECK_PATH)
  public ResponseEntity<GrobleResponse<NicknameDuplicateCheckResponse>> checkNicknameDuplicate(
      @Auth Accessor accessor, @RequestParam("nickname") @NotBlank String nickname) {
    boolean exists = userInfoService.isNicknameTaken(nickname);

    return responseHelper.success(
        new NicknameDuplicateCheckResponse(nickname, exists),
        NICKNAME_DUPLICATE_CHECK_SUCCESS_MESSAGE,
        HttpStatus.OK);
  }
}
