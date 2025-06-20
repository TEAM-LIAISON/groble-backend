package liaison.groble.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseHelper {

  public <T> ResponseEntity<GrobleResponse<T>> success(T data) {
    return success(data, "Success");
  }

  public <T> ResponseEntity<GrobleResponse<T>> success(T data, String message) {
    return success(data, message, HttpStatus.OK);
  }

  public <T> ResponseEntity<GrobleResponse<T>> success(T data, String message, HttpStatus status) {
    return ResponseEntity.status(status).body(GrobleResponse.success(data, message));
  }
}
