package liaison.groble.security.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/** JWT 인증 진입점 구현 클래스 (인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출) */
@Component
public class JwtTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", "인증이 필요합니다.");
    errorResponse.put("path", request.getRequestURI());

    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}
