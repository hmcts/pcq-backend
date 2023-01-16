ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-17-distroless-1.5.2

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/pcq-backend.jar /opt/app/

EXPOSE 4550
CMD [ "pcq-backend.jar" ]
