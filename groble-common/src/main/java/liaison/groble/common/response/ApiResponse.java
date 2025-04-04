package liaison.groble.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 응답에 대한 공통 형식을 제공하는 클래스 모든 API 응답은 이 형식을 따르도록 구현
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON에 포함하지 않음
public class ApiResponse<T> {
  private ResponseStatus status; // 응답 상태 (SUCCESS, ERROR)
  private int code; // HTTP 상태 코드 또는 커스텀 코드
  private String message; // 응답 메시지

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data; // 응답 데이터 (제네릭 타입)

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ErrorDetail error; // 에러 상세 정보 (에러 발생 시에만 포함)

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime timestamp; // 응답 생성 시간

  /**
   * 성공 응답 생성 (데이터 없음)
   *
   * @return 성공 상태의 API 응답
   */
  public static ApiResponse<Void> success() {
    return ApiResponse.<Void>builder()
        .status(ResponseStatus.SUCCESS)
        .code(200)
        .message("요청이 성공적으로 처리되었습니다.")
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 성공 응답 생성 (데이터 포함)
   *
   * @param <T> 응답 데이터의 타입
   * @param data 응답 데이터
   * @return 성공 상태의 API 응답 (데이터 포함)
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .status(ResponseStatus.SUCCESS)
        .code(200)
        .message("요청이 성공적으로 처리되었습니다.")
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 성공 응답 생성 (데이터 및 커스텀 메시지 포함)
   *
   * @param <T> 응답 데이터의 타입
   * @param data 응답 데이터
   * @param message 응답 메시지
   * @return 성공 상태의 API 응답 (데이터 및 메시지 포함)
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .status(ResponseStatus.SUCCESS)
        .code(200)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 성공 응답 생성 (데이터, 메시지, 상태 코드 포함)
   *
   * @param <T> 응답 데이터의 타입
   * @param data 응답 데이터
   * @param message 응답 메시지
   * @param code HTTP 상태 코드
   * @return 성공 상태의 API 응답 (데이터, 메시지, 코드 포함)
   */
  public static <T> ApiResponse<T> success(T data, String message, int code) {
    return ApiResponse.<T>builder()
        .status(ResponseStatus.SUCCESS)
        .code(code)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 에러 응답 생성 (기본)
   *
   * @param message 에러 메시지
   * @param code HTTP 에러 코드
   * @return 에러 상태의 API 응답
   */
  public static ApiResponse<Void> error(String message, int code) {
    return ApiResponse.<Void>builder()
        .status(ResponseStatus.ERROR)
        .code(code)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 에러 응답 생성 (상세 정보 포함)
   *
   * @param message 에러 메시지
   * @param code HTTP 에러 코드
   * @param errorDetail 에러 상세 정보
   * @return 에러 상태의 API 응답 (상세 정보 포함)
   */
  public static ApiResponse<Void> error(String message, int code, ErrorDetail errorDetail) {
    return ApiResponse.<Void>builder()
        .status(ResponseStatus.ERROR)
        .code(code)
        .message(message)
        .error(errorDetail)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /**
   * 에러 응답 생성 (예외 포함)
   *
   * @param message 에러 메시지
   * @param code HTTP 에러 코드
   * @param exception 발생한 예외
   * @return 에러 상태의 API 응답 (예외 정보 포함)
   */
  public static ApiResponse<Void> error(String message, int code, Exception exception) {
    ErrorDetail errorDetail =
        ErrorDetail.builder()
            .exception(exception.getClass().getName())
            .message(exception.getMessage())
            .build();

    return error(message, code, errorDetail);
  }

  /**
   * 실패 응답 생성 (비즈니스 로직 실패)
   *
   * @param message 실패 메시지
   * @param code 상태 코드
   * @return 실패 상태의 API 응답
   */
  public static ApiResponse<Void> fail(String message, int code) {
    return ApiResponse.<Void>builder()
        .status(ResponseStatus.FAIL)
        .code(code)
        .message(message)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
