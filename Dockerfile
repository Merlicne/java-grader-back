FROM gradle:jdk23-alpine

WORKDIR /app

COPY gradle /app/gradle
COPY build.gradle /app
COPY settings.gradle /app
COPY src /app/src

RUN gradle build

CMD ["gradle", "bootRun"]

