FROM gradle:7.4.0-jdk17 AS build
WORKDIR /home/gradle
COPY ./ /home/gradle
RUN ["gradle", "build", "-x", "test"]

FROM openjdk:17
EXPOSE 8080
RUN ["mkdir", "/app"]
COPY --from=build /home/gradle/build/libs/*-all.jar /app/app.jar
ENTRYPOINT ["java","-Xms768M", "-Xmx768M", "-jar", "/app/app.jar"]
