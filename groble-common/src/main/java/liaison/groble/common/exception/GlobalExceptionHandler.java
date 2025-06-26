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

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<GrobleResponse<Void>> handleInvalidRequestException(
      InvalidRequestException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(GrobleResponse.error(e.getMessage(), 400, e));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<GrobleResponse<Void>> handleEntityNotFoundException(
      EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND) // 404 Not Found
        .body(GrobleResponse.error(ex.getMessage(), 404, ex));
  }

  @ExceptionHandler(DuplicateMarketLinkException.class)
  public ResponseEntity<GrobleResponse<Void>> handleDuplicateMarketLinkException(
      DuplicateMarketLinkException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(GrobleResponse.error(ex.getMessage(), 409, ex));
  }

  @ExceptionHandler(GrobleException.class)
  public ResponseEntity<GrobleResponse<Void>> handleGrobleException(GrobleException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(GrobleResponse.error(ex.getMessage(), ex.getStatusCode()));
  }
}
