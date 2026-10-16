FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress clean verify

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system cinecookie \
    && useradd --system --gid cinecookie cinecookie
COPY --from=build --chown=cinecookie:cinecookie /workspace/target/cinecookie-web-0.0.1-SNAPSHOT.jar ./app.jar

USER cinecookie
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
