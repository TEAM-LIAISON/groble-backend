package liaison.groble.security.jwt;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import liaison.groble.common.utils.CookieUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 처리를 담당 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  // ✅ 관리자 쿠키 도메인 가져오기
  @Value("${app.cookie.admin-domain}")
  private String adminCookieDomain;

  @Value("${server.env:local}")
  private String serverEnv;

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  // 인증이 필요없는 경로 패턴 목록
  private static final List<String> PUBLIC_PATHS =
      List.of(
          "api/v1/oauth2/**",
          "/api/v1/home",
          "/api/v1/auth/sign-up",
          "/api/v1/auth/sign-in",
          "/api/v1/auth/sign-in/local/test",
          "/api/v1/auth/email-verification/sign-up",
          "/api/v1/auth/verify-code/sign-up",
          "/api/v1/auth/password/reset-request",
          "/api/v1/auth/password/reset",
          "/api/v1/auth/nickname/check",
          "/api/v1/auth/integrated/sign-up",
          "/api/v1/auth/integrated/sign-in",
          "/api/v1/market/intro/**",
          "/api/v1/market/contents/**",
          "/api/v1/admin/auth/sign-in",
          "/api/v1/verification/email/code/sign-up",
          "/api/v1/verification/email/code/verify/sign-up",
          "/api/v1/verification/email/code/password-reset",
          "/api/v1/verification/password/reset",
          "/payment/**",
          "/payple-payment",
          "/api/v1/groble/contents",
          "/api/v1/home/contents",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/v3/api-docs/**",
          "/swagger-resources/**",
          "/webjars/**",
          "/favicon.ico");

  // Swagger 관련 경로인지 확인하는 메소드
  private boolean isSwaggerRequest(String path) {
    return path.startsWith("/swagger-ui")
        || path.equals("/swagger-ui.html")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-resources")
        || path.startsWith("/webjars")
        || path.equals("/favicon.ico");
  }

  /** 이 필터를 적용할지 결정하는 메소드 Swagger UI 관련 요청은 필터링하지 않음 */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    boolean isSwagger = isSwaggerRequest(path);

    // 공개 경로 매처를 사용하여 필터링 여부 결정
    RequestMatcher publicPathsMatcher = buildPublicPathsMatcher();
    boolean isPublicPath = publicPathsMatcher.matches(request);

    return isSwagger || isPublicPath;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    // ✅ 관리자 로그인 요청은 필터에서 제외
    String requestURI = request.getRequestURI();
    if (requestURI.startsWith("/api/v1/admin/auth/sign-in")) {
      chain.doFilter(request, response);
      return;
    }

    // ✅ 중요: 매 요청마다 SecurityContext 초기화
    SecurityContextHolder.clearContext();

    try {
      // 액세스 토큰 추출
      String accessJwt = extractToken(request);
      boolean accessTokenPresent = StringUtils.hasText(accessJwt);

      // 리프레시 토큰 추출 및 검증
      var refreshTokenOpt = CookieUtils.getCookie(request, "refreshToken");
      boolean validRefreshToken = false;
      String refreshToken = null;

      if (refreshTokenOpt.isPresent()) {
        refreshToken = refreshTokenOpt.get().getValue();
        try {
          jwtTokenProvider.parseClaimsJws(refreshToken, TokenType.REFRESH);
          validRefreshToken = true;
        } catch (ExpiredJwtException e) {
          log.info("리프레시 토큰 만료 - URI: {}", request.getRequestURI());
          deleteAuthCookies(request, response);
        } catch (JwtException e) {
          log.warn("유효하지 않은 리프레시 토큰 - URI: {}", request.getRequestURI());
        }
      }

      // 액세스 토큰 처리
      if (accessTokenPresent) {
        try {
          jwtTokenProvider.parseClaimsJws(accessJwt, TokenType.ACCESS);
          authenticate(accessJwt, request);
        } catch (ExpiredJwtException exp) {
          log.debug("액세스 토큰 만료 - URI: {}", request.getRequestURI());
          // ✅ 관리자 요청인지 확인 후 적절한 도메인으로 토큰 재발급
          handleTokenRefresh(refreshToken, validRefreshToken, response, request);
        } catch (JwtException | IllegalArgumentException bad) {
          log.debug("유효하지 않은 액세스 토큰 - URI: {}", request.getRequestURI());
          response.addHeader("X-Token-Refresh-Status", "invalid-access");

          if (validRefreshToken) {
            handleTokenRefresh(refreshToken, true, response, request);
          }
        }
      } else {
        if (validRefreshToken) {
          handleTokenRefresh(refreshToken, true, response, request);
        }
      }

    } catch (Exception e) {
      log.error("JWT 필터 처리 중 예외 발생 - URI: {}", request.getRequestURI(), e);
      SecurityContextHolder.clearContext();
    } finally {
      try {
        chain.doFilter(request, response);
      } finally {
        SecurityContextHolder.clearContext();
      }
    }
  }

  /** 리프레시 토큰으로 액세스 토큰 재발급 처리 */
  private void handleTokenRefresh(
      String refreshToken,
      boolean validRefreshToken,
      HttpServletResponse response,
      HttpServletRequest request) {
    if (!validRefreshToken || refreshToken == null) {
      return;
    }

    try {
      Long userId = jwtTokenProvider.getUserId(refreshToken, TokenType.REFRESH);
      String email = jwtTokenProvider.getEmail(refreshToken, TokenType.REFRESH);

      String newAccess =
          jwtTokenProvider.createAccessTokenWithRefreshConstraint(userId, email, refreshToken);

      LocalDateTime refreshExpiration = jwtTokenProvider.getRefreshTokenExpirationAt(refreshToken);
      Instant refreshInstant = refreshExpiration.atZone(ZoneId.systemDefault()).toInstant();
      Instant now = Instant.now();
      int maxAge =
          (int)
              Math.min(
                  Duration.between(now, refreshInstant).getSeconds(),
                  jwtTokenProvider.getAccessTokenExpirationMs() / 1000);

      log.info("액세스 토큰 재발급 성공 - userId: {}", userId);
      response.addHeader("X-Token-Refresh-Status", "success");

      // ✅ 관리자 요청인지 확인해서 적절한 도메인 사용
      String requestURI = request.getRequestURI();
      boolean isAdminRequest = requestURI.startsWith("/api/v1/admin");

      String domain = isAdminRequest ? getAdminCookieDomain() : cookieDomain;
      String sameSite = isProductionEnvironment() ? "Strict" : "None";

      CookieUtils.addCookie(
          response, "accessToken", newAccess, maxAge, "/", true, true, sameSite, domain);

      setAuthentication(newAccess, request);
    } catch (Exception e) {
      log.error("토큰 재발급 처리 중 오류 발생", e);
      response.addHeader("X-Token-Refresh-Status", "refresh-error");
    }
  }

  /** 인증용 쿠키 삭제 헬퍼 메소드 */
  private void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    String sameSite = isProductionEnvironment() ? "Strict" : "None";
    CookieUtils.deleteCookie(
        request, response, "accessToken", "/", cookieDomain, sameSite, true, true);
    CookieUtils.deleteCookie(
        request, response, "refreshToken", "/", cookieDomain, sameSite, true, true);
  }

  /** 운영 환경인지 확인 */
  private boolean isProductionEnvironment() {
    return "blue".equals(serverEnv) || "green".equals(serverEnv) || "prod".equals(serverEnv);
  }

  /** 토큰 마스킹 헬퍼 메소드 (로깅 시 보안을 위해) */
  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return "[보안상 숨김]";
    }
    return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
  }

  /** 이메일 마스킹 헬퍼 메소드 */
  private String maskEmail(String email) {
    if (email == null || email.indexOf('@') < 1) {
      return "[이메일 형식 오류]";
    }
    String[] parts = email.split("@");
    String name = parts[0];
    String domain = parts[1];

    String maskedName =
        name.substring(0, Math.min(2, name.length())) + "*".repeat(Math.max(1, name.length() - 2));

    return maskedName + "@" + domain;
  }

  /** 인증 객체 생성 */
  private UsernamePasswordAuthenticationToken createAuthenticationToken(
      UserDetails userDetails, HttpServletRequest request) {

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    // 요청 상세 정보 추가
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    return authentication;
  }

  /** 요청에서 JWT 토큰 추출 */
  private String extractTokenFromRequest(HttpServletRequest request) {
    // 헤더에서 토큰 추출 (Authorization: Bearer xxx)
    String bearerToken = request.getHeader("Authorization");
    String token = jwtTokenProvider.resolveToken(bearerToken);

    // Bearer 헤더에 없으면 쿠키에서 추출 시도
    if (token == null) {
      token = extractTokenFromCookie(request, "accessToken");
    }

    return token;
  }

  /** 쿠키에서 토큰 추출 */
  private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
    jakarta.servlet.http.Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (jakarta.servlet.http.Cookie cookie : cookies) {
        if (cookieName.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  /** 공개 경로 매처 생성 */
  private RequestMatcher buildPublicPathsMatcher() {
    List<RequestMatcher> matchers =
        PUBLIC_PATHS.stream()
            .map(
                (String pattern) -> {
                  RequestMatcher matcher;
                  if (pattern.contains("**")) {
                    // 와일드카드 패턴 매칭
                    matcher = new AntPathRequestMatcher(pattern);
                  } else {
                    // 정확한 경로 매칭
                    matcher = new AntPathRequestMatcher(pattern, null, false);
                  }
                  return matcher;
                })
            .toList();

    return new OrRequestMatcher(matchers.toArray(new RequestMatcher[0]));
  }

  /** 주어진 accessToken 으로 인증 정보를 생성하여 SecurityContext 에 설정 */
  private void authenticate(String jwt, HttpServletRequest request) {
    try {
      // 토큰에서 사용자 정보 추출
      Long userId = jwtTokenProvider.getUserId(jwt, TokenType.ACCESS);

      // ✅ 중요: 매번 새로운 인증 정보로 교체 (기존 인증 정보 무시)
      UserDetails ud = userDetailsService.loadUserByUsername(userId.toString());
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      // ✅ SecurityContext를 명시적으로 설정
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (Exception e) {
      log.error("인증 처리 중 오류 발생", e);
      // ✅ 오류 발생 시 SecurityContext 클리어
      SecurityContextHolder.clearContext();
    }
  }

  /** 재발급된 accessToken 으로 SecurityContext 를 업데이트 */
  private void setAuthentication(String newAccessToken, HttpServletRequest request) {
    try {
      // ✅ 토큰에서 사용자 ID 추출
      Long userId = jwtTokenProvider.getUserId(newAccessToken, TokenType.ACCESS);

      // ✅ userId 기반으로 사용자 정보 조회
      UserDetails ud = userDetailsService.loadUserByUsername(userId.toString());

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (Exception e) {
      log.error("새 토큰으로 인증 설정 중 오류", e);
    }
  }

  /** 요청 헤더나 쿠키에서 accessToken을 추출합니다. */
  private String extractToken(HttpServletRequest request) {
    // Authorization 헤더에서 Bearer 토큰 확인
    String bearer = request.getHeader("Authorization");
    String token = jwtTokenProvider.resolveToken(bearer);
    // 헤더에 없으면 쿠키에서 조회
    if (token == null) {
      token = CookieUtils.getCookie(request, "accessToken").map(c -> c.getValue()).orElse(null);
    }
    return token;
  }

  private String getAdminCookieDomain() {
    return adminCookieDomain != null && !adminCookieDomain.isBlank()
        ? adminCookieDomain
        : ".groble.im";
  }
}
