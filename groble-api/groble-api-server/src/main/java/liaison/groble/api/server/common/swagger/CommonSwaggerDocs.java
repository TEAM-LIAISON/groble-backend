package liaison.groble.api.server.common.swagger;

public final class CommonSwaggerDocs {

  private CommonSwaggerDocs() {}

  // === 공통 응답 메시지 ===
  public static final String SUCCESS_200 = "요청 성공";
  public static final String CREATED_201 = "생성 성공";
  public static final String NO_CONTENT_204 = "성공 (응답 데이터 없음)";

  // === 공통 에러 메시지 ===
  public static final String BAD_REQUEST = "잘못된 요청 (유효성 검증 실패, 필수값 누락 등)";
  public static final String UNAUTHORIZED = "인증 필요 (토큰 없음 또는 만료)";
  public static final String FORBIDDEN = "권한 없음 (접근 권한 부족)";
  public static final String NOT_FOUND = "리소스를 찾을 수 없음";
  public static final String CONFLICT = "충돌 (중복된 요청 또는 처리 불가능한 상태)";
  public static final String SERVER_ERROR = "서버 내부 오류";

  // === 공통 파라미터 설명 ===
  public static final String USER_ID_DESC = "사용자 ID";
  public static final String PAGE_DESC = "페이지 번호 (0부터 시작)";
  public static final String SIZE_DESC = "페이지 크기";
  public static final String SORT_DESC = "정렬 기준 (예: id,desc)";

  // === 공통 예제 ===
  public static final String USER_ID_EXAMPLE = "12345";
  public static final String PAGE_EXAMPLE = "0";
  public static final String SIZE_EXAMPLE = "20";

  // === 공통 스키마 설명 ===
  public static final String GROBLE_RESPONSE_DESC =
      "그로블 표준 응답 형태 (status, code, message, data, error, timestamp 포함)";
  public static final String PAGE_RESPONSE_DESC = "페이징 처리된 응답 형태";

  // === 응답 상태 값 ===
  public static final String STATUS_SUCCESS = "SUCCESS";
  public static final String STATUS_ERROR = "ERROR";
  public static final String STATUS_FAIL = "FAIL";

  // === 에러 응답 예시 ===
  public static final String BAD_REQUEST_EXAMPLE =
      """
    {
      "status": "ERROR",
      "code": 400,
      "message": "요청 데이터가 올바르지 않습니다.",
      "data": null,
      "error": {
        "code": "VALIDATION_ERROR",
        "message": "결제 금액이 유효하지 않습니다.",
        "exception": "PaymentValidationException",
        "field": "payTotal",
        "trace": null
      },
      "timestamp": "2025-08-31T07:56:33.274Z"
    }
    """;

  public static final String FORBIDDEN_EXAMPLE =
      """
    {
      "status": "ERROR",
      "code": 403,
      "message": "요청을 처리할 권한이 없습니다.",
      "data": null,
      "error": {
        "code": "PERMISSION_DENIED",
        "message": "결제 권한이 없습니다.",
        "exception": "PaymentAuthorizationException",
        "field": null,
        "trace": null
      },
      "timestamp": "2025-08-31T07:56:33.274Z"
    }
    """;

  public static final String NOT_FOUND_EXAMPLE =
      """
    {
      "status": "ERROR",
      "code": 404,
      "message": "요청한 리소스를 찾을 수 없습니다.",
      "data": null,
      "error": {
        "code": "RESOURCE_NOT_FOUND",
        "message": "결제 정보를 찾을 수 없습니다.",
        "exception": "PaymentNotFoundException",
        "field": "merchantUid",
        "trace": null
      },
      "timestamp": "2025-08-31T07:56:33.274Z"
    }
    """;

  public static final String CONFLICT_EXAMPLE =
      """
    {
      "status": "ERROR",
      "code": 409,
      "message": "요청이 현재 상태와 충돌합니다.",
      "data": null,
      "error": {
        "code": "PAYMENT_CONFLICT",
        "message": "이미 처리된 결제입니다.",
        "exception": "PaymentConflictException",
        "field": null,
        "trace": null
      },
      "timestamp": "2025-08-31T07:56:33.274Z"
    }
    """;

  public static final String SERVER_ERROR_EXAMPLE =
      """
    {
      "status": "ERROR",
      "code": 500,
      "message": "서버 내부 오류가 발생했습니다.",
      "data": null,
      "error": {
        "code": "INTERNAL_SERVER_ERROR",
        "message": "결제 처리 중 예상치 못한 오류가 발생했습니다.",
        "exception": "PaymentProcessingException",
        "field": null,
        "trace": null
      },
      "timestamp": "2025-08-31T07:56:33.274Z"
    }
    """;
}
