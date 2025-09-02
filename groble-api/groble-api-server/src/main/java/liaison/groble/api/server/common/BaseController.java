package liaison.groble.api.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import liaison.groble.common.response.GrobleResponse;
import liaison.groble.common.response.ResponseHelper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseController {

  protected final ResponseHelper responseHelper;

  protected <T> ResponseEntity<GrobleResponse<T>> success(T data, String message) {
    return responseHelper.success(data, message, HttpStatus.OK);
  }

  protected <T> ResponseEntity<GrobleResponse<T>> success(
      T data, String message, HttpStatus status) {
    return responseHelper.success(data, message, status);
  }

  protected ResponseEntity<GrobleResponse<Void>> successVoid(String message) {
    return responseHelper.success(null, message, HttpStatus.OK);
  }

  protected ResponseEntity<GrobleResponse<Void>> successVoid(String message, HttpStatus status) {
    return responseHelper.success(null, message, status);
  }
}
