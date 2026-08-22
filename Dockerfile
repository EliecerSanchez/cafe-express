FROM eclipse-temurin:26-jdk-alpine AS build
WORKDIR /src
COPY src ./src
RUN javac -encoding UTF-8 -d out $(find src -name "*.java")

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
COPY --from=build /src/out ./bin
COPY static ./static
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "bin", "cafeexpress.App"]
