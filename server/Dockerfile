FROM openjdk:16-jdk-slim
WORKDIR /
COPY *.jar playground.jar
CMD [ "java", "-ea", "--enable-preview", "-XX:+ExitOnOutOfMemoryError", "--illegal-access=permit", "-jar", "playground.jar" ]
