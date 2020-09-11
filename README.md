# PCQ Backend Application

[![Build Status](https://travis-ci.org/hmcts/pcq-backend.svg?branch=master)](https://travis-ci.org/hmcts/pcq-backend)

## Purpose

This is the Protected Characteristics Back-End application that will save user's answers to the database, fetch PCQ Ids that don't have an associated case record and add case information to a PCQ record in the database. The API will be invoked by two components - PCQ front-end and the Consolidation service

## What's inside

The application exposes health endpoint (http://localhost:4550/health).

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
If another service is using the pcq-backend application, the emnvironment values are available through a batch script:
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
Finally you can remove all volumes - not this rmoves all existing database values:
```bash
  docker volume rm $(docker volume ls -q)
```

There is no need to remove postgres and java or similar core images.


## Hystrix

[Hystrix](https://github.com/Netflix/Hystrix/wiki) is a library that helps you control the interactions
between your application and other services by adding latency tolerance and fault tolerance logic. It does this
by isolating points of access between the services, stopping cascading failures across them,
and providing fallback options. We recommend you to use Hystrix in your application if it calls any services.

### Hystrix circuit breaker

This API has [Hystrix Circuit Breaker](https://github.com/Netflix/Hystrix/wiki/How-it-Works#circuit-breaker)
already enabled. It monitors and manages all the`@HystrixCommand` or `HystrixObservableCommand` annotated methods
inside `@Component` or `@Service` annotated classes.

### Other

Hystrix offers much more than Circuit Breaker pattern implementation or command monitoring.
Here are some other functionalities it provides:
 * [Separate, per-dependency thread pools](https://github.com/Netflix/Hystrix/wiki/How-it-Works#isolation)
 * [Semaphores](https://github.com/Netflix/Hystrix/wiki/How-it-Works#semaphores), which you can use to limit
 the number of concurrent calls to any given dependency
 * [Request caching](https://github.com/Netflix/Hystrix/wiki/How-it-Works#request-caching), allowing
 different code paths to execute Hystrix Commands without worrying about duplicating work

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
