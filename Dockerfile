# open jdk 17 버전의 환경을 구성
FROM openjdk:17-alpine

# 빌드 인수 정의
ARG JAR_FILE=build/libs/*.jar
ARG PROFILES
ARG ENV

# JAR 파일 복사
COPY ${JAR_FILE} app.jar

# 컨테이너 시작 명령 설정
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "app.jar"]
