package liaison.groble.common.response;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseHelper {

  /** 성공 응답 생성 (데이터 없음, 커스텀 메시지) */
  public ResponseEntity<GrobleResponse<Void>> success(String message, HttpStatus status) {
    return ResponseEntity.status(status)
        .body(
            GrobleResponse.<Void>builder()
                .status(ResponseStatus.SUCCESS)
                .code(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
  }

  /** 성공 응답 생성 (데이터 포함, 커스텀 메시지) */
  public <T> ResponseEntity<GrobleResponse<T>> success(T data, String message, HttpStatus status) {
    return ResponseEntity.status(status)
        .body(
            GrobleResponse.<T>builder()
                .status(ResponseStatus.SUCCESS)
                .code(status.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build());
  }

  /** 성공 응답 생성 (데이터만) */
  public <T> ResponseEntity<GrobleResponse<T>> success(T data, HttpStatus status) {
    return success(data, "요청이 성공적으로 처리되었습니다.", status);
  }
}
