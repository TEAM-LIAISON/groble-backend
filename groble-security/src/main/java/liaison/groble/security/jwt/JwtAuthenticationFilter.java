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
          "/api/v1/home",
          "/api/v1/auth/**",
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

    if (isSwagger) {
      log.debug("Swagger 관련 요청은 JWT 필터를 건너뜁니다: {}", path);
    }

    return isSwagger;
  }

  /** 인증 필터 처리 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String requestURI = request.getRequestURI();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    try {
      String jwt = extractTokenFromRequest(request);
      boolean tokenProcessed = false;

      if (StringUtils.hasText(jwt)) {
        try {
          tokenProcessed = processToken(request, jwt);
          if (tokenProcessed) {
            log.debug("JWT 인증 성공 - URI: {}", requestURI);
          } else {
            log.debug("유효하지 않은 JWT 토큰 - URI: {}", requestURI);
          }
        } catch (Exception e) {
          log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
          // 예외는 기록하되 필터 체인은 계속 진행
        }
      } else {
        log.debug("Authorization 헤더 없음 - URI: {}", requestURI);
      }

    } finally {
      filterChain.doFilter(request, response);
      stopWatch.stop();

      // 5초 이상 걸린 요청 로깅 (성능 모니터링)
      if (stopWatch.getTotalTimeMillis() > 5000) {
        log.warn("긴 요청 처리 시간: {} ms - URI: {}", stopWatch.getTotalTimeMillis(), requestURI);
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
      token = extractTokenFromCookie(request, "access_token");
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
