package liaison.grobleauth.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.grobleauth.dto.AuthDto.TokenResponse;
import liaison.grobleauth.service.OAuth2AuthService;
import liaison.grobleauth.service.OAuth2AuthService.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final OAuth2AuthService oAuth2AuthService;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws java.io.IOException {

    log.info("OAuth2 인증 성공 - 처리 시작");

    // OAuth2 사용자 정보 가져오기
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getEmail();

    // 토큰 생성
    TokenResponse tokenResponse = oAuth2AuthService.createTokens(email);

    log.info("사용자 인증 완료: {}, 토큰 발급 완료", email);

    // 응답에 토큰 정보 직접 포함
    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // 응답 데이터 생성 및 반환
    objectMapper.writeValue(response.getOutputStream(), tokenResponse);

    // 인증 속성 정리
    clearAuthenticationAttributes(request);
  }
}
