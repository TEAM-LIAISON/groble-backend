# OpenAPI 명세서 가이드

## 디렉토리 구조
```
openapi/
├── common-schemas.yaml     # 공통 스키마 정의
├── notification-api.yaml   # 알림 API 명세서
├── user-api.yaml          # 사용자 API 명세서
└── order-api.yaml         # 주문 API 명세서
```

## 참조 규칙

### 1. 같은 디렉토리 내 파일 참조
```yaml
# 올바른 예시 (같은 디렉토리)
$ref: './common-schemas.yaml#/components/schemas/GrobleResponse'

# 잘못된 예시
$ref: '../common-schemas.yaml#/components/schemas/GrobleResponse'
```

### 2. 같은 파일 내 참조
```yaml
# 올바른 예시
$ref: '#/components/schemas/NotificationItem'
```

### 3. 공통 응답 참조
```yaml
responses:
  '401':
    $ref: './common-schemas.yaml#/components/responses/Unauthorized'
  '404':
    $ref: './common-schemas.yaml#/components/responses/NotFound'
  '500':
    $ref: './common-schemas.yaml#/components/responses/InternalServerError'
```

## GrobleResponse 구조

프로젝트의 모든 API는 GrobleResponse 형식을 따릅니다:

```yaml
GrobleResponse:
  type: object
  properties:
    status:
      type: string
      enum: [SUCCESS, ERROR, FAIL]
    code:
      type: integer
    message:
      type: string
    data:
      type: object
      nullable: true
    error:
      $ref: '#/components/schemas/ErrorDetail'
      nullable: true
    timestamp:
      type: string
      format: date-time
```

### 성공 응답 예시
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    // 실제 데이터
  },
  "timestamp": "2025-05-06 04:26:26"
}
```

### 에러 응답 예시
```json
{
  "status": "ERROR",
  "code": 404,
  "message": "리소스를 찾을 수 없습니다.",
  "error": {
    "exception": "NotFoundException",
    "message": "Notification not found"
  },
  "timestamp": "2025-05-06 04:26:26"
}
```

## 커스텀 응답 정의

각 API의 응답은 GrobleResponse를 확장하여 정의합니다:

```yaml
# 데이터가 없는 응답
VoidResponse:
  allOf:
    - $ref: '#/components/schemas/GrobleResponse'
    - type: object
      properties:
        data:
          type: object
          nullable: true
          example: null

# 특정 데이터를 포함한 응답
NotificationItemsResponse:
  allOf:
    - $ref: '#/components/schemas/GrobleResponse'
    - type: object
      properties:
        data:
          $ref: '#/components/schemas/NotificationItems'
```

## 검증 방법

### 1. IntelliJ IDEA에서 검증
- OpenAPI 플러그인 설치
- YAML 파일에서 참조 오류 확인

### 2. Swagger Editor에서 검증
```bash
# 로컬에서 Swagger Editor 실행
docker run -p 8081:8080 swaggerapi/swagger-editor
```

### 3. OpenAPI Generator로 코드 생성 테스트
```bash
# 단일 API 생성
./gradlew :groble-api:groble-api-spec:generateNotificationApi

# 모든 API 생성
./gradlew :groble-api:groble-api-spec:generateAllApis
```

## 주의사항

1. **상대 경로 사용**: 같은 디렉토리의 파일은 `./` 접두사 사용
2. **스키마 명명**: PascalCase 사용 (예: NotificationItem)
3. **operationId**: camelCase 사용 (예: getNotifications)
4. **태그**: API 그룹핑을 위해 일관된 태그 사용
5. **보안**: 인증이 필요한 엔드포인트는 `security` 섹션 추가

## 문제 해결

### "미해결 참조" 오류
- 파일 경로가 올바른지 확인
- 참조하는 스키마/응답이 실제로 존재하는지 확인
- 상대 경로 문법 확인 (`./` vs `../`)

### "Duplicate mapping key" 오류
- 같은 이름의 스키마가 중복 정의되지 않았는지 확인
- YAML 들여쓰기 확인

### "Invalid type" 오류
- OpenAPI 3.0 지원 타입 확인
- format 속성이 올바른지 확인 (예: date-time, int64)
