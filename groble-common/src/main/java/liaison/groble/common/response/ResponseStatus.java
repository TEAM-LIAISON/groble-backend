package liaison.groble.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "응답 상태 타입")
public enum ResponseStatus {
  @Schema(description = "요청 성공")
  SUCCESS,

  @Schema(description = "요청 실패 (서버 오류)")
  ERROR,

  @Schema(description = "요청 실패 (비즈니스 로직 오류)")
  FAIL
}
