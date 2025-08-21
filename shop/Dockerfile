FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 build.gradle과 settings.gradle 먼저 복사
COPY build.gradle settings.gradle* ./

# gradle 디렉토리가 있으면 복사
COPY gradle/ gradle/

# 의존성 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN gradle clean build -x test --no-daemon

# 2️ Production Stage - JRE 실행
FROM eclipse-temurin:17-jre-alpine

# 환경 변수 설정
ENV TZ=Asia/Seoul

# 보안 패치 및 필수 패키지 설치
RUN apk update && \
    apk upgrade && \
    apk add --no-cache \
        curl \
        tzdata && \
    rm -rf /var/cache/apk/* /tmp/*

# 애플리케이션 사용자 생성
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# 작업 디렉토리 생성 및 권한 설정
WORKDIR /app
RUN chown appuser:appuser /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appuser app.jar

# 사용자 변경
USER appuser

# 포트 노출
EXPOSE 8006

# 애플리케이션 실행 (환경변수를 런타임에 적용)
ENTRYPOINT ["sh", "-c", "java -Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom -jar app.jar"]