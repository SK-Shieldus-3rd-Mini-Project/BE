# Dockerfile (in jeonbongjun/ folder)

# 1. Build stage: Maven을 사용하여 JAR 파일 빌드
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# pom.xml에 명시된 의존성만 먼저 다운로드 (레이어 캐싱 활용)
RUN ./mvnw dependency:go-offline -B
COPY src ./src
# 애플리케이션 빌드 (테스트는 생략)
RUN ./mvnw package -DskipTests

# 2. Production stage: JRE 환경에서 JAR 실행
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/*.jar app.jar
# 포트 노출 (application.properties와 일치해야 함)
EXPOSE 8080
# 애플리케이션 실행 (환경 변수는 GKE ConfigMap/Secret으로 주입)
ENTRYPOINT ["java", "-jar", "app.jar"]