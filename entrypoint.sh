if [ -d /var/lib/pgsql/9.6/data/$DB_NAME ]; then
  su postgres -c "/usr/pgsql-9.6/bin/pg_ctl -D /var/lib/pgsql/9.6/data/$DB_NAME start -w"
else
  mkdir -p /var/lib/pgsql/9.6/data/$DB_NAME
  chown -R postgres:postgres /var/lib/pgsql/9.6/data/$DB_NAME
  su postgres -c "/usr/pgsql-9.6/bin/initdb -D /var/lib/pgsql/9.6/data/$DB_NAME --auth-host=md5 \
      && echo \"host all  all    0.0.0.0/0  md5\" >> /var/lib/pgsql/9.6/data/$DB_NAME/pg_hba.conf \
      && /usr/pgsql-9.6/bin/pg_ctl -D /var/lib/pgsql/9.6/data/$DB_NAME start -w \
      && psql -c \"CREATE USER $POSTGRES_USER WITH SUPERUSER PASSWORD '$POSTGRES_PASSWORD';\" \
      && createdb -O $POSTGRES_USER $DB_NAME"
fi

java -jar /app/auth-service.jar
