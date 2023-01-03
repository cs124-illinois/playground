FROM openjdk:17-slim

ENV KOTLIN_ROOT /opt/kotlinc
ENV KOTLIN_VERSION 1.8.0
ENV PATH $PATH:$KOTLIN_ROOT/bin

RUN apt update \
  && apt install -y wget unzip \
  && wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -O /tmp/kotlin.zip \
	&& mkdir -p /opt \
	&& unzip /tmp/kotlin.zip -d /opt \
	&& rm -v /tmp/kotlin.zip \
	&& java -version \
	&& kotlin -version

COPY run.sh what.sh /
WORKDIR /playground
CMD ["/run.sh"]
