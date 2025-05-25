# OpenAPI Generator 마이그레이션 가이드

## 개요
이 문서는 Groble 프로젝트의 API를 OpenAPI Generator 기반으로 마이그레이션하는 방법을 설명합니다.

## 현재 상태
- OpenAPI Generator는 단계적 마이그레이션을 위해 임시로 비활성화되어 있습니다.
- NotificationController가 첫 번째 마이그레이션 대상으로 준비되었습니다.

## 마이그레이션 단계

### 1단계: OpenAPI 명세서 작성
각 API에 대한 OpenAPI 3.0 명세서를 작성합니다.
- 위치: `groble-api/groble-api-spec/openapi/`
- 파일명 규칙: `{domain}-api.yaml`
- 예시: `notification-api.yaml`, `user-api.yaml`, `order-api.yaml`

### 2단계: build.gradle 설정 활성화
`groble-api/groble-api-spec/build.gradle`에서 주석 처리된 OpenAPI Generator 설정을 활성화합니다.

```gradle
plugins {
    id 'java'
    id 'org.openapi.generator' version '7.3.0'  // 주석 해제
}

// 주석 처리된 설정들을 활성화
```

### 3단계: Controller 리팩토링
기존 Controller를 인터페이스 기반으로 변경합니다.

#### Before (기존 방식):
```java
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    // 직접 구현
}
```

#### After (OpenAPI Generator 방식):
```java
@RestController
public class NotificationApiController implements NotificationApiDelegate {
    // 생성된 인터페이스 구현
}
```

### 4단계: 생성된 코드 활용
```bash
# API 인터페이스 및 모델 생성
./gradlew :groble-api:groble-api-spec:generateAllApis

# 특정 API만 생성
./gradlew :groble-api:groble-api-spec:generateNotificationApi
```

## NotificationController 마이그레이션 예시

### 1. OpenAPI 명세서 (`notification-api.yaml`)
- 작성 완료: `/groble-api/groble-api-spec/openapi/notification-api.yaml`

### 2. 임시 인터페이스 생성
- `NotificationApi` 인터페이스 작성 (OpenAPI Generator가 생성할 형태)
- `NotificationApiController` 구현체 작성

### 3. 기존 Controller deprecated 처리
- 기존 `NotificationController`에 `@Deprecated` 추가
- 마이그레이션 완료 후 삭제 예정

## 장점
1. **API 문서화 자동화**: 코드와 문서의 일관성 보장
2. **타입 안정성**: 클라이언트 코드 자동 생성 가능
3. **표준화**: OpenAPI 3.0 표준 준수
4. **버전 관리**: API 버전 관리 체계화
5. **개발 생산성**: 보일러플레이트 코드 자동 생성

## 주의사항
1. `@Auth Accessor` 같은 커스텀 애노테이션은 수동으로 추가 필요
2. 생성된 코드는 수정하지 않고 상속/구현으로 확장
3. 공통 스키마는 `common-schemas.yaml`에 정의

## 다음 단계
1. NotificationController 마이그레이션 완료 후 테스트
2. 다른 Controller들도 순차적으로 마이그레이션
3. 클라이언트 SDK 자동 생성 설정 추가
4. API 버전 관리 전략 수립

## 참고 자료
- [OpenAPI Generator Documentation](https://openapi-generator.tech/docs/generators/spring/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
- [Spring Boot Integration Guide](https://openapi-generator.tech/docs/integrations/spring/)
