package liaison.groble.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.groble.security.oauth2.exception.TokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/** JWT 토큰 생성, 검증, 파싱 등을 담당하는 컴포넌트 */
@Slf4j
@Component
public class JwtTokenProvider {
  private final SecretKey accessTokenKey;
  private final SecretKey refreshTokenKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;
  private final String issuer;

  /** 생성자 - 설정 값 주입 및 초기화 */
  public JwtTokenProvider(
      @Value("${app.jwt.access-token.secret}") String accessTokenSecret,
      @Value("${app.jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${app.jwt.access-token.expiration-ms}") long accessTokenExpirationMs,
      @Value("${app.jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs,
      @Value("${app.jwt.issuer:auth-service}") String issuer) {

    // 안전한 키 생성 (HMAC-SHA-512 알고리즘용 키)
    this.accessTokenKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    this.issuer = issuer;

    log.info(
        "JWT 토큰 제공자 초기화 완료 - 액세스 토큰 만료: {}ms, 리프레시 토큰 만료: {}ms",
        accessTokenExpirationMs,
        refreshTokenExpirationMs);
  }

  /** 인증 객체로부터 액세스 토큰 생성 */
  public String generateAccessToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return generateAccessToken(userPrincipal);
  }

  /** UserPrincipal 객체로부터 액세스 토큰 생성 */
  public String generateAccessToken(UserPrincipal userPrincipal) {
    return generateToken(userPrincipal, TokenType.ACCESS, accessTokenKey, accessTokenExpirationMs);
  }

  /**
   * 사용하지 않는 메소드 제거 또는 Deprecated 처리
   *
   * @deprecated 이메일 중복 시 보안 문제 발생 가능
   */
  @Deprecated
  public String generateAccessToken(String email) {
    throw new UnsupportedOperationException("이메일만으로 토큰을 생성하는 것은 안전하지 않습니다. User 객체를 사용하세요.");
  }

  @Deprecated
  public String generateRefreshToken(String email) {
    throw new UnsupportedOperationException("이메일만으로 토큰을 생성하는 것은 안전하지 않습니다. User 객체를 사용하세요.");
  }

  /** 리프레시 토큰 생성 */
  public String generateRefreshToken(UserPrincipal userPrincipal) {
    return generateToken(
        userPrincipal, TokenType.REFRESH, refreshTokenKey, refreshTokenExpirationMs);
  }

  /** 토큰 생성 공통 메서드 (UserPrincipal 기반) */
  private String generateToken(
      UserPrincipal userPrincipal, TokenType tokenType, Key key, long expirationMs) {
    return generateToken(
        userPrincipal.getId(), userPrincipal.getEmail(), tokenType, key, expirationMs);
  }

  /** 토큰 생성 공통 메서드 (이메일, ID 기반) userId는 이제 null이 될 수 없음 */
  private String generateToken(
      Long userId, String email, TokenType tokenType, Key key, long expirationMs) {

    if (userId == null) {
      throw new IllegalArgumentException("토큰 생성 시 사용자 ID는 null이 될 수 없습니다");
    }

    Instant now = Instant.now();
    Instant expiryDate = now.plusMillis(expirationMs);
    String tokenId = UUID.randomUUID().toString();

    Map<String, Object> claims = new HashMap<>();
    claims.put("type", tokenType.name());
    claims.put("jti", tokenId);
    claims.put("userId", userId); // ID는 항상 포함
    claims.put("email", email); // 이메일은 Subject가 아닌 claim으로 포함

    String token =
        Jwts.builder()
            .setHeaderParam("typ", Header.JWT_TYPE)
            .setClaims(claims)
            .setSubject(userId.toString()) // Subject를 userId로 변경
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiryDate))
            .setIssuer(issuer)
            .setId(tokenId)
            .signWith(key)
            .compact();

    log.debug(
        "토큰 생성 완료 - 유형: {}, 사용자 ID: {}, 이메일: {}, 토큰 ID: {}, 만료: {}",
        tokenType,
        userId,
        email,
        tokenId,
        expiryDate);

    return token;
  }

  /** 토큰에서 사용자 이메일 추출 */
  public String getUserEmailFromToken(String token, TokenType tokenType) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(tokenType));
      return claims.get("email", String.class); // Subject 대신 email claim 사용
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("email", String.class);
    } catch (Exception e) {
      throw new TokenException("토큰에서 사용자 이메일을 추출할 수 없습니다", e);
    }
  }

  /** 토큰에서 사용자 ID 추출 */
  public Long getUserIdFromToken(String token, TokenType tokenType) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(tokenType));
      return claims.get("userId", Long.class);
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("userId", Long.class);
    } catch (Exception e) {
      throw new TokenException("토큰에서 사용자 ID를 추출할 수 없습니다", e);
    }
  }

  /** 토큰에서 토큰 ID(JTI) 추출 */
  public String getTokenIdFromToken(String token, TokenType tokenType) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(tokenType));
      return claims.getId();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getId();
    } catch (Exception e) {
      throw new TokenException("토큰에서 토큰 ID를 추출할 수 없습니다", e);
    }
  }

  /** 토큰에서 만료 시간 추출 */
  public Date getExpirationFromToken(String token, TokenType tokenType) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(tokenType));
      return claims.getExpiration();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getExpiration();
    } catch (Exception e) {
      throw new TokenException("토큰에서 만료 시간을 추출할 수 없습니다", e);
    }
  }

  /** 토큰 유효성 검증 */
  public boolean validateToken(String token, TokenType tokenType) {
    if (!StringUtils.hasText(token)) {
      log.debug("토큰이 비어있습니다");
      return false;
    }

    try {
      Key key = getKeyForTokenType(tokenType);
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

      // 토큰 타입 확인
      String tokenTypeValue = getTokenTypeFromToken(token, key);
      if (!tokenType.name().equals(tokenTypeValue)) {
        log.debug("토큰 타입이 일치하지 않습니다. 기대: {}, 실제: {}", tokenType, tokenTypeValue);
        return false;
      }

      return true;
    } catch (ExpiredJwtException e) {
      log.debug("만료된 토큰입니다: {}", e.getMessage());
      return false;
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("유효하지 않은 토큰입니다: {}", e.getMessage());
      return false;
    }
  }

  /** 토큰을 블랙리스트에 추가 */
  public void blacklistToken(String token, TokenType tokenType) {
    if (!StringUtils.hasText(token)) {
      return;
    }

    try {
      Date expiration = getExpirationFromToken(token, tokenType);
      long ttlInSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

      if (ttlInSeconds > 0) {
        String tokenId = getTokenIdFromToken(token, tokenType);
      }
    } catch (Exception e) {
      log.error("토큰 블랙리스트 처리 중 오류 발생", e);
    }
  }

  /** HTTP 요청 헤더에서 Bearer 토큰 추출 */
  public String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /** 토큰 타입에 따른 키 반환 */
  private Key getKeyForTokenType(TokenType tokenType) {
    return switch (tokenType) {
      case ACCESS -> accessTokenKey;
      case REFRESH -> refreshTokenKey;
      default -> throw new TokenException("지원되지 않는 토큰 타입: " + tokenType);
    };
  }

  /** 토큰 파싱하여 Claims 반환 */
  private Claims parseToken(String token, Key key) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }

  /** 토큰에서 토큰 타입 추출 */
  private String getTokenTypeFromToken(String token, Key key) {
    try {
      Claims claims = parseToken(token, key);
      return claims.get("type", String.class);
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("type", String.class);
    } catch (Exception e) {
      throw new TokenException("토큰에서 타입을 추출할 수 없습니다", e);
    }
  }

  /** 액세스 토큰 만료 시간 (밀리초) 반환 */
  public long getAccessTokenExpirationMs() {
    return accessTokenExpirationMs;
  }

  /** 리프레시 토큰 만료 시간 (밀리초) 반환 */
  public long getRefreshTokenExpirationMs() {
    return refreshTokenExpirationMs;
  }

  /**
   * User 객체로부터 액세스 토큰 생성 (이메일 중복 시 대비)
   *
   * @param email 사용자 이메일
   * @param userId 사용자 ID
   * @return 생성된 액세스 토큰
   */
  public String createAccessToken(Long userId, String email) {
    // userId를 반드시 포함하고 고유 식별자로 사용
    return generateToken(userId, email, TokenType.ACCESS, accessTokenKey, accessTokenExpirationMs);
  }

  /**
   * User 객체로부터 리프레시 토큰 생성
   *
   * @param email 사용자 이메일
   * @param userId 사용자 ID
   * @return 생성된 리프레시 토큰
   */
  public String createRefreshToken(Long userId, String email) {
    // userId를 반드시 포함하고 고유 식별자로 사용
    return generateToken(
        userId, email, TokenType.REFRESH, refreshTokenKey, refreshTokenExpirationMs);
  }

  /**
   * 토큰 유효성 검증 (간편 버전) 기본적으로 액세스 토큰으로 간주하고 검증
   *
   * @param token 검증할 토큰
   * @return 토큰 유효 여부
   */
  public boolean validateToken(String token) {
    return validateToken(token, TokenType.ACCESS);
  }

  /**
   * 토큰에서 사용자 ID 추출 (간편 버전) 기본적으로 액세스 토큰으로 간주하고 추출
   *
   * @param token 토큰
   * @return 사용자 ID
   */
  public Long getUserIdFromToken(String token) {
    return getUserIdFromToken(token, TokenType.ACCESS);
  }

  /**
   * 액세스 토큰 유효 기간을 초 단위로 반환
   *
   * @return 액세스 토큰 유효 기간(초)
   */
  public long getAccessTokenValidityInSeconds() {
    return accessTokenExpirationMs / 1000;
  }
}
