# groble-mapping 모듈

MapStruct 기반의 객체 매핑을 담당하는 모듈입니다.

## 개요

이 모듈은 프로젝트 전체에서 사용되는 DTO 변환 로직을 중앙화하여 관리합니다. 
MapStruct를 사용하여 컴파일 타임에 매핑 코드를 자동 생성하므로, 런타임 오버헤드가 없고 타입 안전성이 보장됩니다.

## 구조

```
groble-mapping/
├── src/main/java/liaison/groble/mapping/
│   ├── config/
│   │   └── GrobleMapperConfig.java    # MapStruct 공통 설정
│   ├── auth/
│   │   └── AuthMapper.java            # 인증 관련 매핑
│   ├── content/
│   │   ├── ContentMapper.java         # 콘텐츠 매핑
│   │   └── ContentOptionMapper.java   # 콘텐츠 옵션 매핑
│   └── user/
│       └── UserMapper.java            # 사용자 관련 매핑
└── build/generated/                   # MapStruct가 생성한 구현체
```

## 사용 방법

### 1. Mapper 인터페이스 정의

```java
@Mapper(config = GrobleMapperConfig.class)
public interface AuthMapper {
    
    SignUpDto toSignUpDto(SignUpRequest request);
    
    @Mapping(target = "termsTypeStrings", 
             expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
    SignUpDto toSignUpDtoWithMapping(SignUpRequest request);
}
```

### 2. 컨트롤러에서 사용

```java
@RestController
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final AuthMapper authMapper;  // MapStruct가 생성한 구현체 주입
    
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        // 자동 매핑
        SignUpDto dto = authMapper.toSignUpDto(request);
        
        // 서비스 호출
        TokenDto tokenDto = authService.signUp(dto);
        
        return ResponseEntity.ok(SignUpResponse.of(request.getEmail()));
    }
}
```

### 3. 복잡한 매핑 처리

#### @Named 메서드 사용
```java
@Mapper(config = GrobleMapperConfig.class)
public interface ContentMapper {
    
    @Mapping(target = "priceOptionLength", qualifiedByName = "calculatePriceLength")
    ContentResponse toResponse(ContentDto dto);
    
    @Named("calculatePriceLength")
    default int calculatePriceLength(ContentDto dto) {
        return (int) dto.getOptions().stream()
            .filter(opt -> opt.getPrice() != null)
            .count();
    }
}
```

#### Decorator 패턴 사용
```java
@DecoratedWith(ContentMapperDecorator.class)
@Mapper(config = GrobleMapperConfig.class)
public interface ContentMapper {
    // 기본 매핑 정의
}

public abstract class ContentMapperDecorator implements ContentMapper {
    @Autowired
    @Qualifier("delegate")
    private ContentMapper delegate;
    
    @Override
    public ContentDto toContentDto(ContentRequest request) {
        ContentDto dto = delegate.toContentDto(request);
        // 추가 로직
        return dto;
    }
}
```

## 주요 기능

### 1. 자동 매핑
- 동일한 이름과 타입의 필드는 자동으로 매핑
- 중첩된 객체도 자동으로 처리

### 2. 커스텀 매핑
- `@Mapping` 어노테이션으로 필드별 매핑 규칙 지정
- expression으로 복잡한 변환 로직 작성
- qualifiedByName으로 커스텀 메서드 지정

### 3. null 처리
- 설정에 따라 null 값 무시 또는 매핑
- NullValueCheckStrategy로 null 체크 방식 지정

### 4. 컬렉션 매핑
- List, Set 등 컬렉션 자동 변환
- 요소별 매핑 규칙 적용

## 성능 이점

1. **컴파일 타임 코드 생성**: 리플렉션 없이 순수 Java 코드로 동작
2. **타입 안전성**: 컴파일 시점에 매핑 오류 감지
3. **디버깅 용이**: 생성된 코드를 직접 확인 가능

## 마이그레이션 가이드

### 기존 수동 Mapper → MapStruct

1. **기존 코드 (수동)**
```java
@Component
public class AuthDtoMapper {
    public SignUpDto toServiceSignUpDto(SignUpRequest request) {
        return SignUpDto.builder()
            .userType(request.getUserType())
            .termsTypeStrings(request.getTermsTypes().stream().map(Enum::name).toList())
            .email(request.getEmail())
            .password(request.getPassword())
            .nickname(request.getNickname())
            .phoneNumber(request.getPhoneNumber())
            .build();
    }
}
```

2. **MapStruct 변환**
```java
@Mapper(config = GrobleMapperConfig.class)
public interface AuthMapper {
    @Mapping(target = "termsTypeStrings", 
             expression = "java(request.getTermsTypes().stream().map(Enum::name).toList())")
    SignUpDto toSignUpDto(SignUpRequest request);
}
```

## 주의사항

1. **unmappedTargetPolicy = ERROR**: 매핑되지 않은 target 필드가 있으면 컴파일 에러
2. **Lombok과 함께 사용**: lombok-mapstruct-binding 의존성 필요
3. **생성된 코드 위치**: `build/generated/sources/annotationProcessor/`

## 확장 가이드

새로운 Mapper 추가 시:

1. 해당 도메인 패키지에 Mapper 인터페이스 생성
2. `@Mapper(config = GrobleMapperConfig.class)` 적용
3. 매핑 메서드 정의
4. 필요시 커스텀 매핑 규칙 추가
5. 테스트 작성

## 빌드 및 테스트

```bash
# 빌드
./gradlew :groble-mapping:build

# 생성된 코드 확인
ls -la groble-mapping/build/generated/sources/annotationProcessor/java/main/

# 테스트
./gradlew :groble-mapping:test
```
