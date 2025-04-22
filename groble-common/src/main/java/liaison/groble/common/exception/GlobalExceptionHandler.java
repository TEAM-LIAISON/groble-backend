package liaison.groble.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import liaison.groble.common.response.GrobleResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DuplicateNicknameException.class)
  public ResponseEntity<GrobleResponse<Void>> handleDuplicateNicknameException(
      DuplicateNicknameException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
        .body(GrobleResponse.error(ex.getMessage(), 409, ex));
  }
}
