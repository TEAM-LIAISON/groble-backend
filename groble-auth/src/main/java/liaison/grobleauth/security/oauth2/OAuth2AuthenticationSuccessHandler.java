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
import liaison.grobleauth.security.jwt.JwtTokenProvider;
import liaison.grobleauth.service.OAuth2AuthService.CustomOAuth2User;
import liaison.groblecore.domain.User;
import liaison.groblecore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws java.io.IOException {

    log.info("OAuth2 인증 성공 - 처리 시작");

    // OAuth2 사용자 정보 가져오기
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    Long userId = oAuth2User.getId();

    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

    // 직접 토큰 생성 (OAuth2AuthService에 의존하지 않음)
    String accessToken = jwtTokenProvider.createAccessToken(user);
    String refreshToken = jwtTokenProvider.createRefreshToken(user);

    // 리프레시 토큰 저장
    user.updateRefreshToken(refreshToken);
    userRepository.save(user);

    // 토큰 응답 객체 생성
    TokenResponse tokenResponse =
        TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
            .build();

    log.info("사용자 인증 완료: {}, 토큰 발급 완료", oAuth2User.getEmail());

    // 응답에 토큰 정보 직접 포함
    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // 응답 데이터 생성 및 반환
    objectMapper.writeValue(response.getOutputStream(), tokenResponse);

    // 인증 속성 정리
    clearAuthenticationAttributes(request);
  }
}
