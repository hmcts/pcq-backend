#!/bin/bash
## Usage: ./idam-service-token.sh [microservice_name]
##
## Options:
##    - microservice_name: Name of the microservice. Default to `bulk_scan_processor`.
##
## Returns a valid IDAM service token for the given microservice.

microservice="${1:-bulk_scan_processor}"

curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"microservice":"'${microservice}'"}' \
  http://localhost:4502/testing-support/lease
