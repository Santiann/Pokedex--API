# syntax=docker/dockerfile:1

# 1. Frontend (React + Vite)
FROM node:22-alpine AS web
WORKDIR /web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

# 2. Backend (Spring Boot), já com o frontend dentro do jar
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY core core
COPY cli cli
COPY api api
COPY --from=web /web/dist web/dist
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -pl api -am package -DskipTests

# 3. Imagem final: só a JRE e o jar, rodando sem root
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S pokedex && adduser -S pokedex -G pokedex
USER pokedex
WORKDIR /app
COPY --from=build /app/api/target/pokedex-api.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s \
  CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
