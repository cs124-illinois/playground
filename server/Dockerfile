FROM openjdk:17-jdk-buster

RUN apt update && \
    apt install -y --no-install-recommends software-properties-common ca-certificates gpg-agent ssh curl bash gnupg
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
  add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian buster stable" && \
  apt update && apt install -y --no-install-recommends docker-ce=5:19.03.2~3-0~debian-buster docker-ce-cli=5:19.03.2~3-0~debian-buster

WORKDIR /
COPY *.jar playground.jar
CMD [ "java", "-ea", "--enable-preview", "-XX:+ExitOnOutOfMemoryError", "-jar", "playground.jar" ]
