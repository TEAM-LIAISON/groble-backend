package liaison.groble.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liaison.groble.common.enums.GuestTokenScope;
import liaison.groble.security.oauth2.exception.TokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/** JWT 토큰 생성, 검증, 파싱 등을 담당하는 컴포넌트 */
@Slf4j
@Component
public class JwtTokenProvider {
  private final SecretKey accessTokenKey;
  private final SecretKey refreshTokenKey;
  private final SecretKey guestTokenKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;
  private final long guestTokenExpirationMs;
  private final String issuer;
  private final String environment;

  private static final String ENV_CLAIM = "env";

  /** 생성자 - 설정 값 주입 및 초기화 */
  public JwtTokenProvider(
      @Value("${app.jwt.access-token.secret}") String accessTokenSecret,
      @Value("${app.jwt.refresh-token.secret}") String refreshTokenSecret,
      @Value("${app.jwt.guest-token.secret}") String guestTokenSecret,
      @Value("${app.jwt.access-token.expiration-ms}") long accessTokenExpirationMs,
      @Value("${app.jwt.refresh-token.expiration-ms}") long refreshTokenExpirationMs,
      @Value("${app.jwt.guest-token.expiration-ms}") long guestTokenExpirationMs,
      @Value("${app.jwt.issuer:auth-service}") String issuer,
      @Value("${app.jwt.environment:local}") String environment) {

    // 안전한 키 생성 (HMAC-SHA-512 알고리즘용 키)
    this.accessTokenKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.guestTokenKey = Keys.hmacShaKeyFor(guestTokenSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    this.guestTokenExpirationMs = guestTokenExpirationMs; // 1 hour for guest tokens
    this.issuer = issuer;
    this.environment = environment;

    log.info(
        "JWT 토큰 제공자 초기화 완료 - 액세스 토큰 만료: {}ms, 리프레시 토큰 만료: {}ms",
        accessTokenExpirationMs,
        refreshTokenExpirationMs);
  }

  /** 토큰 생성 공통 메서드 (이메일, ID 기반) userId는 이제 null이 될 수 없음 */
  private String generateToken(
      Long userId, String email, TokenType tokenType, Key key, long expirationMs) {

    if (userId == null) {
      throw new IllegalArgumentException("토큰 생성 시 사용자 ID는 null이 될 수 없습니다");
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiryDate = now.plusSeconds(expirationMs / 1000);
    String tokenId = UUID.randomUUID().toString();

    Map<String, Object> claims = new HashMap<>();
    claims.put("type", tokenType.name());
    claims.put("jti", tokenId);
    claims.put("userId", userId);
    claims.put("email", email);
    claims.put(ENV_CLAIM, environment);

    String token =
        Jwts.builder()
            .setHeaderParam("typ", Header.JWT_TYPE)
            .setClaims(claims)
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
            .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
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
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .requireIssuer(issuer)
        .require(ENV_CLAIM, environment)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /** 액세스 토큰 만료 시간 (밀리초) 반환 */
  public long getAccessTokenExpirationMs() {
    return accessTokenExpirationMs;
  }

  /**
   * User 객체로부터 액세스 토큰 생성 (이메일 중복 시 대비)
   *
   * @param email 사용자 이메일
   * @param userId 사용자 ID
   * @return 생성된 액세스 토큰
   */
  public String createAccessToken(Long userId, String email) {
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
   * 액세스 토큰 유효 기간을 초 단위로 반환
   *
   * @return 액세스 토큰 유효 기간(초)
   */
  public long getAccessTokenValidityInSeconds() {
    return accessTokenExpirationMs / 1000;
  }

  public String createPasswordResetToken(String email, String secretKey, long expirationMinutes) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationMinutes * 60 * 1000);

    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String validatePasswordResetTokenAndGetEmail(String token, String secretKey) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      return claims.getSubject();
    } catch (Exception e) {
      throw new IllegalArgumentException("비밀번호 재설정 토큰이 유효하지 않거나 만료되었습니다.");
    }
  }

  public LocalDateTime getRefreshTokenExpirationAt(String refreshToken) {
    try {
      Claims claims = parseToken(refreshToken, refreshTokenKey);
      return claims.getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } catch (ExpiredJwtException e) {
      return e.getClaims()
          .getExpiration()
          .toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
    } catch (Exception e) {
      throw new TokenException("RefreshToken에서 만료 시간을 추출할 수 없습니다", e);
    }
  }

  public Long getUserIdFromRefreshToken(String token) {
    try {
      Claims claims = parseToken(token, getKeyForTokenType(TokenType.REFRESH));
      return claims.get("userId", Long.class);
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("userId", Long.class);
    } catch (Exception e) {
      throw new TokenException("리프레쉬 토큰에서 사용자 ID를 추출할 수 없습니다", e);
    }
  }

  public Long getUserId(String token, TokenType type) {
    try {
      return parseClaimsJws(token, type).get("userId", Long.class);
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("userId", Long.class);
    }
  }

  public String getEmail(String token, TokenType type) {
    try {
      return parseClaimsJws(token, type).get("email", String.class);
    } catch (ExpiredJwtException e) {
      return e.getClaims().get("email", String.class);
    }
  }

  public Claims parseClaimsJws(String token, TokenType type) {
    Key key = getKeyForTokenType(type); // ACCESS/REFRESH 키 반환
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .requireIssuer(issuer)
        .require(ENV_CLAIM, environment)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /** 리프레시 토큰의 만료 시간을 고려하여 액세스 토큰을 생성 액세스 토큰은 리프레시 토큰보다 길게 유효하지 않음 */
  public String createAccessTokenWithRefreshConstraint(
      Long userId, String email, String refreshToken) {
    try {
      // 리프레시 토큰의 만료 시간 가져오기
      LocalDateTime refreshExpiration = getRefreshTokenExpirationAt(refreshToken);
      LocalDateTime now = LocalDateTime.now();

      // 기본 액세스 토큰 만료 시간
      LocalDateTime defaultAccessExpiration = now.plusSeconds(accessTokenExpirationMs / 1000);

      // 액세스 토큰 만료 시간은 리프레시 토큰 만료 시간과 기본 액세스 토큰 만료 시간 중 더 이른 시간으로 설정
      LocalDateTime effectiveExpiration =
          refreshExpiration.isBefore(defaultAccessExpiration)
              ? refreshExpiration
              : defaultAccessExpiration;

      // 만료 시간이 현재보다 이전이면 토큰 발급 불가
      if (!effectiveExpiration.isAfter(now)) {
        throw new TokenException("유효한 토큰을 생성할 수 없습니다: 만료 시간이 현재 시간보다 이전입니다");
      }

      // 밀리초 단위로 변환
      long expirationMs = Duration.between(now, effectiveExpiration).toMillis();

      // 액세스 토큰 생성 (제한된, 더 짧은 만료 시간 사용)
      return generateToken(userId, email, TokenType.ACCESS, accessTokenKey, expirationMs);
    } catch (Exception e) {
      throw new TokenException("제한된 액세스 토큰 생성 실패", e);
    }
  }

  // 게스트 토큰 생성 (기본: FULL_ACCESS)
  public String createGuestToken(Long guestUserId) {
    return createGuestTokenWithScope(guestUserId, GuestTokenScope.FULL_ACCESS);
  }

  // 스코프 지정 게스트 토큰 생성
  public String createGuestTokenWithScope(Long guestUserId, GuestTokenScope scope) {
    LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(guestTokenExpirationMs / 1000);
    return Jwts.builder()
        .setSubject("guest:" + guestUserId)
        .claim("type", "GUEST")
        .claim(ENV_CLAIM, environment)
        .claim("scope", scope.getCode())
        .claim("roles", List.of("ROLE_GUEST"))
        .setIssuedAt(new Date())
        .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
        .setIssuer(issuer)
        .signWith(guestTokenKey)
        .compact();
  }

  // 게스트 토큰에서 스코프 추출
  public GuestTokenScope getGuestTokenScope(String guestToken) {
    try {
      Claims claims = getClaims(guestToken);
      String scopeCode = claims.get("scope", String.class);
      return scopeCode != null ? GuestTokenScope.fromCode(scopeCode) : GuestTokenScope.FULL_ACCESS;
    } catch (Exception e) {
      throw new TokenException("게스트 토큰에서 스코프를 추출할 수 없습니다", e);
    }
  }

  // Guest ID 추출
  public Long getGuestIdFromToken(String token) {
    Claims claims = getClaims(token);
    String subject = claims.getSubject(); // "guest:123"

    return Long.valueOf(subject.substring(6));
  }

  // getClaims 메서드
  private Claims getClaims(String token) {
    if (token == null || token.isBlank()) {
      throw new JwtException("토큰이 비어 있습니다.");
    }
    try {
      return Jwts.parserBuilder()
          .setSigningKey(guestTokenKey) // secretKey → guestTokenKey로 변경
          .requireIssuer(issuer)
          .require(ENV_CLAIM, environment)
          .setAllowedClockSkewSeconds(60) // 스큐 허용
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      log.warn("JWT expired (exp={})", e.getClaims().getExpiration());
      throw new JwtException("토큰이 만료되었습니다", e);
    } catch (io.jsonwebtoken.security.SignatureException e) {
      log.warn("JWT signature invalid");
      throw new JwtException("서명이 유효하지 않습니다", e);
    } catch (UnsupportedJwtException e) {
      log.warn("JWT unsupported");
      throw new JwtException("지원되지 않는 토큰입니다", e);
    } catch (MalformedJwtException e) {
      log.warn("JWT malformed");
      throw new JwtException("잘못된 토큰 형식입니다", e);
    } catch (IllegalArgumentException e) {
      log.warn("JWT empty/illegal");
      throw new JwtException("토큰 검증에 실패했습니다", e);
    }
  }

  /**
   * 게스트 토큰 유효성 검증
   *
   * @param token 검증할 게스트 토큰
   * @return 유효하면 true, 아니면 false
   */
  public boolean validateGuestToken(String token) {
    try {
      // 토큰이 없으면 false
      if (token == null || token.isBlank()) {
        return false;
      }

      // Claims 파싱 (만료, 서명 등 자동 검증됨)
      Claims claims = getClaims(token);

      // 게스트 토큰인지 확인
      String type = claims.get("type", String.class);
      if (!"GUEST".equals(type)) {
        log.warn("토큰 타입이 GUEST가 아님: {}", type);
        return false;
      }

      // subject 형식 검증 (guest:숫자 형식인지)
      String subject = claims.getSubject();
      if (subject == null || !subject.startsWith("guest:")) {
        log.warn("잘못된 게스트 토큰 subject 형식: {}", subject);
        return false;
      }

      // 만료 시간 체크 (이미 parseClaimsJws에서 체크되지만 명시적으로)
      Date expiration = claims.getExpiration();
      if (expiration != null && expiration.before(new Date())) {
        log.debug("게스트 토큰 만료됨");
        return false;
      }

      return true;

    } catch (ExpiredJwtException e) {
      log.debug("게스트 토큰 만료: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.debug("게스트 토큰 검증 실패: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      log.error("게스트 토큰 검증 중 예상치 못한 오류", e);
      return false;
    }
  }
}
