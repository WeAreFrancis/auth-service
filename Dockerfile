FROM centos:latest
MAINTAINER Guillaume Leroy <pro.guillaume.leroy@gmail.com>

ENV DB_HOST localhost
ENV DB_NAME auth
ENV POSTGRES_USER auth
ENV POSTGRES_PASSWORD auth

RUN rpm -Uvh https://yum.postgresql.org/9.6/redhat/rhel-7-x86_64/pgdg-centos96-9.6-3.noarch.rpm
RUN yum update -y
RUN yum install -y postgresql96 postgresql96-server postgresql96-contrib java-1.8.0-openjdk

VOLUME ["/etc/pgsql", "/var/lib/pgsql"]

USER root
ADD entrypoint.sh /entrypoint.sh
ADD target/auth-service.jar /app/auth-service.jar
RUN chmod +x /entrypoint.sh
ENTRYPOINT /entrypoint.sh
