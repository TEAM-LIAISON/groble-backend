package liaison.groble.application.dashboard.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * 조회수/트래킹 기록 시 뷰어 식별 정보를 생성하는 유틸리티.
 *
 * <p>익명 사용자는 IP + UserAgent 를 기반으로 해시를 구성하고, 로그인 사용자는 userId를 우선 사용합니다. 또한 로그 테이블에 저장될 visitor_hash
 * 값도 동일한 기준의 SHA-256으로 생성합니다.
 */
@Component
@RequiredArgsConstructor
public class ViewTrackingKeyGenerator {

  private static final int MAX_USER_AGENT_LENGTH = 500;
  private static final int MAX_IP_LENGTH = 45;
  private static final HexFormat HEX_FORMAT = HexFormat.of();

  @Value("${spring.flyway.placeholders.visitor.hash.salt}")
  private String visitorHashSalt;

  public ViewerIdentity generate(Long userId, String ip, String userAgent) {
    String normalizedIp = normalizeIp(ip);
    String normalizedUserAgent = normalizeUserAgent(userAgent);

    String visitorHash = sha256(visitorHashSalt + "|" + normalizedIp + "|" + normalizedUserAgent);

    String viewerKey;
    if (userId != null) {
      viewerKey = "user:" + userId;
    } else {
      String anonSeed = normalizedIp + "|" + normalizedUserAgent;
      viewerKey = "anon:" + sha256("viewer|" + anonSeed);
    }

    return new ViewerIdentity(viewerKey, visitorHash, normalizedIp, normalizedUserAgent);
  }

  private String normalizeIp(String ip) {
    String candidate = StringUtils.hasText(ip) ? ip.trim() : "unknown";
    return candidate.length() > MAX_IP_LENGTH ? candidate.substring(0, MAX_IP_LENGTH) : candidate;
  }

  private String normalizeUserAgent(String userAgent) {
    String candidate = StringUtils.hasText(userAgent) ? userAgent.trim() : "unknown";
    return candidate.length() > MAX_USER_AGENT_LENGTH
        ? candidate.substring(0, MAX_USER_AGENT_LENGTH)
        : candidate;
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedHash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return HEX_FORMAT.formatHex(encodedHash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  public record ViewerIdentity(
      String viewerKey, String visitorHash, String normalizedIp, String normalizedUserAgent) {}
}
