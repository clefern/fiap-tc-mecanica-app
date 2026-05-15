FROM eclipse-temurin:21-jre-alpine

ARG OTEL_AGENT_VERSION=2.26.1

WORKDIR /app

COPY ./target/*.jar app.jar

RUN apk add --no-cache curl \
    && curl -sSfL \
      "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" \
      -o otel.jar

CMD [ "java", "-javaagent:/app/otel.jar", "-jar", "app.jar" ]
