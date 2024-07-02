#
# Build stage
#
FROM eclipse-temurin:22-jdk-jammy AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml dependency:go-offline clean verify

#
# Package stage
#
FROM eclipse-temurin:22-jre-jammy
ARG JAR_FILE=/usr/app/target/*.jar
RUN pwd
COPY --from=build $JAR_FILE /app/runner.jar
EXPOSE 8080
WORKDIR /app
ADD src/main/resources resources
ENTRYPOINT java -Dlog4j2.configurationFile=file:resources/log4j2.properties -jar runner.jar