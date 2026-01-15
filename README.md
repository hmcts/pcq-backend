# PCQ Backend Application

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Apcq-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Apcq-backend) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Apcq-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Apcq-backend) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Apcq-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Apcq-backend) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Apcq-backend&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Apcq-backend) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Apcq-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Apcq-backend)

This is the backend for the protected characteristics questionnaire service. This service will ask a set of questions that will help us check we are treating people fairly and equally. It helps us to meet our commitment to equality (under the Equality Act 2010).

## Overview

<p align="center">
<a href="https://github.com/hmcts/pcq-frontend">pcq-frontend</a> • <b><a href="https://github.com/hmcts/pcq-backend">pcq-backend</a></b> • <a href="https://github.com/hmcts/pcq-consolidation-service">pcq-consolidation-service</a> • <a href="https://github.com/hmcts/pcq-shared-infrastructure">pcq-shared-infrastructure</a> • <a href="https://github.com/hmcts/pcq-loader">pcq-loader</a>
</p>

<br>

<p align="center">
  <img src="https://raw.githubusercontent.com/hmcts/pcq-frontend/master/pcq_overview.png" width="500"/>
  <br>
  <sub>System overview diagram maintained in the pcq-frontend repository.</sub>
</p>

## Purpose

This is the Protected Characteristics backend application that will save user's answers to the database, fetch PCQ Ids that don't have an associated case record and add case information to a PCQ record in the database. The API will be invoked by two components - PCQ front-end and the Consolidation service.

## What's inside

The application exposes few endpoints
1. Submit answers (/pcq/backend/submitAnswers).
2. Get answer for PCQ Id (/pcq/backend/getAnswer/{pcqId}).
3. Get PCQ Ids without CaseId (/pcq/backend/consolidation/pcqRecordWithoutCase).
4. Add CaseId for PCQ Record (/pcq/backend/consolidation/addCaseForPCQ/{pcqId}).


## Authorisation

The service uses two authentication mechanisms, depending on the endpoint:

1. JWT (for submitAnswers)
   - Endpoint: `/pcq/backend/submitAnswers`
   - Header: `Authorization: Bearer <jwt>`
   - Secret: `security.jwt.secret` (env: `JWT_SECRET`)
   - The JWT subject is used as the party id; `authorities` claim is mapped to Spring authorities.

2. Service-to-service (for SAS token generation)
   - Endpoint: `/pcq/backend/token/bulkscan`
   - Header: `ServiceAuthorization: <s2s token>`
   - Token is validated via the IdAM s2s-auth service (`idam.s2s-auth.url`).
   - Allowed services are configured in `authorised.services`.

Other endpoints are currently permitted without authentication by the HTTP security filter chain.

## Disposal job

PCQ disposal is a batch job that runs on application start when `PCQ_DISPOSER_JOB=true`.
It is additionally gated by `disposer.enabled` (env: `PCQ_DISPOSER_ENABLED`).

Behavior and configuration:
- `disposer.dry-run` (env: `PCQ_DISPOSER_DRY_RUN`) logs candidate PCQ ids without deleting when true.
- `disposer.rateLimit` (env: `PCQ_DISPOSER_RATE_LIMIT`) caps the number of records fetched per category.
- `disposer.keep-with-case` (env: `PCQ_DAYS_TO_KEEP_WITH_CASEID`) keeps records with case ids for N days.
- `disposer.keep-no-case` (env: `PCQ_DAYS_TO_KEEP_WITHOUT_CASEID`) keeps records without case ids for N days.

The job deletes records in batches of 100 when not in dry-run mode.

## Plugins

The application uses the following plugins:

  * checkstyle https://docs.gradle.org/current/userguide/checkstyle_plugin.html
  * pmd https://docs.gradle.org/current/userguide/pmd_plugin.html
  * jacoco https://docs.gradle.org/current/userguide/jacoco_plugin.html
  * io.spring.dependency-management https://github.com/spring-gradle-plugins/dependency-management-plugin
  * org.springframework.boot http://projects.spring.io/spring-boot/
  * org.owasp.dependencycheck https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html
  * com.github.ben-manes.versions https://github.com/ben-manes/gradle-versions-plugin

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:
```bash
  ./gradlew build
```

### Running the application

To run the PostgreSQL 11 PCQ database, execute docker-compose to build the database from the postgreSQL docker image:
```
  docker-compose -f docker-compose.yml up pcq-database
```

To run the PCQ Backend application, execute the gradle run command - this also adds the PCQ schema to the database.
```
  ./gradlew run
```

Finally to use the pcq-backend application, you'll need to add the encryption package to PostgreSQL.
Using your favourite SQL client run the following from the PCQ database. See .env file for dev database password.
 ```
  psql -h 0.0.0.0 -p 5050 -d pcq -u pcquser
  CREATE EXTENSION IF NOT EXISTS pgcrypto;
 ```

#### Environment variables

The `.env` file has a list of the environment variables in use by the pcq-backend and pcq-database components. These are as follows:
* PCQ_DB_NAME
* PCQ_DB_HOST
* PCQ_DB_PORT
* PCQ_DB_USERNAME
* PCQ_DB_PASSWORD
* FLYWAY_URL
* FLYWAY_USER
* FLYWAY_PASSWORD
* FLYWAY_NOOP_STRATEGY
* JWT_SECRET
* DB_ENCRYPTION_KEY

There is no need to export these values if pcq-backend repo is checked out.
If another service is using the pcq-backend application, the environment values are available through a batch script:
```bash
  source ./bin/set-pcq-docker-env.sh
```

#### Manually building the PCQ Database using Flyway

The pcq-backend application will automatically create the PCQ database definitions from the flyway scripts included in the build.
However if you would like to add the table definitions manually you'll need to execute the gradle migrate script:
```
  ./gradlew -Pflyway.user=pcquser -Pflyway.password=pcqpass -Pflyway.url=jdbc:postgresql://0.0.0.0:5050/pcq flywayMigrate
```

## Running the application in Docker

### Build docker image

Create docker image:
```bash
  docker-compose build
```

Bring the database and pcq-backend application up in Docker.
by executing the following command:
```bash
  docker-compose up
```

az login
az acr login --name hmctspublic --subscription DCD-CNP-Prod

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:
```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:
```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Notes:

### Accessing Azure Container Repository

Run the following to install the azure and kubernetes command-line tools
```bash
  brew install azure-cli
  az acs kubernetes install-cli
```
login to azure - you'll not need hmctsprivate access for PCQ.
```bash
  az login (will open a browser to login)
  az acr login --name hmctspublic --subscription DCD-CNP-Prod
  az acr login --name hmctsprivate --subscription DCD-CNP-Prod
```

#### Removing old docker images

Old containers can be removed by executing the following command:
```bash
  docker rm $(docker ps -a -q)
```
You may need to forcibly remove any relevant images by executing the following command:
```bash
  docker rmi $(docker images -q)
```
Finally you can remove all volumes - note this removes all existing database values:
```bash
  docker volume rm $(docker volume ls -q)
```

There is no need to remove postgres and java or similar core images.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
