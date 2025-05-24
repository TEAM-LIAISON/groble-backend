# 🔧 Groble API 리팩토링 가이드

## 📌 현재 상황 요약
- 13개의 컨트롤러, 각각의 mapper 클래스
- 엔드포인트별 커스텀 Swagger 어노테이션
- 복잡한 Request/Response DTO 구조

## 🎯 목표
- API 명세 중심 개발로 전환
- 코드 생성을 통한 보일러플레이트 감소
- 유지보수성 향상

## 🚀 단계별 실행 계획

### Phase 1: API Specification First (1-2주)
1. **OpenAPI 3.0 명세 작성**
   - 기존 Swagger 어노테이션을 YAML로 이관
   - API별로 별도 파일로 분리 (user-api.yaml, order-api.yaml 등)
   
2. **OpenAPI Generator 도입**
   - Controller Interface 자동 생성
   - Request/Response DTO 자동 생성
   - 기존 커스텀 어노테이션 제거

3. **Delegate 패턴 구현**
   ```java
   @Service
   public class UserApiDelegateImpl implements UserApiDelegate {
       // 실제 비즈니스 로직만 구현
   }
   ```

### Phase 2: DTO 최적화 (1주)
1. **MapStruct 도입**
   - 수동 mapper 클래스 제거
   - 컴파일 타임 매핑 코드 생성
   
2. **DTO 통합**
   - 중복 DTO 제거
   - 공통 Base DTO 활용

### Phase 3: GraphQL BFF 도입 (선택적, 2-3주)
1. **Netflix DGS 설정**
   - 모바일/웹 클라이언트별 최적화된 API 제공
   - Over-fetching/Under-fetching 문제 해결
   
2. **Schema First Development**
   - GraphQL Schema 정의
   - Type-safe resolver 자동 생성

### Phase 4: API Gateway 도입 (선택적, 2주)
1. **Spring Cloud Gateway 설정**
   - 인증/인가 중앙화
   - Rate Limiting
   - Circuit Breaker
   
2. **서비스 분리**
   - 도메인별 마이크로서비스 분리
   - 독립적인 배포 가능

## 📊 예상 효과
- **코드량 감소**: 약 40-50% 감소 예상
- **개발 속도**: API 추가 시 2-3배 빠른 개발
- **유지보수성**: 명세와 코드의 일치성 보장
- **테스트**: 자동 생성된 Mock 서버로 프론트엔드 독립 개발

## 🛠️ 필요 기술 스택
- OpenAPI Generator 7.3.0+
- MapStruct 1.5.5+
- Netflix DGS 8.1.1+ (선택)
- Spring Cloud Gateway 4.1.0+ (선택)

## 📝 마이그레이션 체크리스트
- [ ] OpenAPI 명세 작성 완료
- [ ] OpenAPI Generator 설정
- [ ] 첫 번째 컨트롤러 마이그레이션
- [ ] MapStruct 설정 및 매퍼 생성
- [ ] 기존 커스텀 어노테이션 제거
- [ ] 통합 테스트 작성
- [ ] 문서화 업데이트

## 💡 Best Practices
1. **명세 우선**: 코드 작성 전 API 명세부터 리뷰
2. **버전 관리**: API 명세도 Git으로 버전 관리
3. **CI/CD 통합**: API 명세 변경 시 자동 코드 생성
4. **클라이언트 SDK**: OpenAPI Generator로 클라이언트 SDK 자동 생성

## 🔗 참고 자료
- [OpenAPI Generator Documentation](https://openapi-generator.tech/)
- [Netflix DGS Framework](https://netflix.github.io/dgs/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [MapStruct](https://mapstruct.org/)
