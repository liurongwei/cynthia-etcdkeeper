FROM openjdk:8-jdk-alpine
MAINTAINER liuronngwei1981@vip.qq.com

VOLUME /tmp
RUN apk update && apk add libc6-compat

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.cynthia.etcdkeeper.EtcdkeeperApplication"]
