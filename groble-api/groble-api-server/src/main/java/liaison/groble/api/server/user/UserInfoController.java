package liaison.groble.api.server.user;

import jakarta.validation.constraints.NotBlank;

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
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/info")
@Tag(
    name = "[사용자 정보] 사용자 정보(닉네임, 가입 유형 등) 기능 관련 API",
    description = "닉네임 중복 체크, 닉네임 설정 및 수정, 가입 유형 변경 등의 기능을 제공합니다.")
public class UserInfoController {
  private final UserInfoService userInfoService;

  @Operation(summary = "닉네임 설정 및 수정", description = "첫 회원가입 과정에서 닉네임을 설정하거나, 기존 닉네임을 수정합니다.")
  @PostMapping("/set-nickname")
  public ResponseEntity<GrobleResponse<SetNicknameResponse>> setNickname(
      @Auth Accessor accessor, @RequestParam("nickname") @NotBlank String nickname) {
    log.info("닉네임 설정 요청: {}", nickname);

    String newNickname = userInfoService.setNickname(accessor.getUserId(), nickname);

    return ResponseEntity.ok(GrobleResponse.success(new SetNicknameResponse(newNickname)));
  }

  @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다. 회원가입 및 닉네임 수정 시 사용합니다.")
  @GetMapping("/nickname/duplicate-check")
  public ResponseEntity<GrobleResponse<NicknameDuplicateCheckResponse>> checkNicknameDuplicate(
      @Auth Accessor accessor, @RequestParam("nickname") @NotBlank String nickname) {
    boolean exists = userInfoService.isNicknameTaken(nickname);
    return ResponseEntity.ok(
        GrobleResponse.success(new NicknameDuplicateCheckResponse(nickname, exists)));
  }
}
