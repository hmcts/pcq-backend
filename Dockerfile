ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

# Application image

FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/pcq-backend.jar /opt/app/

EXPOSE 4550
CMD [ "pcq-backend.jar" ]
