package liaison.groble.common.response;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
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

    // 이미 ApiResponse로 래핑된 경우 그대로 반환
    if (body instanceof ApiResponse) {
      return body;
    }

    // null인 경우 빈 성공 응답 반환
    if (body == null) {
      return ApiResponse.success();
    }

    // 일반 객체인 경우 성공 응답으로 래핑하여 반환
    return ApiResponse.success(body);
  }
}
