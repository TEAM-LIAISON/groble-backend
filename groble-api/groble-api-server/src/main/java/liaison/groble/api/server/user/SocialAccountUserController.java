package liaison.groble.api.server.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.user.request.SetSocialBasicInfoRequest;
import liaison.groble.application.user.dto.SocialBasicInfoDTO;
import liaison.groble.application.user.service.SocialAccountUserService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;
import liaison.groble.mapping.user.UserMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/social")
@Tag(name = "[\uD83D\uDD11 소셜 계정] 소셜 계정의 기본 정보 설정 기능 API", description = "소셜 계정 기본 정보 설정 기능 API")
public class SocialAccountUserController {

  // API 경로 상수화
  private static final String SOCIAL_BASIC_INFO_PATH = "/user/basic-info";

  // 응답 메시지 상수화
  private static final String SOCIAL_BASIC_INFO_SUCCESS_MESSAGE = "소셜 계정 기본 정보가 성공적으로 설정되었습니다.";

  private final UserMapper userMapper;
  private final SocialAccountUserService socialAccountUserService;
  private final ResponseHelper responseHelper;

  @Operation(
      summary = "[✅ 소셜 계정 기본 정보 설정] 소셜 가입 기본 정보 설정",
      description = "소셜 로그인 과정에서 사용자의 기본 정보를 설정합니다.")
  @PostMapping(SOCIAL_BASIC_INFO_PATH)
  public ResponseEntity<GrobleResponse<Void>> setSocialAccountBasicInfo(
      @Auth Accessor accessor,
      @Parameter(description = "소셜 가입 기본 정보", required = true) @Valid @RequestBody
          SetSocialBasicInfoRequest request) {
    SocialBasicInfoDTO socialBasicInfoDTO = userMapper.toSocialBasicInfoDTO(request);
    socialAccountUserService.setSocialAccountBasicInfo(accessor.getUserId(), socialBasicInfoDTO);

    return responseHelper.success(null, SOCIAL_BASIC_INFO_SUCCESS_MESSAGE, HttpStatus.OK);
  }
}
