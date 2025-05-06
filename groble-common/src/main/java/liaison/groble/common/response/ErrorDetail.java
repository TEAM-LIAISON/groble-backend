package liaison.groble.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** API 오류 응답의 상세 정보를 담는 클래스 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "에러 상세 정보")
public class ErrorDetail {
  private String code; // 오류 코드
  private String message; // 오류 메시지
  private String exception; // 예외 클래스 이름
  private String field; // 오류가 발생한 필드명
  private String trace; // 오류 발생 시점의 스택 트레이스 (개발 환경에서만 포함)
}
