package liaison.groble.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

  /** 응답 성공 여부 */
  private boolean success;

  /** 응답 코드 성공 시 "SUCCESS", 실패 시 에러 코드 */
  private String code;

  /** 응답 메시지 성공 시 성공 메시지, 실패 시 에러 메시지 */
  private String message;

  /** 응답 데이터 실패 시에는 null이 될 수 있음 */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
  }

  /**
   * 성공 응답 생성 (데이터 + 커스텀 메시지)
   *
   * @param data 응답 데이터
   * @param message 커스텀 성공 메시지
   * @param <T> 데이터 타입
   * @return 성공 응답 객체
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, "SUCCESS", message, data);
  }

  /**
   * 데이터 없는 성공 응답 생성
   *
   * @return 성공 응답 객체
   */
  public static ApiResponse<Void> success() {
    return new ApiResponse<>(true, "SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
  }

  /**
   * 데이터 없는 성공 응답 생성 (커스텀 메시지)
   *
   * @param message 커스텀 성공 메시지
   * @return 성공 응답 객체
   */
  public static ApiResponse<Void> success(String message) {
    return new ApiResponse<>(true, "SUCCESS", message, null);
  }

  /**
   * 에러 응답 생성
   *
   * @param code 에러 코드
   * @param message 에러 메시지
   * @return 에러 응답 객체
   */
  public static ApiResponse<Void> error(String code, String message) {
    return new ApiResponse<>(false, code, message, null);
  }
}
