package liaison.groble.api.server.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.auth.service.UserInfoService;
import liaison.groble.mapping.user.UserInfoMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/info")
@Tag(name = "사용자 정보 기능 관련 API", description = "사용자 정보 기능 관련 API")
public class UserInfoController {
  private final UserInfoMapper userInfoMapper;
  private final UserInfoService userInfoService;

  //    @Operation(summary = "닉네임 설정 및 수정", description = "첫 회원가입 과정에서 닉네임을 설정하거나, 기존 닉네임을 수정합니다.")
  //    public ResponseEntity<GrobleResponse<SetNicknameResponse>> setNickname(
  //            @Auth Accessor accessor,
  //            @Parameter(description = "변경하려는 닉네임 값", required = true) @Valid @RequestBody
  //            SetNicknameRequest request
  //    ) {
  //        log.info("닉네임 설정 요청: {}", request.getNickname());
  //
  //    }
}
