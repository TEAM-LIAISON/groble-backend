package liaison.grobleauth.security.jwt;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import liaison.grobleauth.security.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** JWT 기반 인증을 처리하는 필터 모든 요청에 대해 Authorization 헤더를 확인하고 유효한 JWT 토큰이 있을 경우 인증 처리 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider tokenProvider;
  private final UserDetailsServiceImpl userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = getJwtFromRequest(request);
      String requestURI = request.getRequestURI();

      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        String username = tokenProvider.getUsernameFromToken(jwt);
        log.debug("JWT 토큰 확인 - 사용자: {}, URI: {}", username, requestURI);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("사용자 '{}' 인증 처리 완료, 권한: {}", username, userDetails.getAuthorities());
      } else {
        if (StringUtils.hasText(jwt)) {
          log.debug("유효하지 않은 JWT 토큰 (URI: {})", requestURI);
        }
      }
    } catch (Exception ex) {
      log.error("JWT 토큰 처리 중 오류 발생: {}", ex.getMessage());
      // 인증 실패 시에도 필터 체인은 계속 진행 (SecurityContext에 인증 정보가 설정되지 않음)
    }

    filterChain.doFilter(request, response);
  }

  /**
   * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출
   *
   * @param request HTTP 요청
   * @return JWT 토큰 또는 null
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    return tokenProvider.resolveToken(bearerToken);
  }
}
