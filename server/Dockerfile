FROM openjdk:16-jdk-slim
WORKDIR /
COPY *.jar playground.jar
CMD [ "java", "-ea", "--enable-preview", "-Xms512m", "-Xmx1G", "-Xss256k", "-XX:+ExitOnOutOfMemoryError", "--illegal-access=permit", "-jar", "playground.jar" ]
