package liaison.grobleauth.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.grobleauth.exception.TokenException;
import liaison.grobleauth.model.TokenType;
import liaison.grobleauth.model.UserPrincipal;
import liaison.grobleauth.service.MetricsService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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
  private final MetricsService metricsService;
  private final TokenBlacklistService tokenBlacklistService;

  /** 생성자 - 설정 값 주입 및 초기화 */
  public JwtTokenProvider(
      @Value("${app.jwt.access-token.secret}") String accessTokenSecret,
      @Value("${app.jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${app.jwt.access-token.expiration-ms}") long accessTokenExpirationMs,
      @Value("${app.jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs,
      @Value("${app.jwt.issuer:auth-service}") String issuer,
      MetricsService metricsService,
      TokenBlacklistService tokenBlacklistService) {

    // 안전한 키 생성 (HMAC-SHA-512 알고리즘용 키)
    this.accessTokenKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    this.issuer = issuer;
    this.metricsService = metricsService;
    this.tokenBlacklistService = tokenBlacklistService;

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

  /** 이메일로부터 액세스 토큰 생성 */
  public String generateAccessToken(String email) {
    return generateToken(email, null, TokenType.ACCESS, accessTokenKey, accessTokenExpirationMs);
  }

  /** 리프레시 토큰 생성 */
  public String generateRefreshToken(UserPrincipal userPrincipal) {
    return generateToken(
        userPrincipal, TokenType.REFRESH, refreshTokenKey, refreshTokenExpirationMs);
  }

  /** 이메일로부터 리프레시 토큰 생성 */
  public String generateRefreshToken(String email) {
    return generateToken(email, null, TokenType.REFRESH, refreshTokenKey, refreshTokenExpirationMs);
  }

  /** 토큰 생성 공통 메서드 (UserPrincipal 기반) */
  private String generateToken(
      UserPrincipal userPrincipal, TokenType tokenType, Key key, long expirationMs) {
    return generateToken(
        userPrincipal.getEmail(), userPrincipal.getId(), tokenType, key, expirationMs);
  }

  /** 토큰 생성 공통 메서드 (이메일, ID 기반) */
  private String generateToken(
      String email, Long userId, TokenType tokenType, Key key, long expirationMs) {
    Instant now = Instant.now();
    Instant expiryDate = now.plusMillis(expirationMs);
    String tokenId = UUID.randomUUID().toString();

    Map<String, Object> claims = new HashMap<>();
    claims.put("type", tokenType.name());
    claims.put("jti", tokenId);

    if (userId != null) {
      claims.put("userId", userId);
    }

    String token =
        Jwts.builder()
            .header()
            .type(Header.JWT_TYPE) // setHeaderParam 대신 header().type() 사용
            .and()
            .claims(claims) // setClaims 대신 claims() 사용
            .subject(email) // setSubject 대신 subject() 사용
            .issuedAt(Date.from(now)) // setIssuedAt 대신 issuedAt() 사용
            .expiration(Date.from(expiryDate)) // setExpiration 대신 expiration() 사용
            .issuer(issuer) // setIssuer 대신 issuer() 사용
            .id(tokenId) // setId 대신 id() 사용
            .signWith(key) // signWith에서 알고리즘 파라미터 제거
            .compact();

    // 메트릭 기록
    metricsService.recordTokenGeneration(tokenType);

    log.debug(
        "토큰 생성 완료 - 유형: {}, 사용자: {}, 토큰 ID: {}, 만료: {}", tokenType, email, tokenId, expiryDate);

    return token;
  }

  /** 토큰에서 사용자 이메일 추출 */
  public String getUserEmailFromToken(String token, TokenType tokenType) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(tokenType));
      return claims.getSubject();
    } catch (ExpiredJwtException e) {
      return e.getClaims().getSubject();
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

    // 블랙리스트 확인
    if (tokenBlacklistService.isBlacklisted(token)) {
      log.debug("블랙리스트에 등록된 토큰입니다");
      metricsService.recordTokenValidationFailure(tokenType, "blacklisted");
      return false;
    }

    try {
      Key key = getKeyForTokenType(tokenType);
      Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload();

      // 토큰 타입 확인
      String tokenTypeValue = getTokenTypeFromToken(token, key);
      if (!tokenType.name().equals(tokenTypeValue)) {
        log.debug("토큰 타입이 일치하지 않습니다. 기대: {}, 실제: {}", tokenType, tokenTypeValue);
        metricsService.recordTokenValidationFailure(tokenType, "wrong_type");
        return false;
      }

      metricsService.recordTokenValidationSuccess(tokenType);
      return true;
    } catch (SecurityException | SignatureException e) {
      log.debug("유효하지 않은 JWT 서명: {}", e.getMessage());
      metricsService.recordTokenValidationFailure(tokenType, "invalid_signature");
    } catch (MalformedJwtException e) {
      log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
      metricsService.recordTokenValidationFailure(tokenType, "malformed");
    } catch (ExpiredJwtException e) {
      log.debug("만료된 JWT 토큰: {}", e.getMessage());
      metricsService.recordTokenValidationFailure(tokenType, "expired");
    } catch (UnsupportedJwtException e) {
      log.debug("지원되지 않는 JWT 토큰: {}", e.getMessage());
      metricsService.recordTokenValidationFailure(tokenType, "unsupported");
    } catch (IllegalArgumentException e) {
      log.debug("JWT 토큰 문자열이 비어있습니다: {}", e.getMessage());
      metricsService.recordTokenValidationFailure(tokenType, "empty");
    } catch (Exception e) {
      log.error("JWT 토큰 검증 중 예상치 못한 오류 발생", e);
      metricsService.recordTokenValidationFailure(tokenType, "unexpected");
    }

    return false;
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
        tokenBlacklistService.addToBlacklist(token, tokenId, ttlInSeconds, TimeUnit.SECONDS);
        log.debug("토큰을 블랙리스트에 추가했습니다. 토큰 ID: {}, TTL: {}초", tokenId, ttlInSeconds);
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
    return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload();
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
   * User 객체로부터 액세스 토큰 생성
   *
   * @param user 사용자 객체
   * @return 생성된 액세스 토큰
   */
  public String createAccessToken(liaison.groblecore.domain.User user) {
    return generateAccessToken(user.getEmail());
  }

  /**
   * User 객체로부터 리프레시 토큰 생성
   *
   * @param user 사용자 객체
   * @return 생성된 리프레시 토큰
   */
  public String createRefreshToken(liaison.groblecore.domain.User user) {
    return generateRefreshToken(user.getEmail());
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
