#!/bin/sh

export SERVER_PORT=4550

# Database
export PCQ_DB_NAME=pcq
export PCQ_DB_HOST=0.0.0.0
export PCQ_DB_PORT=5052
export PCQ_DB_USERNAME=pcquser
export PCQ_DB_PASSWORD=pcqpass

export FLYWAY_URL=jdbc:postgresql://0.0.0.0:5052/pcq
export FLYWAY_USER=pcquser
export FLYWAY_PASSWORD=pcqpass
export FLYWAY_NOOP_STRATEGY=false

export JWT_SECRET=JwtSecretKey

export DB_ENCRYPTION_KEY=ThisIsATestKeyForEncryption
