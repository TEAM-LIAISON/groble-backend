# Hyphen API Integration Module

하이픈(Hyphen) 펌뱅킹 API 연동 모듈입니다.

## 주요 기능

1. **계좌 실명 인증 (FCS계좌인증)**
   - 계좌번호와 신원확인번호(생년월일/사업자번호)를 통한 예금주 확인

2. **1원 송금 인증**
   - 1원 송금 요청 (tr1)
   - 1원 송금 검증 (tr2)

## API 엔드포인트

- FCS 계좌인증: `POST /hb0081000398`
- 1원송금 tr1: `POST /hb0081000378`
- 1원송금 tr2: `POST /hb0081000379`

## 설정 방법

### 1. application.yml 설정

```yaml
hyphen:
  api-url: https://api.hyphen.im
  user-id: ${HYPHEN_USER_ID}
  hkey: ${HYPHEN_HKEY}
  test-mode: true  # 운영환경에서는 false로 설정
```

### 2. 환경변수 설정

```bash
export HYPHEN_USER_ID=your-user-id
export HYPHEN_HKEY=your-hkey
```

## 사용 예시

### 계좌 실명 인증

```java
@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final HyphenApiService hyphenApiService;
    
    public Mono<Boolean> verifyAccountOwner(String bankCode, String accountNumber, String birthDate) {
        return hyphenApiService.verifyAccountOwner(bankCode, accountNumber, birthDate)
                .map(HyphenAccountVerificationResponse::isSuccess);
    }
}
```

### 1원 송금 인증

```java
@Service
@RequiredArgsConstructor
public class OneWonAuthService {
    
    private final HyphenApiService hyphenApiService;
    
    public Mono<String> requestOneWonTransfer(String bankCode, String accountNumber) {
        return hyphenApiService.requestOneWonTransfer(bankCode, accountNumber, "1", null, null)
                .map(HyphenOneWonTransferResponse::getOriSeqNo);
    }
    
    public Mono<Boolean> verifyOneWonTransfer(String oriSeqNo, String userInput, String trDate) {
        return hyphenApiService.verifyOneWonTransfer(oriSeqNo, userInput, trDate)
                .map(HyphenOneWonVerifyResponse::isSuccess);
    }
}
```

## 은행 코드

`BankCode` enum을 통해 은행 코드를 관리합니다:

```java
BankCode.SHINHAN.getCode(); // "088"
BankCode.SHINHAN.getName(); // "신한은행"
BankCode.findByCode("088"); // BankCode.SHINHAN
```

## 에러 처리

API 호출 실패 시 `HyphenApiException`이 발생합니다:

```java
hyphenApiService.verifyAccountOwner(bankCode, accountNumber, idNumber)
    .onErrorResume(HyphenApiException.class, error -> {
        log.error("Hyphen API 오류: {}", error.getErrorMessage());
        return Mono.empty();
    });
```

## 주의사항

1. **테스트 모드**: 개발/테스트 환경에서는 `test-mode: true`로 설정하여 `hyphen-gustation` 헤더를 추가합니다.
2. **타임아웃**: 1원 송금 검증은 5분 이내에 완료되어야 합니다.
3. **거래 고유번호**: 각 API 호출마다 고유한 거래번호(UUID)가 자동 생성됩니다.

## 보안

- API 키(Hkey)와 사용자 ID는 환경변수로 관리하세요.
- 실제 운영 환경에서는 반드시 `test-mode: false`로 설정하세요.
