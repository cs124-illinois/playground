FROM openjdk:17-jdk-buster

RUN apt update && \
    apt install -y --no-install-recommends software-properties-common ca-certificates gpg-agent curl bash gnupg

RUN mkdir -p /etc/apt/keyrings && ( curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg ) && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list && \
    apt update && apt install -y --no-install-recommends docker-ce=5:20.10.17~3-0~debian-buster docker-ce-cli=5:20.10.17~3-0~debian-buster

WORKDIR /
COPY *.jar playground.jar
CMD [ "java", "-ea", "--enable-preview", "-XX:+ExitOnOutOfMemoryError", "-jar", "playground.jar" ]

# vim: tw=0
