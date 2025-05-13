package liaison.groble.security.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** JWT 접근 거부 핸들러 (인증된 사용자가 권한이 없는 리소스에 접근할 때 호출) */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {

    log.error("접근 권한 없음: {}, 경로: {}", accessDeniedException.getMessage(), request.getRequestURI());

    // HTTP 403 (Forbidden) 상태 코드 설정
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // 클라이언트에게 반환할 오류 메시지 생성
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
    errorResponse.put("error", "Forbidden");
    errorResponse.put("message", "이 리소스에 접근할 권한이 없습니다.");
    errorResponse.put("path", request.getRequestURI());

    // JSON 형식으로 응답 작성
    objectMapper.writeValue(response.getOutputStream(), errorResponse);
  }
}
