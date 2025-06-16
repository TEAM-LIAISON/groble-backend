package liaison.groble.api.server.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.application.auth.service.SocialAccountAuthService;
import liaison.groble.common.utils.TokenCookieService;
import liaison.groble.mapping.auth.AuthMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "소셜 로그인 기능 관련 API", description = "소셜 로그인 기능 API")
public class SocialAccountAuthController {
  private final AuthMapper authMapper;
  private final SocialAccountAuthService socialAccountAuthService;
  private final TokenCookieService tokenCookieService;

  //    @SocialSignUp
  //    @PostMapping("/social/basic-info")
  //    public ResponseEntity<GrobleResponse<SocialBasicInfoResponse>> setSocialAccountBasicInfo(
  //            @Auth(required = false) Accessor accessor,
  //            @Parameter(description = "소셜 가입 기본 정보", required = true) @Valid @RequestBody
  //            SetSocialBasicInfoRequest request,
  //            HttpServletResponse response
  //    ) {
  //        SocialBasicInfoDto socialBasicInfoDto = authMapper.toSocialBasicInfoDto(request);
  //
  //        tokenCookieService.addTokenCookies(
  //                response, signUpAuthResultDTO.getAccessToken(),
  // signUpAuthResultDTO.getRefreshToken());
  //    }
}
