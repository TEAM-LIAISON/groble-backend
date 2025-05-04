package liaison.groble.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** REST 컨트롤러의 응답을 자동으로 ApiResponse 형식으로 래핑하는 어드바이스 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // ApiResponse로 이미 래핑된 응답이나 void 반환 메서드는 처리하지 않음
    return !returnType.getParameterType().equals(ApiResponse.class)
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

    // /env 경로 및 Swagger 관련 경로 요청은 래핑하지 않음
    if (request instanceof ServletServerHttpRequest) {
      String path = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();
      if ("/env".equals(path) || isSwaggerRequest(path)) {
        return body;
      }
    }

    // String 타입 특별 처리
    if (body instanceof String) {
      // 문자열 타입은 래핑하지 않고 그대로 반환
      return body;
    }

    // 이미 ApiResponse로 래핑된 경우 그대로 반환
    if (body instanceof ApiResponse) {
      return body;
    }

    // 로그인 응답이나 이미 독립적인 응답 형식을 갖춘 객체는 래핑하지 않음
    if (isStandaloneResponse(body)) {
      return body;
    }

    // null인 경우 빈 성공 응답 반환
    if (body == null) {
      return ApiResponse.success();
    }

    // 일반 객체인 경우 성공 응답으로 래핑하여 반환
    return ApiResponse.success(body);
  }

  // 독립적인 응답 형식을 갖춘 객체인지 확인하는 메소드
  private boolean isStandaloneResponse(Object body) {
    // 클래스명으로 확인하거나 마커 인터페이스를 사용할 수 있음
    return body.getClass().getSimpleName().endsWith("Response") && hasResponseStructure(body);
  }

  // 응답 구조를 갖추고 있는지 확인
  private boolean hasResponseStructure(Object body) {
    try {
      // reflection을 사용하여 필요한 필드가 있는지 확인
      return body.getClass().getDeclaredField("status") != null
          && body.getClass().getDeclaredField("code") != null
          && body.getClass().getDeclaredField("message") != null
          && body.getClass().getDeclaredField("data") != null;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  // Swagger 관련 요청인지 확인하는 메소드
  private boolean isSwaggerRequest(String path) {
    return path.contains("/swagger-ui")
        || path.contains("/v3/api-docs")
        || path.contains("/swagger-resources");
  }
}
