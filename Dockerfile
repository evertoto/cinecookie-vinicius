# Estágio 1 — build: compila e gera target/app.jar (Maven + JDK ficam só aqui)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

# Estágio 2 — final: apenas JRE + JAR
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system cinecookie \
    && useradd --system --gid cinecookie cinecookie
COPY --from=build --chown=cinecookie:cinecookie /workspace/target/app.jar ./app.jar

USER cinecookie
EXPOSE 8080
# MaxRAMPercentage: aproveita melhor a memória pequena do plano gratuito do Render.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
