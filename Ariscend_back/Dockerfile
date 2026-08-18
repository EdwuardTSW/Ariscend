FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system ariscend \
    && useradd --system --gid ariscend --home-dir /app ariscend

WORKDIR /app

COPY --from=build --chown=ariscend:ariscend /workspace/build/libs/*.jar app.jar

USER ariscend

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx280m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-8080}"]
