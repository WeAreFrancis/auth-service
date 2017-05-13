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
ADD target/auth-service.jar /app/auth-service.jar
ENTRYPOINT mkdir -p /var/lib/pgsql/9.6/data/$DB_NAME \
    && chown -R postgres:postgres /var/lib/pgsql/9.6/data/$DB_NAME \
    && (su postgres -c "/usr/pgsql-9.6/bin/initdb -D /var/lib/pgsql/9.6/data/$DB_NAME --auth-host=md5 \
    && /usr/pgsql-9.6/bin/pg_ctl -D /var/lib/pgsql/9.6/data/$DB_NAME start -w \
    && psql -c \"CREATE USER $POSTGRES_USER WITH SUPERUSER PASSWORD '$POSTGRES_PASSWORD';\" \
    && createdb -O $POSTGRES_USER $DB_NAME") \
    && java -jar /app/auth-service.jar
