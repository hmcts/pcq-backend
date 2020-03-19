FROM postgres:11.7-alpine

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d ${PCQ_DB_NAME} -U ${PCQ_DB_USERNAME}

EXPOSE 5432
