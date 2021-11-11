FROM openjdk:16-alpine

ENV KOTLIN_ROOT /opt/kotlinc
ENV KOTLIN_VERSION 1.5.31
ENV PATH $PATH:$KOTLIN_ROOT/bin

RUN apk add --no-cache --virtual=.build-dependencies bash wget unzip \
	&& wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip -O /tmp/kotlin.zip \
	&& mkdir -p /opt \
	&& unzip /tmp/kotlin.zip -d /opt \
	&& rm -v /tmp/kotlin.zip \
	&& java -version \
	&& kotlin -version

COPY run.sh what.sh /
WORKDIR /playground
CMD ["/run.sh"]
