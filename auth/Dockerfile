FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위해 build.gradle과 settings.gradle 먼저 복사
COPY build.gradle settings.gradle* ./
COPY gradle/ gradle/ 2>/dev/null || true

# 의존성 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN gradle clean build -x test --no-daemon

# 2단계: Runtime Stage - JRE 실행
FROM openjdk:17-jre-slim

# 환경 변수 설정
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom" \
    SPRING_PROFILES_ACTIVE=prod

# 보안 패치 및 필수 패키지 설치
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
        curl \
        tzdata && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# 애플리케이션 사용자 생성 (보안 강화)
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 작업 디렉토리 생성 및 권한 설정
WORKDIR /app
RUN chown appuser:appuser /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appuser app.jar

# 사용자 변경
USER appuser

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8002/actuator/health || exit 1

# 포트 노출
EXPOSE 8002

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]