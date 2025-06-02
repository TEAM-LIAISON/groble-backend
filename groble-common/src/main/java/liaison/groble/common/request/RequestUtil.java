package liaison.groble.common.request;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestUtil {

  private final HttpServletRequest request;

  /** 클라이언트 IP 주소를 가져옵니다. 프록시를 통한 요청 시 실제 클라이언트 IP를 반환합니다. */
  public String getClientIp() {
    String clientIp = request.getHeader("X-Forwarded-For");

    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("WL-Proxy-Client-IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_CLIENT_IP");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getRemoteAddr();
    }

    // X-Forwarded-For에는 여러 IP가 쉼표로 구분돼 있을 수 있음
    if (clientIp != null && clientIp.contains(",")) {
      clientIp = clientIp.split(",")[0].trim();
    }

    return clientIp;
  }

  /** 클라이언트 User-Agent 정보를 가져옵니다. */
  public String getUserAgent() {
    return request.getHeader("User-Agent");
  }
}
