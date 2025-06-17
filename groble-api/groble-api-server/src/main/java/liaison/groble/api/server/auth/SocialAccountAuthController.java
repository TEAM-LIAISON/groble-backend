package liaison.groble.api.server.auth;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import liaison.groble.api.model.auth.request.SetSocialBasicInfoRequest;
import liaison.groble.api.model.auth.response.swagger.SetSocialBasicInfo;
import liaison.groble.application.auth.dto.SocialBasicInfoDTO;
import liaison.groble.application.auth.service.SocialAccountAuthService;
import liaison.groble.common.annotation.Auth;
import liaison.groble.common.model.Accessor;
import liaison.groble.common.response.GrobleResponse;
import liaison.groble.mapping.auth.AuthMapper;

import io.swagger.v3.oas.annotations.Parameter;
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

  // 소셜 계정 기본 정보 설정 API (userType, termsTypeStrings)
  @SetSocialBasicInfo
  @PostMapping("/social/basic-info")
  public ResponseEntity<GrobleResponse<String>> setSocialAccountBasicInfo(
      @Auth(required = false) Accessor accessor,
      @Parameter(description = "소셜 가입 기본 정보", required = true) @Valid @RequestBody
          SetSocialBasicInfoRequest request) {
    SocialBasicInfoDTO socialBasicInfoDTO = authMapper.toSocialBasicInfoDto(request);
    socialAccountAuthService.setSocialAccountBasicInfo(accessor.getUserId(), socialBasicInfoDTO);

    return ResponseEntity.status(HttpStatus.OK)
        .body(GrobleResponse.success("소셜 계정 기본 정보가 성공적으로 설정되었습니다."));
  }
}
