FROM adoptopenjdk/openjdk11

ENV SCALA_ROOT /opt/scala3
ENV SCALA_VERSION 3.1.2
ENV PATH $PATH:$SCALA_ROOT/bin

RUN apt update \
  && apt install -y wget unzip \
	&& wget https://github.com/lampepfl/dotty/releases/download/${SCALA_VERSION}/scala3-${SCALA_VERSION}.zip -O /tmp/scala.zip \
	&& mkdir -p /opt \
	&& unzip /tmp/scala.zip -d /opt \
  && mv /opt/scala3* /opt/scala3 \
	&& rm -v /tmp/scala.zip

COPY common /opt/scala3/bin/common

RUN java -version && scala -version

COPY run.sh what.sh /
WORKDIR /playground
CMD ["/run.sh"]
