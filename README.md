# 📁 Groble - 메이커를 위한 콘텐츠 마켓 플랫폼

<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.10-brightgreen?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-17-brown?logo=java" alt="Java">
  <img src="https://img.shields.io/badge/Gradle-8.x-02303A?logo=gradle" alt="Gradle">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.0-DC382D?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?logo=mongodb" alt="MongoDB">
</div>

## 📚 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [아키텍처 개요](#-아키텍처-개요)
3. [주요 기능](#-주요-기능)
4. [기술 스택](#-기술-스택)
5. [모듈 구조](#-모듈-구조)
6. [로컬 개발 가이드](#-로컬-개발-가이드)
7. [테스트 & 품질 관리](#-테스트--품질-관리)
8. [코딩 컨벤션](#-코딩-컨벤션)
9. [기여 방법](#-기여-방법)

## 📌 프로젝트 소개

**Groble(그로블)**은 디지털 콘텐츠를 제작·판매하는 메이커와 이를 소비하는 이용자가 신뢰 기반으로 연결될 수 있도록 설계된 B2C 마켓 플랫폼입니다. 단순 판매를 넘어, 결제·정산·알림 등 비즈니스 운영에 필요한 기능을 원스톱으로 제공합니다.

## 🏛 아키텍처 개요

Groble은 멀티 모듈 기반의 Spring 애플리케이션으로 구성되어 있으며, 핵심 도메인은 다음과 같이 분리되어 있습니다.

- **API 레이어**: 외부에 노출되는 REST API 및 Swagger 문서 제공
- **Application 레이어**: 유스케이스에 맞는 서비스 로직, 이벤트 발행/구독, 통합 트랜잭션 처리
- **Domain 레이어**: 순수 도메인 모델, 엔티티, 도메인 서비스, 레포지토리 인터페이스 정의
- **Infrastructure 레이어**: JPA/QueryDSL 기반 persistence, 외부 연동(결제/디스코드/알림) 구현체
- **Common/Mapping/Security 레이어**: 공통 유틸, DTO 매핑, 인증·인가 전략 제공



## 🚀 주요 기능

| 영역 | 상세 기능 |
| --- | --- |
| **컨텐츠 마켓** | 콘텐츠 등록/수정/삭제, 다국어 옵션, 썸네일 및 상세 이미지 관리 |
| **주문/결제** | 회원/비회원 통합 주문 흐름, Payple 결제 연동, 결제 검증·환불, 결제 성공/환불 Discord 알림 |
| **정산** | 판매자별 정산 데이터 생성, 승인/반려, 판매 내역 집계 대시보드 |
| **알림** | 이메일, 카카오 알림톡, 웹훅을 통한 실시간 알림 전송 |
| **인증/인가** | 회원·비회원 인증, 메이커 인증(개인/사업자), 관리자 전용 API 보호 |
| **관리자 포털** | 사용자 요약 정보, 정산/대시보드, 마켓 운영 현황 모니터링 |

## 🧰 기술 스택

| 분류 | 사용 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 3.3, Spring MVC, Spring Data JPA, QueryDSL, Spring Security |
| Database | MySQL 8.0, MongoDB 7.0, Redis 7.0 |
| Build & Infra | Gradle, Docker, GitHub Actions (CI/CD), AWS RDS/EC2, CloudWatch |
| Messaging & Notification | Discord Webhook, Kakao 알림톡, 이메일 SMTP |
| 기타 | MapStruct, Lombok, Jakarta Validation, Swagger(OpenAPI 3) |

## 🗂 모듈 구조

```
groble
├── groble-api
│   ├── groble-api-model        # API 리퀘스트/리스폰스 DTO 및 Swagger 문서 어노테이션
│   └── groble-api-server       # Controller, 인터셉터, 예외 핸들러
├── groble-application          # Service, 이벤트, 비즈니스 유스케이스
├── groble-domain               # 엔티티, 도메인 이벤트, 리포지토리 인터페이스
├── groble-infrastructure
│   ├── groble-external         # Discord, 결제 모듈 등 외부 연동 어댑터
│   └── groble-persistence      # JPA/QueryDSL 기반 데이터 접근 구현체
├── groble-common               # 공통 유틸리티, 응답 포맷, 예외 정의
├── groble-mapping              # MapStruct 매핑 설정 및 공통 매퍼
└── groble-security             # 인증/인가, 사용자 컨텍스트 관리
```

## 💻 로컬 개발 가이드

### 1. 사전 준비

- JDK 17 이상
- Gradle Wrapper 사용 (`./gradlew`)
- 로컬 DB/Redis 환경 (필요 시 Docker Compose 사용)

### 2. 환경 변수

`application-local.yml`에서 다음 값을 환경에 맞게 설정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/groble
    username: root
    password: root
  data:
    mongodb:
      uri: mongodb://localhost:27017/groble

groble:
  redis:
    host: localhost
    port: 6379
discord:
  webhook:
    alert:
      payment-success:
        url: https://discord.com/api/webhooks/...
```

### 3. 실행

```bash
# 의존성 다운로드 및 전체 빌드
./gradlew clean build

# API 서버 (local profile) 실행
./gradlew :groble-api:groble-api-server:bootRun --args='--spring.profiles.active=local'
```

### 4. Swagger 문서

- `/swagger-ui/index.html`
- 프로파일별로 접근 URL이 다를 수 있으므로 `application-*.yml`을 확인하세요.

## ✅ 테스트 & 품질 관리

- 단위 테스트: `./gradlew test`
- 특정 모듈 테스트: `./gradlew :groble-application:test`
- 정적 분석: Spotless/Checkstyle (추가 예정 시 이 섹션 업데이트)
- GitHub Actions CI: PR 생성 시 빌드/테스트 자동 실행

## ✍️ 코딩 컨벤션

- **Java**: Google Java Style 기반, Spotless 적용
- **패키지 구조**: 기능(Feature) 기반 + 계층형 구조 혼합 사용
- **커밋 메시지**: `type: subject` 포맷 권장 (예: `feat: 비회원 결제 디스코드 알림 추가`)
- **브랜치 전략**: `main` - 운영, `develop` - 통합, `feature/*` - 기능 단위 개발, `refactor/*` - 리팩토링

## 🤝 기여 방법

1. 이슈 또는 작업 내용을 미리 공유해 주세요.
2. `feature/이슈번호-작업명` 브랜치를 생성합니다.
3. 작업 후 Pull Request를 생성하고, 변경 사항과 테스트 결과를 명시합니다.
4. 코드 리뷰 후 머지합니다.

---
