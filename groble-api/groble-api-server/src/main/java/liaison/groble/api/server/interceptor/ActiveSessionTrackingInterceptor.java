package liaison.groble.api.server.interceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import liaison.groble.application.session.ActiveSessionService;
import liaison.groble.application.session.GuestActivityCommand;
import liaison.groble.application.session.MemberActivityCommand;
import liaison.groble.security.jwt.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveSessionTrackingInterceptor implements HandlerInterceptor {

  private static final String ADMIN_PATH_PREFIX = "/api/v1/admin";
  private static final String ACTIVE_VISITOR_ENDPOINT = "/api/v1/admin/dashboard/active-visitors";

  private final ActiveSessionService activeSessionService;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {

    if (shouldSkip(request)) {
      return true;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String requestUri = request.getRequestURI();
    String httpMethod = request.getMethod();
    String queryString = request.getQueryString();
    String referer = request.getHeader("Referer");
    String userAgent = request.getHeader("User-Agent");
    String anonymousId = request.getHeader("X-Anonymous-Id");
    String clientIp = extractClientIp(request);
    String fingerprint = generateFingerprint(clientIp, userAgent, anonymousId);
    Instant now = Instant.now();

    if (isMemberAuthentication(authentication)) {
      UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles = extractRoles(authentication.getAuthorities());

      MemberActivityCommand command =
          MemberActivityCommand.builder()
              .sessionKey(buildMemberSessionKey(principal.getId(), fingerprint))
              .userId(principal.getId())
              .accountType(principal.getAccountType())
              .lastUserType(principal.getLastUserType())
              .roles(roles)
              .requestUri(requestUri)
              .httpMethod(httpMethod)
              .queryString(queryString)
              .referer(referer)
              .clientIp(clientIp)
              .userAgent(userAgent)
              .clientFingerprint(fingerprint)
              .occurredAt(now)
              .build();

      activeSessionService.recordMemberActivity(command);
      return true;
    }

    Long guestId = resolveGuestId(authentication);
    boolean authenticatedGuest = guestId != null;

    GuestActivityCommand command =
        GuestActivityCommand.builder()
            .sessionKey(buildGuestSessionKey(guestId, fingerprint))
            .guestId(guestId)
            .authenticated(authenticatedGuest)
            .anonymousId(anonymousId)
            .requestUri(requestUri)
            .httpMethod(httpMethod)
            .queryString(queryString)
            .referer(referer)
            .clientIp(clientIp)
            .userAgent(userAgent)
            .clientFingerprint(fingerprint)
            .occurredAt(now)
            .build();

    activeSessionService.recordGuestActivity(command);
    return true;
  }

  private boolean shouldSkip(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (!uri.startsWith("/api/")) {
      return true;
    }
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    if (uri.startsWith(ADMIN_PATH_PREFIX)) {
      return true;
    }
    if (ACTIVE_VISITOR_ENDPOINT.equals(uri)) {
      return true;
    }
    return false;
  }

  private boolean isMemberAuthentication(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return false;
    }
    return authentication.getPrincipal() instanceof UserDetailsImpl;
  }

  private Long resolveGuestId(Authentication authentication) {
    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
      return null;
    }

    String name = authentication.getName();
    if (name == null || !name.startsWith("guest_")) {
      return null;
    }

    try {
      return Long.parseLong(name.substring("guest_".length()));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private List<String> extractRoles(Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null) {
      return List.of();
    }
    return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
  }

  private String buildMemberSessionKey(Long userId, String fingerprint) {
    return "member:" + userId + ":" + fingerprint;
  }

  private String buildGuestSessionKey(Long guestId, String fingerprint) {
    if (guestId != null) {
      return "guest:" + guestId + ":" + fingerprint;
    }
    return "anonymous:" + fingerprint;
  }

  private String extractClientIp(HttpServletRequest request) {
    String[] headerNames = {
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_CLIENT_IP",
      "HTTP_X_FORWARDED_FOR"
    };

    for (String header : headerNames) {
      String value = request.getHeader(header);
      if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
        if (value.contains(",")) {
          return value.split(",")[0].trim();
        }
        return value;
      }
    }

    return request.getRemoteAddr();
  }

  private String generateFingerprint(String ip, String userAgent, String anonymousId) {
    String raw = String.join("|", nullSafe(ip), nullSafe(userAgent), nullSafe(anonymousId));
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      log.warn("SHA-256 not available for fingerprint generation", e);
      return Integer.toHexString(raw.hashCode());
    }
  }

  private String nullSafe(String value) {
    return value == null ? "" : value;
  }
}
