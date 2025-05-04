package liaison.groble.common.response;

import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // GrobleResponse로 이미 래핑된 응답이나 void 반환 메서드는 처리하지 않음
    return !returnType.getParameterType().equals(GrobleResponse.class)
        && !returnType.getParameterType().equals(Void.TYPE);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    // Special paths handling (unchanged)
    if (request instanceof ServletServerHttpRequest) {
      String path = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();
      if ("/env".equals(path) || isSwaggerRequest(path)) {
        return body;
      }
    }

    // Check if this is a Spring error response
    if (isSpringErrorResponse(body)) {
      Map<String, Object> errorMap = (Map<String, Object>) body;
      String errorMessage = (String) errorMap.get("message");
      Integer status = (Integer) errorMap.get("status");

      // Convert to your error response format
      return GrobleResponse.error(errorMessage, status);
    }

    // Rest of your logic remains the same
    if (body instanceof String) {
      return body;
    }

    if (body instanceof GrobleResponse) {
      return body;
    }

    if (body == null) {
      return GrobleResponse.success();
    }

    return GrobleResponse.success(body);
  }

  private boolean isSpringErrorResponse(Object body) {
    if (!(body instanceof Map)) {
      return false;
    }

    Map<String, Object> map = (Map<String, Object>) body;
    return map.containsKey("timestamp")
        && map.containsKey("status")
        && map.containsKey("error")
        && map.containsKey("path");
  }

  // Swagger 관련 요청인지 확인하는 메소드
  private boolean isSwaggerRequest(String path) {
    return path.contains("/swagger-ui")
        || path.contains("/v3/api-docs")
        || path.contains("/swagger-resources");
  }
}
