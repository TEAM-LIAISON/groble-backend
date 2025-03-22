package liaison.grobleauth.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.grobleauth.exception.JwtTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/** JWT 토큰 생성 및 검증을 담당하는 컴포넌트 */
@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey jwtSecretKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  /**
   * 설정 파일에서 JWT 관련 값을 주입받아 초기화합니다.
   *
   * @param jwtSecret JWT 서명에 사용할 비밀키
   * @param accessTokenExpirationMs 액세스 토큰 만료 시간(밀리초)
   * @param refreshTokenExpirationMs 리프레시 토큰 만료 시간(밀리초)
   */
  public JwtTokenProvider(
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.expiration.ms}") long accessTokenExpirationMs,
      @Value("${app.jwt.refresh-token.expiration.ms}") long refreshTokenExpirationMs) {
    this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;

    log.info(
        "JWT Token Provider 초기화되었습니다. 액세스 토큰 만료: {}ms, 리프레시 토큰 만료: {}ms",
        accessTokenExpirationMs,
        refreshTokenExpirationMs);
  }

  /**
   * 인증 객체로부터 JWT 액세스 토큰을 생성합니다.
   *
   * @param authentication 인증된 사용자 정보
   * @return 생성된 JWT 액세스 토큰
   */
  public String generateAccessToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
    return generateAccessToken(userPrincipal.getUsername());
  }

  /**
   * 사용자 이름으로 JWT 액세스 토큰을 생성합니다.
   *
   * @param username 사용자 이름(이메일)
   * @return 생성된 JWT 액세스 토큰
   */
  public String generateAccessToken(String username) {
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("사용자 이름은 비어있을 수 없습니다");
    }

    Instant now = Instant.now();
    Instant expiryDate = now.plus(accessTokenExpirationMs, ChronoUnit.MILLIS);
    String tokenId = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .id(tokenId)
            .signWith(jwtSecretKey)
            .compact();

    log.debug("액세스 토큰 생성 완료 - 사용자: {}, 토큰 ID: {}, 만료: {}", username, tokenId, expiryDate);

    return token;
  }

  /**
   * 사용자 이름으로 JWT 리프레시 토큰을 생성합니다.
   *
   * @param username 사용자 이름(이메일)
   * @return 생성된 JWT 리프레시 토큰
   */
  public String generateRefreshToken(String username) {
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("사용자 이름은 비어있을 수 없습니다");
    }

    Instant now = Instant.now();
    Instant expiryDate = now.plus(refreshTokenExpirationMs, ChronoUnit.MILLIS);
    String tokenId = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .subject(username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .id(tokenId)
            .signWith(jwtSecretKey)
            .compact();

    log.debug("리프레시 토큰 생성 완료 - 사용자: {}, 토큰 ID: {}, 만료: {}", username, tokenId, expiryDate);

    return token;
  }

  /**
   * JWT 토큰에서 사용자 이름(주체)을 추출합니다.
   *
   * @param token JWT 토큰
   * @return 토큰에서 추출한 사용자 이름
   * @throws JwtTokenException 토큰 파싱 중 오류 발생 시
   */
  public String getUsernameFromToken(String token) {
    if (!StringUtils.hasText(token)) {
      throw new JwtTokenException("토큰이 비어있습니다");
    }

    try {
      Claims claims = parseToken(token);
      return claims.getSubject();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰에서 사용자 이름을 추출합니다: {}", e.getMessage());
      return e.getClaims().getSubject();
    } catch (Exception e) {
      throw new JwtTokenException("토큰에서 사용자 정보를 추출할 수 없습니다", e);
    }
  }

  /**
   * JWT 토큰을 파싱하여 Claims 객체를 반환합니다.
   *
   * @param token JWT 토큰
   * @return 토큰에서 추출한 Claims 객체
   * @throws JwtTokenException 토큰 파싱 중 오류 발생 시
   */
  private Claims parseToken(String token) {
    return Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token).getPayload();
  }

  /**
   * JWT 토큰의 유효성을 검증합니다.
   *
   * @param token 검증할 JWT 토큰
   * @return 토큰이 유효하면 true, 그렇지 않으면 false
   */
  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) {
      log.warn("빈 토큰이 검증을 위해 제공되었습니다");
      return false;
    }

    try {
      Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token);
      return true;
    } catch (SecurityException e) {
      log.error("유효하지 않은 JWT 서명: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("유효하지 않은 JWT 토큰: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("만료된 JWT 토큰: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT 토큰 문자열이 비어있습니다: {}", e.getMessage());
    } catch (JwtException e) {
      log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
    }
    return false;
  }

  /**
   * 액세스 토큰의 만료 시간을 반환합니다.
   *
   * @return 액세스 토큰 만료 시간(밀리초)
   */
  public long getAccessTokenExpiryDuration() {
    return accessTokenExpirationMs;
  }

  /**
   * 리프레시 토큰의 만료 시간을 반환합니다.
   *
   * @return 리프레시 토큰 만료 시간(밀리초)
   */
  public long getRefreshTokenExpiryDuration() {
    return refreshTokenExpirationMs;
  }

  /**
   * HTTP 요청 헤더에서 Bearer 토큰을 추출합니다.
   *
   * @param bearerToken Bearer 접두사를 포함한 인증 헤더 값
   * @return 토큰 문자열 또는 null(유효하지 않은 형식인 경우)
   */
  public String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
