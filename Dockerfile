FROM gcr.io/distroless/java21-debian13:nonroot

WORKDIR /app

COPY --chown=nonroot:nonroot target/mecanica*.jar /app/app.jar

CMD [ "app.jar" ]
