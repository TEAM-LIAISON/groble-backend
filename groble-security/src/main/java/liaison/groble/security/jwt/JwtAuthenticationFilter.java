package liaison.groble.security.jwt;

import java.io.IOException;
import java.time.Instant;
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
          "/payment/**",
          "/payple-payment",
          "/api/v1/groble/contents",
          "/api/v1/payments/**",
          "/api/v1/auth/phone-number/verify-request",
          "/api/v1/auth/phone-number/verify-code",
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

    if (isSwagger) {
      log.debug("Swagger 관련 요청은 JWT 필터를 건너뜁니다: {}", path);
    } else if (isPublicPath) {
      log.debug("공개 경로 요청은 JWT 필터를 건너뜁니다: {}", path);
    }

    return isSwagger || isPublicPath;
  }

  /** 인증 필터 처리 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      // =================================================================
      // 1단계: 기본 요청 정보 로깅 - 요청의 전체 컨텍스트를 파악
      // =================================================================
      log.debug("=== JWT 필터 디버깅 시작 ===");
      log.debug("요청 URI: {}", request.getRequestURI());
      log.debug("요청 메소드: {}", request.getMethod());
      log.debug("요청 URL: {}", request.getRequestURL());
      log.debug("서버 이름: {}", request.getServerName());
      log.debug("서버 포트: {}", request.getServerPort());
      log.debug("프로토콜: {}", request.getScheme());
      log.debug("리모트 주소: {}", request.getRemoteAddr());

      // =================================================================
      // 2단계: HTTP 헤더 전체 분석 - 브라우저가 실제로 무엇을 보냈는지 확인
      // =================================================================
      log.debug("=== 요청 헤더 분석 ===");
      java.util.Enumeration<String> headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        String headerValue = request.getHeader(headerName);
        // Cookie 헤더는 특별히 강조해서 로깅
        if ("Cookie".equalsIgnoreCase(headerName)) {
          log.debug("🍪 Cookie 헤더 발견: {}", headerValue);
        } else {
          log.debug("헤더 - {}: {}", headerName, headerValue);
        }
      }

      // =================================================================
      // 3단계: 쿠키 상세 분석 - 개별 쿠키들의 속성까지 모두 확인
      // =================================================================
      log.debug("=== 쿠키 상세 분석 ===");
      jakarta.servlet.http.Cookie[] cookies = request.getCookies();
      if (cookies != null && cookies.length > 0) {
        log.debug("총 쿠키 개수: {}", cookies.length);
        for (int i = 0; i < cookies.length; i++) {
          jakarta.servlet.http.Cookie cookie = cookies[i];
          log.debug(
              "쿠키[{}] - 이름: '{}', 값: '{}', 도메인: '{}', 경로: '{}', Secure: {}, HttpOnly: {}",
              i,
              cookie.getName(),
              maskToken(cookie.getValue()),
              cookie.getDomain(),
              cookie.getPath(),
              cookie.getSecure(),
              cookie.isHttpOnly());
        }
      } else {
        log.debug("❌ 요청에 쿠키가 전혀 없습니다!");
      }

      // =================================================================
      // 4단계: 토큰 추출 과정 상세 분석 - 각 방법별로 시도해보기
      // =================================================================
      log.debug("=== 토큰 추출 과정 분석 ===");

      // 4-1: Authorization 헤더에서 토큰 추출 시도
      String authHeader = request.getHeader("Authorization");
      log.debug("Authorization 헤더: {}", authHeader != null ? authHeader : "없음");
      String tokenFromHeader = jwtTokenProvider.resolveToken(authHeader);
      log.debug("헤더에서 추출된 토큰: {}", tokenFromHeader != null ? maskToken(tokenFromHeader) : "없음");

      // 4-2: CookieUtils를 사용한 토큰 추출 시도
      var cookieOptional = CookieUtils.getCookie(request, "accessToken");
      String tokenFromCookieUtils = cookieOptional.map(c -> c.getValue()).orElse(null);
      log.debug(
          "CookieUtils로 추출한 accessToken: {}",
          tokenFromCookieUtils != null ? maskToken(tokenFromCookieUtils) : "없음");

      // 4-3: 직접 구현된 메서드로 토큰 추출 시도
      String tokenFromDirect = extractTokenFromCookie(request, "accessToken");
      log.debug(
          "직접 구현 메서드로 추출한 accessToken: {}",
          tokenFromDirect != null ? maskToken(tokenFromDirect) : "없음");

      // 4-4: 최종 토큰 결정
      String accessJwt = extractToken(request);
      boolean accessTokenPresent = StringUtils.hasText(accessJwt);
      log.debug("최종 결정된 accessToken: {}", accessJwt != null ? maskToken(accessJwt) : "없음");
      log.debug("토큰 존재 여부: {}", accessTokenPresent);

      // =================================================================
      // 5단계: 리프레시 토큰 추출 과정 분석
      // =================================================================
      log.debug("=== 리프레시 토큰 추출 과정 분석 ===");
      var refreshTokenOpt = CookieUtils.getCookie(request, "refreshToken");
      boolean validRefreshToken = false;
      String refreshToken = null;

      if (refreshTokenOpt.isPresent()) {
        refreshToken = refreshTokenOpt.get().getValue();
        log.debug("리프레시 토큰 발견: {}", maskToken(refreshToken));

        try {
          jwtTokenProvider.parseClaimsJws(refreshToken, TokenType.REFRESH);
          validRefreshToken = true;
          log.debug("✅ 리프레시 토큰 유효성 검증 성공");
        } catch (ExpiredJwtException e) {
          log.debug("❌ 리프레시 토큰 만료: {}", e.getMessage());
          deleteAuthCookies(request, response);
        } catch (JwtException e) {
          log.warn("❌ 유효하지 않은 리프레시 토큰: {}", e.getMessage());
        }
      } else {
        log.debug("리프레시 토큰 없음");
      }

      // =================================================================
      // 6단계: 액세스 토큰 처리 로직
      // =================================================================
      log.debug("=== 액세스 토큰 처리 ===");
      if (accessTokenPresent) {
        log.debug("✅ 액세스 토큰이 존재합니다: {}", maskToken(accessJwt));
        try {
          jwtTokenProvider.parseClaimsJws(accessJwt, TokenType.ACCESS);
          log.debug("✅ 액세스 토큰 유효성 검증 성공");
          authenticate(accessJwt, request);
        } catch (ExpiredJwtException exp) {
          log.debug("❌ 액세스 토큰 만료, 리프레시 토큰으로 재발급 시도");
          handleTokenRefresh(refreshToken, validRefreshToken, response, request);
        } catch (JwtException | IllegalArgumentException bad) {
          log.debug("❌ 액세스 토큰 유효하지 않음: {}", bad.getMessage());
          response.addHeader("X-Token-Refresh-Status", "invalid-access");

          // 액세스 토큰이 유효하지 않지만 리프레시 토큰이 유효한 경우 새 액세스 토큰 발급
          if (validRefreshToken) {
            log.debug("리프레시 토큰이 유효하므로 새 액세스 토큰 발급 시도");
            handleTokenRefresh(refreshToken, true, response, request);
          }
        }
      } else {
        log.debug("❌ 액세스 토큰이 없습니다");

        // 액세스 토큰이 없지만 유효한 리프레시 토큰이 있는 경우 새 액세스 토큰 발급
        if (validRefreshToken) {
          log.debug("액세스 토큰은 없지만 유효한 리프레시 토큰이 있어 새 액세스 토큰 발급 시도");
          handleTokenRefresh(refreshToken, true, response, request);
        } else {
          log.debug("액세스 토큰도 없고 유효한 리프레시 토큰도 없어 인증 없이 진행");
        }
      }

      // =================================================================
      // 7단계: 최종 인증 상태 확인
      // =================================================================
      var auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated()) {
        log.debug("✅ 최종 인증 상태: 인증됨 - 사용자: {}", auth.getName());
      } else {
        log.debug("❌ 최종 인증 상태: 인증되지 않음 (익명 사용자)");
      }

      log.debug("=== JWT 필터 디버깅 완료 ===");

    } catch (Exception e) {
      log.error("JWT 필터 처리 중 예외 발생", e);
    } finally {
      chain.doFilter(request, response);
    }
  }

  /** 리프레시 토큰으로 액세스 토큰 재발급 처리 */
  private void handleTokenRefresh(
      String refreshToken,
      boolean validRefreshToken,
      HttpServletResponse response,
      HttpServletRequest request) {
    if (!validRefreshToken || refreshToken == null) {
      log.debug("리프레시 토큰이 없거나 유효하지 않아 액세스 토큰을 재발급할 수 없습니다");
      return;
    }

    try {
      Long userId = jwtTokenProvider.getUserId(refreshToken, TokenType.REFRESH);
      String email = jwtTokenProvider.getEmail(refreshToken, TokenType.REFRESH);

      // 리프레시 토큰의 만료 시간을 고려하여 새 액세스 토큰 생성
      String newAccess =
          jwtTokenProvider.createAccessTokenWithRefreshConstraint(userId, email, refreshToken);

      // 리프레시 토큰의 만료 시간 정보
      Instant refreshExpiration = jwtTokenProvider.getRefreshTokenExpirationInstant(refreshToken);
      Instant now = Instant.now();
      int maxAge =
          (int)
              Math.min(
                  (refreshExpiration.toEpochMilli() - now.toEpochMilli()) / 1000,
                  jwtTokenProvider.getAccessTokenExpirationMs() / 1000);

      log.info(
          "새 액세스 토큰 발급 성공 - userId: {}, email: {}, 쿠키 만료: {}초", userId, maskEmail(email), maxAge);

      // 디버깅 헤더 추가
      response.addHeader("X-Token-Refresh-Status", "success");

      // 쿠키에 새 액세스 토큰 추가 (수정된 최대 수명으로)
      CookieUtils.addCookie(
          response, "accessToken", newAccess, maxAge, "/", true, true, "None", cookieDomain);

      // 인증 설정
      setAuthentication(newAccess, request);
    } catch (Exception e) {
      log.error("토큰 재발급 처리 중 오류 발생: {}", e.getMessage());
      response.addHeader("X-Token-Refresh-Status", "refresh-error");
    }
  }

  /** 인증용 쿠키 삭제 헬퍼 메소드 */
  private void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    CookieUtils.deleteCookie(
        request, response, "accessToken", "/", cookieDomain, "None", true, true);
    CookieUtils.deleteCookie(
        request, response, "refreshToken", "/", cookieDomain, "None", true, true);
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
    // 이미 context에 인증 정보가 있을 때만 설정
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      log.debug("이미 인증 정보가 있습니다.");
      return;
    }

    try {
      // 토큰에서 사용자 정보 추출
      Long userId = jwtTokenProvider.getUserId(jwt, TokenType.ACCESS);
      log.debug("인증 정보 설정 시도: {}", maskToken(jwt));

      UserDetails ud = userDetailsService.loadUserByUsername(userId.toString());
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(auth);
      log.debug("인증 정보 설정 완료: {}", maskToken(jwt));
    } catch (Exception e) {
      log.error("인증 처리 중 오류 발생: {}", e.getMessage());
    }
  }

  /** 재발급된 accessToken 으로 SecurityContext 를 업데이트 */
  private void setAuthentication(String newAccessToken, HttpServletRequest request) {
    try {
      // 토큰에서 사용자 정보 추출
      String email = jwtTokenProvider.getEmail(newAccessToken, TokenType.ACCESS);
      log.debug("새 토큰으로 인증 정보 설정: {}", maskEmail(email));

      UserDetails ud = userDetailsService.loadUserByUsername(email);
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(auth);
      log.debug("새 토큰으로 인증 설정 완료");
    } catch (Exception e) {
      log.error("새 토큰으로 인증 설정 중 오류: {}", e.getMessage());
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
}
