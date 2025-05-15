package liaison.groble.security.jwt;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import liaison.groble.common.utils.CookieUtils;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 인증 처리를 담당 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final UserCacheService userCacheService;

  // 인증이 필요없는 경로 패턴 목록
  private static final List<String> PUBLIC_PATHS =
      List.of(
          "api/v1/oauth2/**",
          "/api/v1/home",
          "/api/v1/auth/sign-up",
          "/api/v1/auth/sign-in",
          "/api/v1/auth/email-verification/sign-up",
          "/api/v1/auth/verify-code/sign-up",
          "/api/v1/auth/password/reset-request",
          "/api/v1/auth/password/reset",
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
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 0) refreshToken 만료시 기존 토큰 삭제 (기존 구현)
    CookieUtils.getCookie(request, "refreshToken")
        .ifPresent(
            cookie -> {
              try {
                jwtTokenProvider.validateToken(cookie.getValue(), TokenType.REFRESH);
              } catch (ExpiredJwtException ex) {
                CookieUtils.deleteCookie(request, response, "accessToken");
                CookieUtils.deleteCookie(request, response, "refreshToken");
                log.debug("refreshToken 만료, 쿠키 삭제");
              }
            });

    final String requestURI = request.getRequestURI();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      String accessJwt = extractTokenFromRequest(request);

      if (StringUtils.hasText(accessJwt)) {
        try {
          // 1) accessToken 검증 & 인증
          processToken(request, accessJwt);

        } catch (ExpiredJwtException expiredEx) {
          log.debug("accessToken 만료 확인: {}", requestURI);

          // 2) accessToken 만료 → refreshToken 으로 재발급 시도
          CookieUtils.getCookie(request, "refreshToken")
              .ifPresent(
                  refreshCookie -> {
                    String refreshJwt = refreshCookie.getValue();
                    try {
                      if (jwtTokenProvider.validateToken(refreshJwt, TokenType.REFRESH)) {
                        // 3) refreshToken이 유효하면 새 accessToken 발급
                        Long userId =
                            jwtTokenProvider.getUserIdFromToken(refreshJwt, TokenType.REFRESH);
                        String email =
                            jwtTokenProvider.getUserEmailFromToken(refreshJwt, TokenType.REFRESH);

                        String newAccess = jwtTokenProvider.createAccessToken(userId, email);
                        int maxAge = (int) (jwtTokenProvider.getAccessTokenExpirationMs() / 1000);

                        // 4) 쿠키에 덮어쓰기
                        CookieUtils.addCookie(response, "accessToken", newAccess, maxAge);
                        // 5) (선택) 응답 헤더에도 세팅
                        response.setHeader("Authorization", "Bearer " + newAccess);

                        // 6) SecurityContext에 인증 정보 직접 설정
                        UserDetails principal = userDetailsService.loadUserByUsername(email);
                        UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        log.debug("새 accessToken 발급 및 SecurityContext 적용");

                      } else {
                        // refreshToken도 만료된 경우
                        CookieUtils.deleteCookie(request, response, "accessToken");
                        CookieUtils.deleteCookie(request, response, "refreshToken");
                        log.debug("refreshToken도 만료, 전체 로그아웃 처리");
                      }

                    } catch (Exception e) {
                      // refreshToken 검증 중 오류
                      CookieUtils.deleteCookie(request, response, "accessToken");
                      CookieUtils.deleteCookie(request, response, "refreshToken");
                      log.error("refreshToken 검증 중 오류 발생, 쿠키 삭제", e);
                    }
                  });
        }
      }

    } finally {
      // 7) 체인 계속 진행
      filterChain.doFilter(request, response);
      stopWatch.stop();
      if (stopWatch.getTotalTimeMillis() > 5000) {
        log.warn("긴 요청 처리 시간: {}ms - {}", stopWatch.getTotalTimeMillis(), requestURI);
      }
    }
  }

  /**
   * JWT 토큰 처리 및 인증 설정
   *
   * @return 인증 처리 성공 여부
   */
  private boolean processToken(HttpServletRequest request, String jwt) {
    // 1. 토큰 유효성 검증
    if (!jwtTokenProvider.validateToken(jwt, TokenType.ACCESS)) {
      return false;
    }

    try {
      // 2. 토큰에서 사용자 정보 추출
      final String userEmail = jwtTokenProvider.getUserEmailFromToken(jwt, TokenType.ACCESS);
      final Long userId = jwtTokenProvider.getUserIdFromToken(jwt, TokenType.ACCESS);

      // 3. 이미 인증이 되어있지 않은 경우에만 처리
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        // 4. 사용자 조회 (캐시 우선 조회)
        UserDetails userDetails;

        // 캐시에서 사용자 조회 시도
        userDetails = userId != null ? userCacheService.getUserFromCache(userId) : null;

        // 캐시에 없으면 DB에서 조회
        if (userDetails == null) {
          userDetails = userDetailsService.loadUserByUsername(userEmail);
          // 다음 요청을 위해 캐시에 저장
          if (userId != null) {
            userCacheService.cacheUser(userId, userDetails);
          }
        }

        // 5. 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
            createAuthenticationToken(userDetails, request);

        // 6. 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return true;
      }
    } catch (Exception e) {
      log.error("토큰 처리 중 예외 발생", e);
      return false;
    }

    return false;
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
}
