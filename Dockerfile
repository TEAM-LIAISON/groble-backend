# open jdk 17 버전의 환경을 구성
FROM openjdk:17-alpine

# 빌드 인수 정의 - JAR_FILE의 기본 경로를 서브모듈의 빌드 산출물 경로로 설정
ARG JAR_FILE=groble-api/groble-api-server/build/libs/*.jar
ARG PROFILES
ARG ENV

# 지정한 경로의 JAR 파일을 컨테이너 내 app.jar로 복사
COPY ${JAR_FILE} app.jar

# 컨테이너 시작 명령 설정
ENTRYPOINT ["java",
  "-Dcom.amazonaws.sdk.disableMetricAdminMBeanRegistration=true",
  "-Dcom.amazonaws.sdk.disableMetrics=true",
  "-Dspring.profiles.active=${PROFILES}",
  "-Dserver.env=${ENV}",
  "-jar", "app.jar"]
