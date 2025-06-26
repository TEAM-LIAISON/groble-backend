package liaison.groble.common.service;

import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClientInfoService {

  private static final String[] IP_HEADER_CANDIDATES = {
    "X-Forwarded-For",
    "X-Real-IP",
    "Proxy-Client-IP",
    "WL-Proxy-Client-IP",
    "HTTP_CLIENT_IP",
    "HTTP_X_FORWARDED_FOR"
  };

  /**
   * 요청 헤더에서 클라이언트 IP 주소를 추출합니다.
   *
   * @param request HttpServletRequest
   * @return 클라이언트 IP 주소
   */
  public String getClientIpAddress(HttpServletRequest request) {
    return Stream.of(IP_HEADER_CANDIDATES)
        .map(request::getHeader)
        .filter(ip -> StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip))
        .findFirst()
        .map(ip -> ip.contains(",") ? ip.split(",")[0].trim() : ip)
        .orElse(request.getRemoteAddr());
  }
}
