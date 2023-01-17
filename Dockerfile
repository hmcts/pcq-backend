ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
ARG PLATFORM=""

# Application image

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/pcq-backend.jar /opt/app/

EXPOSE 4550
CMD [ "pcq-backend.jar" ]
